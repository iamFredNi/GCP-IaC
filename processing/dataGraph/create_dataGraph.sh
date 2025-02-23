gcloud functions deploy generateGraphData \
    --runtime nodejs22 \
    --trigger-http \
    --allow-unauthenticated	\   
	--region us-central1

gcloud scheduler jobs create http daily-generateGraphData-job   
	--schedule "0 8 * * *"   
	--uri "https://us-central1-mmsdtd.cloudfunctions.net/generateGraphData"   
	--http-method POST   
	--location us-central1

gcloud scheduler jobs list --location us-central1

curl https://us-central1-mmsdtd.cloudfunctions.net/generateGraphData