include "root" {
  path = find_in_parent_folders("root.hcl")
}

terraform {
  source = "../../modules/job-scraper"
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
    "SPRING_DATASOURCE_URL"      = local.helm_values.database.url
    "SPRING_DATASOURCE_USERNAME" = local.helm_values.database.user
    "telegram.chatId"            = local.helm_values.telegram.chatId
    "scraper.userAgent"          = local.helm_values.scraper.userAgent

    "scraper.providers.cvbankas.url"      = local.helm_values.scraper.providers.cvbankas.url
    "scraper.providers.cvbankas.name"     = local.helm_values.scraper.providers.cvbankas.name
    "scraper.providers.cvbankas.delay.min" = local.helm_values.scraper.providers.cvbankas.delay.min
    "scraper.providers.cvbankas.delay.max" = local.helm_values.scraper.providers.cvbankas.delay.max

    "scraper.providers.cvmarket.url"      = local.helm_values.scraper.providers.cvmarket.url
    "scraper.providers.cvmarket.name"     = local.helm_values.scraper.providers.cvmarket.name
    "scraper.providers.cvmarket.delay.min" = local.helm_values.scraper.providers.cvmarket.delay.min
    "scraper.providers.cvmarket.delay.max" = local.helm_values.scraper.providers.cvmarket.delay.max

    "scraper.providers.cvonline.url"      = local.helm_values.scraper.providers.cvonline.url
    "scraper.providers.cvonline.name"     = local.helm_values.scraper.providers.cvonline.name
    "scraper.providers.cvonline.delay.min" = local.helm_values.scraper.providers.cvonline.delay.min
    "scraper.providers.cvonline.delay.max" = local.helm_values.scraper.providers.cvonline.delay.max

    "scraper.providers.linkedin.url"      = local.helm_values.scraper.providers.linkedin.url
    "scraper.providers.linkedin.name"     = local.helm_values.scraper.providers.linkedin.name
    "scraper.providers.linkedin.delay.min" = local.helm_values.scraper.providers.linkedin.delay.min
    "scraper.providers.linkedin.delay.max" = local.helm_values.scraper.providers.linkedin.delay.max
  }
  scrapers_cron = {
    "ALL" = local.helm_values.scraper.providers.all_cron
    "linkedin" = local.helm_values.scraper.providers.linkedin.cron
  }
}