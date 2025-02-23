import os
import requests
from typing import List
from src.gdelt.file_manager import FileManager
from colorama import Fore  # type: ignore


class GdeltProcessor:
    """
    La classe GdeltProcessor est responsable du traitement des données GDELT.
    Elle télécharge les dernières mises à jour depuis une URL spécifiée,
    télécharge les fichiers CSV correspondants, et envoie ces fichiers à Kafka.

    Attributes:
        kafka_manager (KafkaManager): L'objet responsable de l'envoi des données à Kafka.
        file_manager (FileManager): L'objet responsable de la gestion des fichiers téléchargés.
        url_lastupdate (str): L'URL où les mises à jour de GDELT sont disponibles.

    Methods:
        run(): Fonction principale de la classe GdeltProcessor.
        __download_last_updates(): Fonction privée pour télécharger les dernières mises à jour depuis l'URL spécifiée.
        __download_csv_files_from_lines(lines): Fonction privée pour télécharger les fichiers CSV correspondants à chacune des lignes du fichier principal de GDELT
        __send_csv_files_to_kafka(csv_files): Fonction privée pour envoyer les fichiers CSV à Kafka.
        __send_csv_file_to_kafka(csv_file): Fonction privée pour envoyer un fichier CSV à Kafka.
    """

    def __init__(self, kafka_manager, url_lastupdate, destination_folder):
        self.kafka_manager = kafka_manager
        self.file_manager = FileManager(destination_folder)
        self.url_lastupdate = url_lastupdate

    def run(self):
        lines = self.__download_last_updates()
        print(
            f"{Fore.GREEN}#######################Téléchargement#######################{Fore.WHITE}"
        )
        self.__download_csv_files_from_lines(lines)
        print(
            f"{Fore.GREEN}#######################Renommage des fichiers#######################{Fore.WHITE}"
        )
        csv_files = self.file_manager.get_csv_files()
        print(
            f"{Fore.GREEN}#######################Envoie dans kafka#######################{Fore.WHITE}"
        )
        self.__send_csv_files_to_kafka(csv_files)
        print(
            f"{Fore.GREEN}#######################Fin du programme#######################{Fore.WHITE}"
        )
        self.kafka_manager.flush()

    def __download_last_updates(self) -> List[str]:
        response = requests.get(self.url_lastupdate)
        if response.status_code != 200:
            raise Exception(
                f"Error while downloading GDELT last updates: {str(response)}"
            )
        return response.text.splitlines()

    def __download_csv_files_from_lines(self, lines):
        for line in lines:
            if (
                line.endswith(".export.CSV.zip")
                or line.endswith(".mentions.CSV.zip")
                or line.endswith(".gkg.csv.zip")
            ):
                url = line.split()[-1]
                self.file_manager.download_file(url)

    def __send_csv_files_to_kafka(self, csv_files):
        for csv_file in csv_files:
            self.__send_csv_file_to_kafka(csv_file)

    def __send_csv_file_to_kafka(self, csv_file):
        with open(csv_file, "r", encoding="utf-8", errors="ignore") as file:
            content = file.read()
            self.kafka_manager.send_to_kafka(
                content, os.path.basename(csv_file).encode("utf-8")
            )
            print(f"Envoyé à Kafka : {csv_file}")
