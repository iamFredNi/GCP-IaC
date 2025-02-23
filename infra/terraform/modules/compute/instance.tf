resource "google_compute_instance" "vm_instance" {
  name         = var.instance_name
  machine_type = var.instance_type
  zone         = var.instance_zone

  boot_disk {
    initialize_params {
      image = "debian-cloud/debian-11"
    }
  }

  network_interface {
    subnetwork = var.subnet_id
    access_config {}
  }

  metadata = {
    ssh-keys = "ansible:${var.ssh_public_key}"
  }

  service_account {
    email  = "terraform-srv@mmsdtd.iam.gserviceaccount.com"
    scopes = ["cloud-platform"]
  }

  tags = ["ssh", "streamer"]
}
