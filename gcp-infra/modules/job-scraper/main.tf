# Variables
variable "project_id" { type = string }
variable "region" { type = string }
variable "docker_image" { type = string }
variable "env_vars" { type = map(string) }

variable "db_password" { type = string }
variable "telegram_token" { type = string }

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


# 3. Service Accounts and permissions
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

# 4. Cloud Run Job configuration
resource "google_cloud_run_v2_job" "scraper_job" {
  name     = "job-scraper-task"
  location = var.region

  template {
    template {
      timeout = "1800s"

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
}

# 5. Cloud Scheduler configuration

resource "google_cloud_scheduler_job" "invoke_scraper" {
  name             = "trigger-job-scraper"
  description      = "Paleidzia Java skreperi"
  schedule         = "13 */1 * * *"
  time_zone        = "Europe/Vilnius"
  region           = var.region

  http_target {
    http_method = "POST"
    uri         = "https://${var.region}-run.googleapis.com/apis/run.googleapis.com/v1/namespaces/${var.project_id}/jobs/${google_cloud_run_v2_job.scraper_job.name}:run"

    oauth_token {
      service_account_email = google_service_account.scheduler_sa.email
    }
  }
}