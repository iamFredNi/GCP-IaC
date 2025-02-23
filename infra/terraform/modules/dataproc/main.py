import googleapiclient.discovery
from os import environ

def trigger_gdelt_job(request):
    project_id = environ["PROJECT_ID"]
    region = environ["REGION"]
    cluster_name = environ["CLUSTER_NAME"]
    job = {
        "placement": {
            "clusterName": cluster_name
        },
        "sparkJob": {
            "mainJarFileUri": environ["JAR_URI"],
            "args": [environ["KAFKA_ARGS"]]
        }
    }
    
    dataproc = googleapiclient.discovery.build('dataproc', 'v1')
    result = dataproc.projects().regions().jobs().submit(
        projectId=project_id,
        region=region,
        body={"job": job}
    ).execute()

    return f"Job submitted: {result['reference']['jobId']}"