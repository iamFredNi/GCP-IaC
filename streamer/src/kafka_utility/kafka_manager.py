from kafka import KafkaProducer  # type: ignore


class KafkaManager:
    """
    La classe KafkaManager est responsable de l'envoi de messages à un topic Kafka.
    Elle initialise un producteur Kafka avec le topic, l'hôte et le port spécifiés,
    et fournit des méthodes pour envoyer des messages et vider le producteur.
    """

    def __init__(self, topic, kafka_host, kafka_port):
        self.topic = topic
        self.producer = KafkaProducer(
            bootstrap_servers=f"{kafka_host}:{kafka_port}",
            api_version=(0, 11, 5),
            value_serializer=lambda v: v.encode("utf-8"),
        )

    def send_to_kafka(self, message, key):
        self.producer.send(self.topic, value=message, key=key)
        print(f"Message envoyé à Kafka sur le topic {self.topic} et avec la clé {key}.")

    def flush(self):
        self.producer.flush()
        print("Flush de kafka")
