import json
import websocket  # type: ignore
from datetime import datetime


class WebSocketManager:
    """
    La classe WebSocketManager est responsable de la gestion des connexions
    WebSocket et du traitement des messages reçus depuis l'API Coinbase.
    Elle initialise une connexion WebSocket à une URL spécifiée et s'abonne à
    un flux de données Coinbase pour le produit BTC-USD. Lorsque des messages
    sont reçus, ils sont envoyés à un gestionnaire Kafka pour un traitement ultérieur.

    Attributs:

        url (str): L'URL du point d'API Coinbase à requêter.
        kafka_manager (KafkaManager): L'objet responsable de l'envoi des données à Kafka.
        ws (websocket.WebSocketApp): L'objet de connexion WebSocket.

    Méthodes:

        on_message(ws, message): Une fonction callback pour la gestion des messages
            reçus depuis le WebSocket. Elle envoie les données reçues au gestionnaire Kafka.
        on_error(ws, error): Une fonction callback pour la gestion des erreurs WebSocket.
            Elle print le message d'erreur.
        on_close(ws, close_status_code, close_msg): Une fonction callback pour la
            gestion des fermetures de connexion WebSocket. Elle vide le gestionnaire
            Kafka et print le code de statut et le message de fermeture.
        on_open(ws): Une fonction callback pour la gestion de l'ouverture de la connexion WebSocket.
            Elle s'abonne au flux de données Coinbase pour le produit BTC-USD et print le message d'abonnement.
        run(): Initialise et démarre la connexion WebSocket.
    """

    def __init__(self, url, kafka_manager):
        self.url = url
        self.kafka_manager = kafka_manager
        self.ws = None

    def on_message(self, ws, message):
        """Gestion des messages reçus depuis le WebSocket"""
        data = json.loads(message)
        print("Données reçues :", data)
        if data.get("type") == "ticker":
            timestamp = datetime.now().strftime("%Y%m%d%H%M")
            self.kafka_manager.send_to_kafka(
                json.dumps(data), (timestamp + ".json").encode("utf-8")
            )

    def on_error(self, ws, error):
        """Gestion des erreurs WebSocket"""
        print("Erreur WebSocket :", error)

    def on_close(self, ws, close_status_code, close_msg):
        """Gestion de la fermeture du WebSocket"""
        self.kafka_manager.flush()
        print(
            f"Connexion WebSocket fermée : {close_status_code}, message : {close_msg}"
        )

    def on_open(self, ws):
        """Gestion de l'ouverture du WebSocket et abonnement à Coinbase"""
        subscription_message = {
            "type": "subscribe",
            "channels": [{"name": "ticker", "product_ids": ["BTC-USD","ETH-USD","BTC-EUR","USDT-USD","XRP-USD","SOL-USD","DOGE-USD","USDC-USD","ADA-USD","AVAX-USD"]}],
        }
        ws.send(json.dumps(subscription_message))
        print(f"Abonné au flux de Coinbase pour BTC-USD : {subscription_message}")

    def run(self):
        """Initialise et démarre la connexion WebSocket"""
        self.ws = websocket.WebSocketApp(
            self.url,
            on_message=self.on_message,
            on_error=self.on_error,
            on_close=self.on_close,
        )
        self.ws.on_open = self.on_open
        self.ws.run_forever()
