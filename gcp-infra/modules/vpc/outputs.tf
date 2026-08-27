output "network_name" {
  description = "GCP VPC network name"
  value       = google_compute_network.vpc.name
}

output "subnetwork_name" {
  description = "GCP subnet name"
  value       = google_compute_subnetwork.subnet.name
}