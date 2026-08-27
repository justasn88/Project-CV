include "root" {
  path = find_in_parent_folders("root.hcl")
}

terraform {
  source = "../../modules/database"
}

locals {
  # Read YAML file
  helm_values = yamldecode(file("../../job-scraper-chart/values-prod.yaml"))
}

inputs = {
  region      = "us-east1"

  # Dynamically set DB settings
  db_username = local.helm_values.database.user
  db_password = local.helm_values.database.password
  db_name     = local.helm_values.database.name
}