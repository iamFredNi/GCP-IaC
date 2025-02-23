module "networking" {
  source = "./modules/networking"
}

module "streamer_instance" {
  source = "./modules/compute"

  instance_name = "streamer-instance"
  instance_type = "e2-medium"

  instance_zone = "europe-west1-b"
  subnet_id     = module.networking.streamer_subnet.id

  ssh_public_key = module.networking.ssh_public_key
}

module "ingestion" {
  source = "./modules/ingestion"

  main_vpc_id           = module.networking.main_vpc.id
  vpc_network_self_link = module.networking.main_vpc.self_link
  subnetwork_self_link  = module.networking.kafka_subnet.self_link
  lb_subnet_id          = module.networking.kafka_subnet.id
  instances_type        = "e2-medium"

  instances = [
    {
      name = "kafka-broker-1"
      zone = "europe-west1-b"
    },
    {
      name = "kafka-broker-2"
      zone = "europe-west1-c"
    },
    {
      name = "kafka-broker-3"
      zone = "europe-west1-d"
    }
  ]

  instances_subnet_id = module.networking.kafka_subnet.id
  ssh_public_key      = module.networking.ssh_public_key
}

module "dataproc" {
  source = "./modules/dataproc"

  cluster_name = "spark-cluster"
  region       = "europe-west1"

  service_account = "spark-cluster-srv@mmsdtd.iam.gserviceaccount.com"

  vpc_id    = module.networking.main_vpc.id
  subnet_id = module.networking.spark_subnet.id

  coinbase_jar_path = "${path.module}/../../processing/coinbase_processing/target/coinbase_processing.jar"
  gdelt_jar_path    = "${path.module}/../../processing/gdelt_processing/target/gdelt_processing.jar"

  master_machine_type = "n1-standard-2"
  worker_machine_type = "n1-standard-2"
  worker_count        = 2

  kafka_cluster_ip = module.ingestion.ingestion_lb_ip
}

module "app" {
  source = "./modules/app"

  main_vpc_id   = module.networking.main_vpc.id
  main_vpc_name = module.networking.main_vpc.name

  zones = [
    "europe-west1-b",
    "europe-west1-c" // IL FAUT BIEN 1 DISQUE / AZ
  ]

  disk_image_name = "app-pythie-disk-image"
  disk_image_zone = "europe-west1-b"
  region          = "europe-west1"

  ssh_public_key = module.networking.ssh_public_key

  //bucket_writer_srv_email = module.storage.bucket_writer_srv_email
}

module "ansible_files" {
  source = "./modules/ansible"

  streamers_servers_public_ips = [
    module.streamer_instance.instance_public_ip
  ]

  kafka_servers_public_ips  = module.ingestion.ingestion_instances_public_ip
  kafka_servers_private_ips = module.ingestion.ingestion_instances_private_ips
}

output "kafka_host" {
  value = module.ingestion.ingestion_lb_ip
}
