include "root" {
  path = find_in_parent_folders("root.hcl")
}

terraform {
  source = "../../modules/eks"
}

dependency "vpc" {
  config_path = "../vpc"
}

inputs = {
  cluster_name = "job-checker-dev-cluster"
  vpc_id       = dependency.vpc.outputs.vpc_id
  subnet_ids   = dependency.vpc.outputs.private_subnets
}