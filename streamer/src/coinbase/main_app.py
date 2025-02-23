from src.kafka_utility.kafka_manager import KafkaManager
from src.coinbase.ws_manager import WebSocketManager


class MainApp:
    """
    Classe principale de la gestion des connections à l'API Coinbase
    """

    def __init__(self, kafka_host, kafka_port, topic, ws_url):
        self.kafka_manager = KafkaManager(topic, kafka_host, kafka_port)
        self.ws_manager = WebSocketManager(ws_url, self.kafka_manager)

    def run(self):
        """Lance l'application WebSocket Coinbase"""
        self.ws_manager.run()
