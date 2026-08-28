include "root" {
  path = find_in_parent_folders("root.hcl")
}

terraform {
  source = "../../modules/job-scraper"
}

locals {
  helm_values = yamldecode(file("../../job-scraper-chart/values-prod.yaml"))
}

inputs = {
  project_id   = "global-impulse-504612-k0"
  region       = "us-east1"
  docker_image = "${local.helm_values.image.repository}:${local.helm_values.image.tag}"

  env_vars = {
    "SPRING_DATASOURCE_URL"      = "jdbc:postgresql://34.138.218.102:5432/${local.helm_values.database.name}"
    "SPRING_DATASOURCE_USERNAME" = local.helm_values.database.user
    "SPRING_DATASOURCE_PASSWORD" = local.helm_values.database.password

    "telegram.botToken"          = local.helm_values.telegram.botToken
    "telegram.chatId"            = local.helm_values.telegram.chatId

    "scraper.userAgent"          = local.helm_values.scraper.userAgent

    "scraper.providers.cvbankas.url"      = local.helm_values.scraper.providers.cvbankas.url
    "scraper.providers.cvbankas.name"     = local.helm_values.scraper.providers.cvbankas.name

    "scraper.providers.cvmarket.url"      = local.helm_values.scraper.providers.cvmarket.url
    "scraper.providers.cvmarket.name"     = local.helm_values.scraper.providers.cvmarket.name

    "scraper.providers.cvonline.url"      = local.helm_values.scraper.providers.cvonline.url
    "scraper.providers.cvonline.name"     = local.helm_values.scraper.providers.cvonline.name
  }
}