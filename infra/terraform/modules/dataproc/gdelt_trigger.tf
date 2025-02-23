resource "google_storage_bucket_object" "trigger_zip_code" {
  name   = "python/gdelt_trigger.py"
  bucket = google_storage_bucket.dataproc_bucket.name
  source = "${path.module}/gdelt_trigger.zip"
}

resource "google_cloudfunctions_function" "gdelt_trigger_function" {
  name        = "gdelt-trigger-function"
  runtime     = "python39"
  entry_point = "trigger_gdelt_job"
  region      = var.region

  source_archive_bucket = google_storage_bucket.dataproc_bucket.name
  source_archive_object = google_storage_bucket_object.trigger_zip_code.name

  trigger_http = true

  environment_variables = {
    PROJECT_ID   = var.project_id
    REGION       = var.region
    CLUSTER_NAME = var.cluster_name
    JAR_URI      = "gs://${google_storage_bucket.dataproc_bucket.name}/jars/gdelt.jar"
    KAFKA_ARGS   = "${var.kafka_cluster_ip}:9092"
  }
}

resource "google_cloud_scheduler_job" "dataproc_scheduler" {
  name        = "dataproc-trigger-scheduler"
  description = "Triggers the Dataproc job every 15 minutes"
  schedule    = "*/15 * * * *"
  time_zone   = "UTC"

  http_target {
    http_method = "POST"
    uri         = google_cloudfunctions_function.gdelt_trigger_function.https_trigger_url

    oidc_token {
      service_account_email = "${var.project_id}@appspot.gserviceaccount.com"
    }

    headers = {
      "Content-Type" = "application/json"
    }
  }
}