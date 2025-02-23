# variable "instance_template_id" {
#   type = string
# }

variable "zone" {
  type = list(string)
}

variable "region" {
  type = string
}

variable "group_name" {
  type = string
}

variable "disk_image_name" {
  type = string
}

variable "api_base_url" {
  type = string
}

variable "instances_type" {
  type = string
}

# variable "group_name" {
#   type = string
# }

variable "subnet_id" {
  type = string
}

variable "ssh_public_key" {
  type = string
}
