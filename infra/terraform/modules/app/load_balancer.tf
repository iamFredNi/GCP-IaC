module "instance_groups" {
  source = "../instance_group"

  group_name = "app-node"
  zone       = var.zones
  region     = var.region

  disk_image_name = var.disk_image_name
  instances_type  = "e2-medium"
  ssh_public_key  = var.ssh_public_key
  subnet_id       = google_compute_subnetwork.app_subnet.id
  api_base_url    = google_compute_address.app_lb_ip.address
}

resource "google_compute_region_backend_service" "app_lb" {
  name                  = "app-lb"
  protocol              = "TCP"
  timeout_sec           = 1800
  load_balancing_scheme = "EXTERNAL"
  region                = "europe-west1"

  backend {
    group          = module.instance_groups.instance_group
    balancing_mode = "CONNECTION"
  }

  health_checks = [
    google_compute_region_health_check.app_lb_health_check.self_link
  ]

  session_affinity = "CLIENT_IP" # Ou "CLIENT_IP_PROTO" selon vos besoins
}

resource "google_compute_region_health_check" "app_lb_health_check" {
  name               = "app-lb-health-check"
  timeout_sec        = 10
  check_interval_sec = 10
  tcp_health_check {
    port = 80
  }
}

resource "google_compute_address" "app_lb_ip" {
  name         = "app-lb-ip"
  region       = "europe-west1"
  address_type = "EXTERNAL"
}

resource "google_compute_forwarding_rule" "app_lb_rule" {
  name                  = "app-tcp-lb-rule"
  load_balancing_scheme = "EXTERNAL"
  ip_protocol           = "TCP"
  backend_service       = google_compute_region_backend_service.app_lb.self_link
  ip_address            = google_compute_address.app_lb_ip.address
  region                = "europe-west1"
  ports                 = ["80"]
}

