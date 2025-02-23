module "ingestion_instances" {
  source = "../compute"

  count = length(var.instances)

  subnet_id      = var.instances_subnet_id
  ssh_public_key = var.ssh_public_key

  instance_type = var.instances_type
  instance_name = var.instances[count.index].name
  instance_zone = var.instances[count.index].zone
}