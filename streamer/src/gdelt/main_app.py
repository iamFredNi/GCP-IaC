from src.kafka_utility.kafka_manager import KafkaManager
from src.gdelt.file_manager import FileManager
from src.gdelt.gdelt_processor import GdeltProcessor


class MainApp:
    """
    Application principale pour la gestion des appels à la base de données de GDELT.
    Elle récupère les fichiers de GDELT et les envoie à Kafka.
    La base de données de GDELT étant mise à jour toutes les 15 mins, il faut executer cette fonction toutes
    les 15 minutes en conséquence.
    """

    def __init__(
        self, kafka_host, kafka_port, topic, url_lastupdate, destination_folder
    ):
        self.kafka_manager = KafkaManager(topic, kafka_host, kafka_port)
        self.file_manager = FileManager(destination_folder)
        self.gdelt_processor = GdeltProcessor(
            self.kafka_manager, url_lastupdate, destination_folder
        )

    def run(self):
        self.gdelt_processor.run()
        # Supprimer tous les fichiers du dossier après envoi à Kafka
        self.file_manager.clean_up()
        print("Pipeline GDELT terminé et fichiers supprimés.")
