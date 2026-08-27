include "root" {
  path = find_in_parent_folders("root.hcl")
}

terraform {
  source = "../../modules/vpc"
}

inputs = {
  network_name = "job-checker-dev-vpc"
  region       = "us-east1"

  subnet_name  = "job-checker-dev-subnet"
  subnet_cidr  = "10.0.0.0/16"
}