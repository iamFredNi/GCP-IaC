import json
import base64
from google.cloud import storage
import re

def concat_files(event, context):
    print(f"Event received: {event}")

    if "data" not in event:
        print("No data received in the event.")
        return

    try:
        message_data = base64.b64decode(event["data"]).decode("utf-8")
        print(f"Decoded message: {message_data}")
        data = json.loads(message_data)
    except Exception as e:
        print(f"Error decoding message: {e}")
        return

    print(f"Parsed data: {data}")

    # Lire le nom du bucket et le préfixe
    bucket_name = data["bucket"]
    prefix = data.get("prefix", "")

    # Initialiser le client GCS
    storage_client = storage.Client()
    bucket = storage_client.bucket(bucket_name)

    # Lister tous les blobs correspondant au préfixe
    all_blobs = list(bucket.list_blobs(prefix=prefix))

    # Grouper les blobs par base de date AAAAMMJJ
    files_by_date = {}
    for blob in all_blobs:
        file_name = blob.name.split("/")[-1]

        # Ignorer les fichiers dont le nom est déjà au format AAAAMMJJ.json
        if re.match(r'^\d{8}\.json$', file_name):
            print(f"Ignoring file: {file_name}")
            continue

        # Identifier la date AAAAMMJJ dans le nom du fichier
        if "-" in file_name:
            date_part = file_name.split("-")[0][:8]  # Pour les fichiers avec un tiret
        else:
            date_part = file_name.split(".")[0][:8]  # Pour les fichiers sans tiret

        files_by_date.setdefault(date_part, []).append(blob)

    # Concatenation des fichiers
    for date_part, blob_group in files_by_date.items():
        concatenated_content = []
        for blob in blob_group:
            # Charger chaque blob comme JSON et l'ajouter à la liste
            try:
                blob_content = json.loads(blob.download_as_text())
                if isinstance(blob_content, list):
                    concatenated_content.extend(blob_content)  # Ajouter les éléments si c'est une liste
                else:
                    concatenated_content.append(blob_content)  # Ajouter l'objet directement
            except Exception as e:
                print(f"Error reading file {blob.name}: {e}")

        # Enregistrer le contenu concaténé dans un nouveau blob comme une liste JSON valide
        new_file_name = f"{prefix}{date_part}.json"
        new_blob = bucket.blob(new_file_name)
        new_blob.upload_from_string(
            json.dumps(concatenated_content, indent=2),  # Convertir la liste en JSON formaté
            content_type="application/json"
        )

        # Supprimer les fichiers originaux
        for blob in blob_group:
            blob.delete()

    print(f"Concatenation completed for prefix: {prefix}.")
