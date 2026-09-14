generate "provider" {
  path      = "provider.tf"
  if_exists = "overwrite_terragrunt"
  contents  = <<EOF
provider "google" {
  project = "global-impulse-504612-k0"
  region  = "us-east1"

  default_labels = {
    project    = "job-checker"
    managed-by = "terragrunt"
  }
}
EOF
}