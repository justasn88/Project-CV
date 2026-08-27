resource "google_container_cluster" "primary" {
  name     = var.cluster_name
  location = var.region

  network    = var.network
  subnetwork = var.subnetwork

  remove_default_node_pool = true
  initial_node_count       = 1

  deletion_protection = false
}

resource "google_container_node_pool" "job_checker_nodes" {
  name       = "${var.cluster_name}-node-pool"
  location   = var.region
  cluster    = google_container_cluster.primary.name

  initial_node_count = var.min_nodes

  autoscaling {
    min_node_count = var.min_nodes
    max_node_count = var.max_nodes
  }

  node_config {
    machine_type = var.machine_type
    disk_size_gb = 30
    disk_type    = "pd-standard"

    oauth_scopes = [
      "https://www.googleapis.com/auth/cloud-platform"
    ]
  }
}

variable "cluster_name" { type = string }
variable "region" { type = string }
variable "network" { type = string }
variable "subnetwork" { type = string }
variable "machine_type" { type = string }
variable "min_nodes" { type = number }
variable "max_nodes" { type = number }