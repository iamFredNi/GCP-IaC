variable "project_id" {
  type        = string
  description = "Project id."
  default     = "mmsdtd"
}

variable "main_vpc_id" {
  type = string
}

variable "vpc_network_self_link" {
  type        = string
  description = "VPC self link."
}

variable "subnetwork_self_link" {
  type        = string
  description = "Subnet self link."
}

variable "lb_subnet_id" {
  type = string
}

variable "instances" {
  type = list(object({
    name = string
    zone = string
  }))
}

variable "instances_type" {
  type = string
}

variable "instances_subnet_id" {
  type = string
}

variable "ssh_public_key" {
  type = string
}