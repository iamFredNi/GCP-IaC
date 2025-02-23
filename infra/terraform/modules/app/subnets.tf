resource "google_compute_subnetwork" "app_subnet" {
  name = "app-subnet"

  network       = var.main_vpc_id
  ip_cidr_range = "10.0.3.0/24"

  private_ip_google_access = true
}

resource "google_compute_firewall" "allow-app-only" {
  name    = "allow-app"
  network = var.main_vpc_name
  allow {
    protocol = "tcp"
    ports    = ["80"]
  }

  source_ranges = ["0.0.0.0/0"]
}
