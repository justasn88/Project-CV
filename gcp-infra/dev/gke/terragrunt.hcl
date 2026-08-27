include "root" {
  path = find_in_parent_folders("root.hcl")
}

terraform {
  source = "../../modules/gke"
}

dependency "vpc" {
  config_path = "../vpc"
}

inputs = {
  cluster_name = "job-checker-dev-gke"
  network    = dependency.vpc.outputs.network_name
  subnetwork = dependency.vpc.outputs.subnetwork_name

  region       = "us-east1"
  machine_type = "e2-micro"
  min_nodes    = 1
  max_nodes    = 1
}