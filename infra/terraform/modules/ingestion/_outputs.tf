output "ingestion_instances_public_ip" {
  value = module.ingestion_instances[*].instance_public_ip
}

output "ingestion_instances_private_ips" {
  value = module.ingestion_instances[*].instance_private_ip
}

output "ingestion_lb_ip" {
  value = google_compute_forwarding_rule.ingestion_lb_rule.ip_address
}