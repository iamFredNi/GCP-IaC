import os
from dotenv import load_dotenv  # type: ignore
from src.coinbase.main_app import MainApp

load_dotenv()

if __name__ == "__main__":
    kafka_host = os.getenv("KAFKA_HOST")
    kafka_port = os.getenv("KAFKA_PORT")
    topic = "coinbase"
    ws_url = "wss://ws-feed.exchange.coinbase.com"

    app = MainApp(kafka_host, kafka_port, topic, ws_url)
    app.run()
