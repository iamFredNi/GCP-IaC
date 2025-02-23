# resource "google_compute_disk" "template_disk" {
#   name  = "${var.group_name}-disk-${var.disk_image_zone}"
#   image = var.disk_image_name
#   zone = var.disk_image_zone
#   size  = 20
#   type  = "pd-ssd"
# }

resource "google_compute_instance_template" "instance_template" {
  name = "${var.group_name}-template"

  machine_type = var.instances_type
  disk {
    source_image = var.disk_image_name //google_compute_disk.template_disk.name
    auto_delete  = true
    boot         = true
  }

  network_interface {
    subnetwork = var.subnet_id
  }

  service_account {
    email  = "instance-secret-accessor@mmsdtd.iam.gserviceaccount.com"
    scopes = ["https://www.googleapis.com/auth/cloud-platform"]
  }

  metadata = {
    ssh-keys = "ansible:${var.ssh_public_key}"
  }

  shielded_instance_config {
    enable_integrity_monitoring = true
    enable_secure_boot          = true
    enable_vtpm                 = true
  }

  tags                    = ["ssh", "${var.group_name}"]
  metadata_startup_script = <<EOT
#!/bin/bash
echo "export API_BASE_URL=${var.api_base_url}" >> /etc/profile.d/custom_env.sh
source /etc/profile.d/custom_env.sh
EOT
}
