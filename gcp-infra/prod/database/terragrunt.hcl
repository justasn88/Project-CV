include "root" {
  path = find_in_parent_folders("root.hcl")
}

terraform {
  source = "../../modules/database"
}

locals {
  helm_values = yamldecode(file("../values-prod.yaml"))
}

inputs = {
  region      = "us-east1"

  db_username = local.helm_values.database.user
  db_password = local.helm_values.database.password
  db_name     = local.helm_values.database.name
}