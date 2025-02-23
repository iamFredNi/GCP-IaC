import os
from dotenv import load_dotenv  # type: ignore
from src.gdelt.main_app import MainApp

# Charger les variables d'environnement
load_dotenv()

# Utilisation du programme
if __name__ == "__main__":
    kafka_host = os.getenv("KAFKA_HOST")
    kafka_port = os.getenv("KAFKA_PORT")
    topic = "gdelt"
    url_lastupdate = "http://data.gdeltproject.org/gdeltv2/lastupdate.txt"
    destination_folder = "gdelt/gdelt_files"

    app = MainApp(kafka_host, kafka_port, topic, url_lastupdate, destination_folder)
    app.run()
