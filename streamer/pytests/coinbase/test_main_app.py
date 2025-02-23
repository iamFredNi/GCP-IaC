import pytest
from unittest.mock import patch, MagicMock
from streamer.src.coinbase.main_app import MainApp


@pytest.fixture
def main_app():
    """Fixture to create a MainApp instance with mocked dependencies."""
    with patch("streamer.src.coinbase.main_app.KafkaManager") as MockKafkaManager, \
         patch("streamer.src.coinbase.main_app.WebSocketManager") as MockWebSocketManager:
        mock_kafka_manager = MagicMock()
        mock_ws_manager = MagicMock()
        MockKafkaManager.return_value = mock_kafka_manager
        MockWebSocketManager.return_value = mock_ws_manager

        app = MainApp("localhost", 9092, "test-topic", "ws://test-url")
        return app, mock_kafka_manager, mock_ws_manager


def test_main_app_initialization(main_app):
    """Test if MainApp initializes KafkaManager and WebSocketManager correctly."""
    
    app, mock_kafka_manager, mock_ws_manager = main_app

    # Assert that KafkaManager is initialized
    assert(app.kafka_manager == mock_kafka_manager)
    assert(app.ws_manager == mock_ws_manager)


def test_main_app_run(main_app):
    """Test the run method of MainApp."""
    
    app, _, mock_ws_manager = main_app
    app.run()
    
    # Assert that WebSocketManager's run method is called
    mock_ws_manager.run.assert_called_once()
