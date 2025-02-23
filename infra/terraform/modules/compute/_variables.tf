variable "instance_name" {
  type        = string
  description = "Name for the compute instance."
}

variable "instance_type" {
  type        = string
  description = "Compute instance type to use."
}

variable "instance_zone" {
  type        = string
  description = "Zone where the instance type will be deployed."
}

variable "subnet_id" {
  type        = string
  description = "Subnetwork id for the compute instance."
}

variable "ssh_public_key" {
  type        = string
  description = "Public SSH key used to run Ansible on the instance."
}