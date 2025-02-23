source "googlecompute" "app_disk_image" {
  project_id          = "mmsdtd"
  zone                = "europe-west1-b"
  machine_type        = "e2-medium"
  source_image_family = "debian-12"
  ssh_username        = "ansible"
  image_name          = "app-pythie-disk-image"
}

build {
  sources = ["source.googlecompute.app_disk_image"]
provisioner "shell-local" {
    inline = ["cd ../app && npm install && npm run build"]
  }
	

  provisioner "shell" {
    execute_command = "echo 'packer' | sudo -S sh -c '{{ .Vars }} {{ .Path }}'"
    inline = [
      "mkdir -p /tmp/pythie/build",
      "chmod 777 /tmp/pythie/build /tmp/pythie",
      "mkdir -p /var/www/pythie/build",
      "chmod 777 /var/www/pythie",
    ]
  }

  provisioner "file" {
    sources      = [  "../app/server.mjs", "../app/start.mjs", "../app/package.json","../app/build", "../app/srv" ]
    destination = "/tmp/pythie/"
  }


  provisioner "shell" {
    execute_command = "echo 'packer' | sudo -S sh -c '{{ .Vars }} {{ .Path }}'"
    inline = [
      "mv /tmp/pythie/* /var/www/pythie",
    ]
  }

  provisioner "ansible" {
    playbook_file   = "${path.root}/../ansible/app/playbook.yml"
    user            = "ansible"
    extra_arguments = ["--ssh-extra-args='-o StrictHostKeyChecking=no'"]
  }

  post-processor "manifest" {
    output = "app-disk-image.json"
  }
}
