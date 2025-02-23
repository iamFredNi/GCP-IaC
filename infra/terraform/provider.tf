terraform {

  backend "gcs" {
    bucket = "project-terraform-bucket"
    prefix = "terraform/"
  }

  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~>6.8.0"
    }
  }
}

provider "google" {
  project     = "mmsdtd"
  region      = "europe-west1"
  zone        = "europe-west1-b"
  credentials = "../../personnal-terraform_srv-keys.json"
}
