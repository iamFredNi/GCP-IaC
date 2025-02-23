locals {
  instances_in_zone_b = [for instance in module.ingestion_instances[*].instance : instance.self_link if endswith(instance.zone, "-b")]
  instances_in_zone_c = [for instance in module.ingestion_instances[*].instance : instance.self_link if endswith(instance.zone, "-c")]
  instances_in_zone_d = [for instance in module.ingestion_instances[*].instance : instance.self_link if endswith(instance.zone, "-d")]
}

resource "google_compute_instance_group" "ingestion_group_1" {
  project   = var.project_id
  name      = "ingestion-group-b"
  zone      = "europe-west1-b"
  instances = local.instances_in_zone_b
}

resource "google_compute_instance_group" "ingestion_group_2" {
  project   = var.project_id
  name      = "ingestion-group-c"
  zone      = "europe-west1-c"
  instances = local.instances_in_zone_c
}

resource "google_compute_instance_group" "ingestion_group_3" {
  project   = var.project_id
  name      = "ingestion-group-d"
  zone      = "europe-west1-d"
  instances = local.instances_in_zone_d
}

resource "google_compute_region_backend_service" "ingestion_lb" {
  name                  = "ingestion-lb"
  protocol              = "TCP"
  timeout_sec           = 10
  load_balancing_scheme = "INTERNAL"
  region                = "europe-west1"

  # enable_cdn = true

  backend {
    group          = google_compute_instance_group.ingestion_group_1.self_link
    balancing_mode = "CONNECTION"
  }

  backend {
    group          = google_compute_instance_group.ingestion_group_2.self_link
    balancing_mode = "CONNECTION"
  }

  backend {
    group          = google_compute_instance_group.ingestion_group_3.self_link
    balancing_mode = "CONNECTION"
  }

  health_checks = [
    google_compute_health_check.ingestion_lb_health_check.self_link
  ]
}

resource "google_compute_health_check" "ingestion_lb_health_check" {
  name               = "ingestion-lb-health-check"
  timeout_sec        = 10
  check_interval_sec = 10
  tcp_health_check {
    port = 9092
  }
}

resource "google_compute_address" "ingestion_lb_ip" {
  name         = "lb-ip"
  region       = "europe-west1"
  address_type = "INTERNAL"
  subnetwork   = var.subnetwork_self_link
}

resource "google_compute_forwarding_rule" "ingestion_lb_rule" {
  network               = var.main_vpc_id
  subnetwork            = var.lb_subnet_id
  name                  = "ingestion-tcp-lb-rule"
  load_balancing_scheme = "INTERNAL"
  ip_protocol           = "TCP"
  backend_service       = google_compute_region_backend_service.ingestion_lb.self_link
  ip_address            = google_compute_address.ingestion_lb_ip.address
  region                = "europe-west1"
  ports                 = ["9092"]
}
