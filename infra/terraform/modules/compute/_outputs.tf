output "instance_public_ip" {
  description = "Public ip address of the created instance."
  value       = google_compute_instance.vm_instance.network_interface[0].access_config[0].nat_ip
}

output "instance_private_ip" {
  description = "Private ip address of the created instance."
  value       = google_compute_instance.vm_instance.network_interface[0].network_ip
}

output "instance" {
  value = google_compute_instance.vm_instance
}