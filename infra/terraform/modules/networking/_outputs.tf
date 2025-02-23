output "main_vpc" {
  value = google_compute_network.main_vpc
}

output "streamer_subnet" {
  value = google_compute_subnetwork.streamer_subnet
}

output "kafka_subnet" {
  value = google_compute_subnetwork.kafka_subnet
}

output "spark_subnet" {
  value = google_compute_subnetwork.spark_subnet
}

output "ssh_public_key" {
  value = tls_private_key.ssh_key.public_key_openssh
}