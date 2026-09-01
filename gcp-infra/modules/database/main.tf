resource "google_compute_firewall" "allow_postgres" {
  name    = "allow-postgres"
  network = "default"

  allow {
    protocol = "tcp"
    ports    = ["5432"]
  }
  source_ranges = ["10.0.0.0/16"]
}

resource "google_compute_instance" "postgres_vm" {
  name         = "postgres-free-tier"
  machine_type = "e2-micro"
  zone         = "${var.region}-b"

  boot_disk {
    initialize_params {
      image = "debian-cloud/debian-11"
      size  = 30
      type  = "pd-standard"
    }
  }

  network_interface {
    network = "default"
    access_config {}
  }

  metadata_startup_script = <<-EOT
    #!/bin/bash
    apt-get update
    apt-get install -y docker.io
    systemctl start docker
    systemctl enable docker
    docker run -d \
      --name postgres-db \
      -p 5432:5432 \
      -e POSTGRES_USER=${var.db_username} \
      -e POSTGRES_PASSWORD=${var.db_password} \
      -e POSTGRES_DB=${var.db_name} \
      --restart unless-stopped \
      postgres:15
  EOT
}

variable "region" { type = string }
variable "db_username" { type = string }
variable "db_password" { type = string }
variable "db_name" { type = string }

output "database_ip" {
  description = "Public PostgreSQL server IP address"
  value       = google_compute_instance.postgres_vm.network_interface[0].access_config[0].nat_ip
}