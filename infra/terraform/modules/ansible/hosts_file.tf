resource "local_file" "hosts_file" {
  filename = "${path.module}/../../../ansible/hosts.ini"
  content = templatefile("${path.module}/hosts.ini.tftpl", {
    streamers_ips     = var.streamers_servers_public_ips,
    kafka_public_ips  = var.kafka_servers_public_ips,
    kafka_private_ips = var.kafka_servers_private_ips,
    ingestion_ips     = var.ingestion_servers_public_ips,
    app_ips           = var.app_servers_public_ips
  })
}