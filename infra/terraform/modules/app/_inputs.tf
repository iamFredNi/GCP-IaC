variable "project_id" {
  type        = string
  description = "Project id."
  default     = "mmsdtd"
}

variable "ssh_public_key" {
  type = string
}

variable "main_vpc_id" {
  type = string
}
variable "region" {
  type = string
}
variable "zones" {
  type = list(string)
}

variable "disk_image_name" {
  type = string
}

variable "disk_image_zone" {
  type = string
}

variable "main_vpc_name" {
  type = string
}
