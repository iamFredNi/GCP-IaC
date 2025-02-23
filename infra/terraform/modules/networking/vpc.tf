resource "google_compute_network" "main_vpc" {
  name                    = "main-vpc"
  auto_create_subnetworks = false
  mtu                     = 1460
}

resource "google_compute_subnetwork" "streamer_subnet" {
  name = "streamer-subnet"

  network       = google_compute_network.main_vpc.id
  ip_cidr_range = "10.0.0.0/24"
}

resource "google_compute_subnetwork" "kafka_subnet" {
  name = "kafka-subnet"

  network       = google_compute_network.main_vpc.id
  ip_cidr_range = "10.0.1.0/24"
}

resource "google_compute_subnetwork" "spark_subnet" {
  name = "spark-subnet"

  network       = google_compute_network.main_vpc.id
  ip_cidr_range = "10.0.2.0/24"

  private_ip_google_access = true
}

resource "google_compute_router" "nat_router" {
  name    = "dataproc-nat-router"
  network = google_compute_network.main_vpc.id
}

resource "google_compute_router_nat" "cloud_nat" {
  name                               = "dataproc-cloud-nat"
  router                             = google_compute_router.nat_router.name
  nat_ip_allocate_option             = "AUTO_ONLY"
  source_subnetwork_ip_ranges_to_nat = "ALL_SUBNETWORKS_ALL_IP_RANGES"
}

resource "google_compute_firewall" "allow-ssh-only" {
  name    = "allow-ssh"
  network = google_compute_network.main_vpc.name

  allow {
    protocol = "tcp"
    ports    = ["22"]
  }

  source_ranges = ["0.0.0.0/0"]
  target_tags   = ["ssh"]
}

resource "google_compute_firewall" "allow-http-traffic" {
  name    = "allow-http"
  network = google_compute_network.main_vpc.name

  allow {
    protocol = "tcp"
    ports    = ["80", "443"]
  }

  direction = "EGRESS"
}

resource "google_compute_firewall" "allow-kafka-from-streamer" {
  name    = "allow-kafka"
  network = google_compute_network.main_vpc.name

  allow {
    protocol = "tcp"
    ports    = ["9092"]
  }

  source_ranges = [google_compute_subnetwork.streamer_subnet.ip_cidr_range]
}

resource "google_compute_firewall" "allow-kafka-internal" {
  name    = "allow-kafka-internal"
  network = google_compute_network.main_vpc.name

  allow {
    protocol = "tcp"
    ports    = ["9092", "9093"]
  }

  source_ranges = [google_compute_subnetwork.kafka_subnet.ip_cidr_range]
}

resource "google_compute_firewall" "allow-spark-to-kafka" {
  name    = "allow-spark-to-kafka"
  network = google_compute_network.main_vpc.name

  allow {
    protocol = "tcp"
    ports    = ["9092"]
  }

  direction = "INGRESS"
  priority  = 1000

  source_tags        = ["dataproc"]
  destination_ranges = [google_compute_subnetwork.kafka_subnet.ip_cidr_range]
}

resource "google_compute_firewall" "dataproc_internal" {
  name    = "dataproc-internal"
  network = google_compute_network.main_vpc.name

  allow {
    protocol = "tcp"
    ports    = ["0-65535"]
  }

  allow {
    protocol = "udp"
    ports    = ["0-65535"]
  }

  allow {
    protocol = "icmp"
  }

  source_tags = ["dataproc"]
  target_tags = ["dataproc"]

  direction = "INGRESS"
  priority  = 1000
}

resource "google_compute_firewall" "dataproc_outbound" {
  name    = "dataproc-outbound"
  network = google_compute_network.main_vpc.name

  allow {
    protocol = "tcp"
    ports    = ["80", "443"]
  }

  direction = "EGRESS"
  priority  = 1000
}

resource "google_compute_firewall" "allow_outbound_internet" {
  name    = "allow-outbound-internet"
  network = "default"

  direction = "EGRESS"

  allow {
    protocol = "all"
  }

  destination_ranges = ["0.0.0.0/0"]
  priority           = 1000
}