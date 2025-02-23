resource "google_compute_region_instance_group_manager" "regional_instance_group" {
  name   = var.group_name
  region = var.region

  version {
    instance_template = google_compute_instance_template.instance_template.id
    name              = "primary"
  }

  distribution_policy_zones = [var.zone[0], var.zone[1]]
  target_size               = 2

  base_instance_name = var.group_name
}

resource "google_compute_region_autoscaler" "regional_autoscaler" {
  name   = "${var.group_name}-autoscaler"
  region = var.region
  target = google_compute_region_instance_group_manager.regional_instance_group.id

  autoscaling_policy {
    max_replicas    = 4
    min_replicas    = 2
    cooldown_period = 60

    cpu_utilization {
      target = 0.9
    }
  }
}
