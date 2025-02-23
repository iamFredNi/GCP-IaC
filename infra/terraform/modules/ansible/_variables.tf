variable "streamers_servers_public_ips" {
  type        = list(string)
  description = "Public IPs addresses of every streamers instances to configure."
  default     = []
}

variable "kafka_servers_private_ips" {
  type        = list(string)
  description = "Private IPs addresses of every kafka instances."
  default     = []
}

variable "kafka_servers_public_ips" {
  type        = list(string)
  description = "Public IPs addresses of every kafka instances to configure."
  default     = []
}

variable "ingestion_servers_public_ips" {
  type        = list(string)
  description = "Public IPs addresses of every ingestion instances to configure."
  default     = []
}

variable "app_servers_public_ips" {
  type        = list(string)
  description = "Public IPs addresses of every application instances to configure."
  default     = []
}