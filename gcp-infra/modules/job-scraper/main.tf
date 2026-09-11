# Variables
variable "project_id" { type = string }
variable "region" { type = string }
variable "docker_image" { type = string }

variable "db_password" { type = string }
variable "telegram_token" { type = string }

variable "scrapers_cron" { type = map(string) }
variable "env_vars" { type = map(string) }

#Google Secret Manager
resource "google_secret_manager_secret" "db_password" {
  secret_id = "job-scraper-db-password"
  replication {
    auto {}
  }
}

resource "google_secret_manager_secret_version" "db_password_version" {
  secret      = google_secret_manager_secret.db_password.id
  secret_data = var.db_password
}

resource "google_secret_manager_secret" "telegram_token" {
  secret_id = "job-scraper-telegram-token"
  replication {
    auto {}
  }
}

resource "google_secret_manager_secret_version" "telegram_token_version" {
  secret      = google_secret_manager_secret.telegram_token.id
  secret_data = var.telegram_token
}


# Service Accounts and permissions
resource "google_service_account" "scraper_runner_sa" {
  account_id   = "scraper-runner-sa"
  display_name = "Cloud Run Job Runner SA"
}

resource "google_secret_manager_secret_iam_member" "db_password_accessor" {
  secret_id = google_secret_manager_secret.db_password.id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${google_service_account.scraper_runner_sa.email}"
}

resource "google_secret_manager_secret_iam_member" "telegram_token_accessor" {
  secret_id = google_secret_manager_secret.telegram_token.id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${google_service_account.scraper_runner_sa.email}"
}

resource "google_service_account" "scheduler_sa" {
  account_id   = "scraper-scheduler-sa"
  display_name = "Cloud Scheduler SA"
}

resource "google_project_iam_member" "scheduler_invoker" {
  project = var.project_id
  role    = "roles/run.invoker"
  member  = "serviceAccount:${google_service_account.scheduler_sa.email}"
}

# Cloud Run Service configuration
resource "google_cloud_run_v2_service" "scraper_service" {
  name     = "job-scraper-api"
  location = var.region

  template {
    service_account = google_service_account.scraper_runner_sa.email

    vpc_access {
      network_interfaces {
        network = "default"
      }
      egress = "PRIVATE_RANGES_ONLY"
    }

    containers {
      image = var.docker_image
      resources {
        limits = { memory = "2048Mi", cpu = "1" }
      }

      dynamic "env" {
        for_each = var.env_vars
        content {
          name  = env.key
          value = env.value
        }
      }

      env {
        name  = "GCP_PROJECT_ID"
        value = var.project_id
      }
      env {
        name  = "GCP_REGION"
        value = var.region
      }
      env {
        name  = "GCP_QUEUE_NAME"
        value = google_cloud_tasks_queue.scraper_queue.name
      }
      env {
        name  = "GCP_SERVICE_ACCOUNT_EMAIL"
        value = google_service_account.scheduler_sa.email
      }
      env {
        name  = "SERVICE_URL"
        value = "https://job-scraper-api-${var.project_id}.${var.region}.run.app"
      }

      env {
        name = "SPRING_DATASOURCE_PASSWORD"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.db_password.secret_id
            version = "latest"
          }
        }
      }
      env {
        name = "telegram.botToken"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.telegram_token.secret_id
            version = "latest"
          }
        }
      }
    }
  }
}

# 5. Cloud Scheduler configuration

resource "google_cloud_scheduler_job" "invoke_scraper" {
  for_each         = var.scrapers_cron

  name             = "trigger-job-scraper-${each.key}"
  description      = "Triggers ${each.key} via API"
  schedule         = each.value
  time_zone        = "Europe/Vilnius"
  region           = var.region

  http_target {
    http_method = "POST"
    uri         = "${google_cloud_run_v2_service.scraper_service.uri}/api/dispatch"

    oidc_token {
      service_account_email = google_service_account.scheduler_sa.email
    }

    headers = {
      "TARGET_SCRAPER" = each.key
    }
  }
}

resource "google_cloud_tasks_queue" "scraper_queue" {
  name     = "scraper-delay-queue"
  location = var.region

  rate_limits {
    max_dispatches_per_second = 5
  }

  retry_config {
    max_attempts = 3
  }
}

resource "google_project_iam_member" "tasks_enqueuer" {
  project = var.project_id
  role    = "roles/cloudtasks.enqueuer"
  member  = "serviceAccount:${google_service_account.scraper_runner_sa.email}"
}

resource "google_project_iam_member" "run_invoker" {
  project = var.project_id
  role    = "roles/run.invoker"
  member  = "serviceAccount:${google_service_account.scheduler_sa.email}"
}

resource "google_service_account_iam_member" "run_act_as_scheduler" {
  service_account_id = google_service_account.scheduler_sa.name
  role               = "roles/iam.serviceAccountUser"
  member             = "serviceAccount:${google_service_account.scraper_runner_sa.email}"
}