resource "google_cloud_run_v2_job" "scraper_job" {
  name     = "job-scraper-task"
  location = var.region

  template {
    template {
      timeout = "1800s"
      containers {
        image = var.docker_image

        resources {
          limits = { memory = "512Mi", cpu = "1" }
        }

        dynamic "env" {
          for_each = var.env_vars
          content {
            name  = env.key
            value = env.value
          }
        }
      }
    }
  }
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

resource "google_cloud_scheduler_job" "invoke_scraper" {
  name             = "trigger-job-scraper"
  description      = "Paleidzia Java skreperi"
  schedule         = "13 */2 * * *"
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

variable "project_id" { type = string }
variable "region" { type = string }
variable "docker_image" { type = string }
variable "env_vars" { type = map(string) } # Visi nustatymai atkeliaus čia