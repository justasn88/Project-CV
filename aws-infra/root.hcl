generate "provider" {
  path      = "provider.tf"
  if_exists = "overwrite_terragrunt"
  contents  = <<EOF
provider "aws" {
  region = "eu-north-1"
  
  default_tags {
    tags = {
      Project     = "job-checker"
      ManagedBy   = "terragrunt"
    }
  }
}
EOF
}