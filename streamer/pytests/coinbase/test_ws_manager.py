import pytest
from unittest.mock import Mock, patch
import json
from datetime import datetime
from src.coinbase.ws_manager import WebSocketManager


@pytest.fixture
def mock_kafka_manager():
    """Fixture to provide a mock KafkaManager."""
    return Mock()


@pytest.fixture
def websocket_manager(mock_kafka_manager):
    """Fixture to provide a WebSocketManager instance."""
    return WebSocketManager(url="wss://fake-url.com", kafka_manager=mock_kafka_manager)


def test_initialization(mock_kafka_manager):
    """Test WebSocketManager initialization."""
    manager = WebSocketManager(url="wss://fake-url.com", kafka_manager=mock_kafka_manager)
    assert manager.url == "wss://fake-url.com"
    assert manager.kafka_manager == mock_kafka_manager
    assert manager.ws is None


@patch("websocket.WebSocketApp")
def test_on_message_valid_ticker(mock_websocket_app, websocket_manager, mock_kafka_manager):
    """Test on_message callback with a valid ticker message."""

    timestamp = datetime.now().strftime("%Y%m%d%H%M")
    mock_message = json.dumps({"type": "ticker", "price": "12345"})
    websocket_manager.on_message(mock_websocket_app, mock_message)
    mock_kafka_manager.send_to_kafka.assert_called_once_with(
        mock_message, f"{timestamp}.json".encode("utf-8")
    )


@patch("websocket.WebSocketApp")
def test_on_message_invalid_json(websocket_manager, mock_kafka_manager):
    """Test on_message callback with invalid JSON."""

    invalid_message = "not a json"
    websocket_manager.on_message(None, invalid_message)
    mock_kafka_manager.send_to_kafka.assert_not_called()


@patch("websocket.WebSocketApp")
def test_on_message_no_ticker_type(websocket_manager, mock_kafka_manager):
    """Test on_message callback with non ticker message."""

    mock_message = json.dumps({"type": "not_ticker"})
    websocket_manager.on_message(None, mock_message)
    mock_kafka_manager.send_to_kafka.assert_not_called()


@patch("websocket.WebSocketApp")
def test_on_error(mock_websocket_app, websocket_manager, capsys):
    """Test on_error callback."""

    mock_error = "Test error"
    websocket_manager.on_error(mock_websocket_app, mock_error)
    captured = capsys.readouterr()
    assert "Erreur WebSocket :" in captured.out
    assert mock_error in captured.out


@patch("websocket.WebSocketApp")
def test_on_close(mock_websocket_app, websocket_manager, capsys, mock_kafka_manager):
    """Test on_close callback."""

    websocket_manager.on_close(mock_websocket_app, 1000, "Test close message")
    mock_kafka_manager.flush.assert_called_once()
    captured = capsys.readouterr()
    assert "Connexion WebSocket fermée :" in captured.out
    assert "1000" in captured.out
    assert "Test close message" in captured.out


@patch("websocket.WebSocketApp")
def test_on_open(mock_websocket_app, websocket_manager, capsys):
    """Test on_open callback."""

    websocket_manager.on_open(mock_websocket_app)
    subscription_message = {
        "type": "subscribe",
        "channels": [{"name": "ticker", "product_ids": ["BTC-USD","ETH-USD","BTC-EUR","USDT-USD","XRP-USD","SOL-USD","DOGE-USD","USDC-USD","ADA-USD","AVAX-USD"]}],
    }
    mock_websocket_app.send.assert_called_once_with(json.dumps(subscription_message))
    captured = capsys.readouterr()
    assert "Abonné au flux de Coinbase pour BTC-USD" in captured.out


@patch("websocket.WebSocketApp")
def test_run(mock_websocket_app, websocket_manager):
    """Test the run method."""

    websocket_manager.run()
    mock_websocket_app.assert_called_once_with(
        "wss://fake-url.com",
        on_message=websocket_manager.on_message,
        on_error=websocket_manager.on_error,
        on_close=websocket_manager.on_close,
    )
    assert websocket_manager.ws.on_open == websocket_manager.on_open
    websocket_manager.ws.run_forever.assert_called_once()
