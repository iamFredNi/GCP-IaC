#!/bin/sh

set -e

# Save PWD
tmp=$(echo $PWD)
cd $( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

# Compile Spark
cd ../processing/coinbase_processing
mvn -DskipTests=true package
cd ../gdelt_processing
mvn -DskipTests=true package
cd ../../infra

# zip Python
cd terraform/modules/dataproc
zip -r gdelt_trigger.zip main.py requirements.txt
cd ../../..

# Deploy infrastructurex
cd terraform
terraform init
terraform apply --auto-approve
terraform output -json > raw_outputs.json
cat raw_outputs.json | jq '{kafka_host: .kafka_host.value}' > terraform_outputs.json

# Configure servers
cd ../ansible

# Install ansible plugins
ansible-galaxy install git+https://github.com/GoogleCloudPlatform/google-cloud-ops-agents-ansible.git
ansible-playbook -i hosts.ini playbook.yml --extra-vars="@../terraform/terraform_outputs.json"

# Restore PWD
cd $tmp

