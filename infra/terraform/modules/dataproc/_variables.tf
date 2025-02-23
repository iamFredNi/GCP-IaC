variable "project_id" {
  type = string
  default = "mmsdtd"
}

variable "region" {
  type = string
}

variable "cluster_name" {
  type = string
}

variable "master_machine_type" {
  type = string
}

variable "worker_machine_type" {
  type = string
}

variable "worker_count" {
  type = number
}

variable "coinbase_jar_path" {
  type = string
}

variable "gdelt_jar_path" {
  type = string
}

variable "kafka_cluster_ip" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "subnet_id" {
  type = string
}

variable "service_account" {
  type = string
}