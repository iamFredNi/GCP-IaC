gcloud pubsub topics create concat-daily

gcloud functions deploy concat-files \
    --runtime python310 \
    --trigger-topic concat-daily \
    --entry-point concat_files \
    --source . \
    --region us-central1 \
    --timeout 540s \
    --memory 2GB

gcloud scheduler jobs create pubsub concat-daily-scheduler \
    --schedule "0 0 * * *" \
    --time-zone "UTC" \
    --topic concat-daily \
    --message-body '{"bucket": "test-kafka-sdtd", "prefix": "coinbase/"}' \
    --location us-central1

gcloud functions list
gcloud scheduler jobs list --location us-central1

gcloud pubsub topics publish concat-daily --message='{"bucket": "test-kafka-sdtd", "prefix": "coinbase/"}'
gcloud functions logs read concat-files
