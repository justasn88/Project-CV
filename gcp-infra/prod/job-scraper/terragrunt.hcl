include "root" {
  path = find_in_parent_folders("root.hcl")
}

terraform {
  source = "../../modules/job-scraper"
}

dependency "database" {
  config_path = "../database"
}

locals {
  helm_values = yamldecode(file("../values-prod.yaml"))
}

inputs = {
  project_id   = "global-impulse-504612-k0"
  region       = "us-east1"
  docker_image = "${local.helm_values.image.repository}:${local.helm_values.image.tag}"

  db_password    = local.helm_values.database.password
  telegram_token = local.helm_values.telegram.botToken

  env_vars = {
    "SPRING_DATASOURCE_URL"      = "jdbc:postgresql://${dependency.database.outputs.database_internal_ip}:5432/${local.helm_values.database.name}"
    "SPRING_DATASOURCE_USERNAME" = local.helm_values.database.user
    "telegram.chatId"            = local.helm_values.telegram.chatId
    "scraper.userAgent"          = local.helm_values.scraper.userAgent

    "scraper.providers.cvbankas.url"      = local.helm_values.scraper.providers.cvbankas.url
    "scraper.providers.cvbankas.name"     = local.helm_values.scraper.providers.cvbankas.name

    "scraper.providers.cvmarket.url"      = local.helm_values.scraper.providers.cvmarket.url
    "scraper.providers.cvmarket.name"     = local.helm_values.scraper.providers.cvmarket.name

    "scraper.providers.cvonline.url"      = local.helm_values.scraper.providers.cvonline.url
    "scraper.providers.cvonline.name"     = local.helm_values.scraper.providers.cvonline.name

    "scraper.providers.linkedin.url"      = local.helm_values.scraper.providers.linkedin.url
    "scraper.providers.linkedin.name"     = local.helm_values.scraper.providers.linkedin.name
  }
  scrapers_cron = {
    "cvbankas" = local.helm_values.scraper.providers.cvbankas.cron
    "cvmarket" = local.helm_values.scraper.providers.cvmarket.cron
    "cvonline" = local.helm_values.scraper.providers.cvonline.cron
    "linkedin" = local.helm_values.scraper.providers.linkedin.cron
  }
}