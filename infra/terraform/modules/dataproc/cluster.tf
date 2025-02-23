resource "google_storage_bucket" "dataproc_bucket" {
  name          = "5mmsdtd-dataproc-bucket"
  location      = var.region
  storage_class = "STANDARD"
  force_destroy = true
}

resource "google_storage_bucket_object" "coinbase_jar" {
  name   = "jars/coinbase.jar"
  bucket = google_storage_bucket.dataproc_bucket.name
  source = var.coinbase_jar_path
}

resource "google_storage_bucket_object" "gdelt_jar" {
  name   = "jars/gdelt.jar"
  bucket = google_storage_bucket.dataproc_bucket.name
  source = var.gdelt_jar_path
}

resource "google_dataproc_autoscaling_policy" "example_policy" {
  policy_id = "dataproc-autoscaling-policy"
  location  = var.region

  basic_algorithm {
    yarn_config {
      scale_up_factor               = 0.5
      scale_down_factor             = 0.5
      graceful_decommission_timeout = "600s"

      scale_up_min_worker_fraction   = 0.05
      scale_down_min_worker_fraction = 0.05
    }
  }

  worker_config {
    min_instances = 2
    max_instances = 10
  }

  secondary_worker_config {
    min_instances = 0
    max_instances = 20
  }
}

resource "google_dataproc_cluster" "spark_cluster" {
  name   = var.cluster_name
  region = var.region

  cluster_config {
    staging_bucket = google_storage_bucket.dataproc_bucket.name

    gce_cluster_config {
      subnetwork      = var.subnet_id
      service_account = var.service_account

      internal_ip_only = true

      tags = ["dataproc", "ssh"]
    }

    master_config {
      num_instances = 1
      machine_type  = var.master_machine_type
    }

    worker_config {
      num_instances = var.worker_count
      machine_type  = var.worker_machine_type
    }

    autoscaling_config {
      policy_uri = google_dataproc_autoscaling_policy.example_policy.id
    }
  }
}

resource "google_dataproc_job" "coinbase_job" {
  region       = var.region
  force_delete = true

  placement {
    cluster_name = google_dataproc_cluster.spark_cluster.name
  }

  spark_config {
    main_jar_file_uri = "gs://${google_storage_bucket.dataproc_bucket.name}/jars/coinbase.jar"

    args = [
      "${var.kafka_cluster_ip}:9092"
    ]
  }

  lifecycle {
    replace_triggered_by = [google_storage_bucket_object.coinbase_jar]
  }

  depends_on = [
    google_storage_bucket_object.coinbase_jar
  ]
}
