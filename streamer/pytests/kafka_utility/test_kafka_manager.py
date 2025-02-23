import pytest
from unittest.mock import MagicMock, patch, ANY
from src.kafka_utility.kafka_manager import KafkaManager


@pytest.fixture
def kafka_manager():
    """Fixture to initialize KafkaManager with mock KafkaProducer."""
    with patch("src.kafka_utility.kafka_manager.KafkaProducer") as MockKafkaProducer:
        mock_producer = MagicMock()
        MockKafkaProducer.return_value = mock_producer
        manager = KafkaManager("test-topic", "localhost", 9092)
        return manager, mock_producer, MockKafkaProducer


def test_kafka_manager_initialization(kafka_manager):
    """Test if KafkaManager initializes with correct attributes."""
    manager, mock_producer, MockKafkaProducer = kafka_manager

    # Assert that the topic is correctly set
    assert manager.topic == "test-topic"

    # Assert that the producer is correctly initialized
    assert manager.producer == mock_producer

    # Verify KafkaProducer is initialized with the correct parameters
    MockKafkaProducer.assert_called_once_with(
        bootstrap_servers="localhost:9092",
        api_version=(0, 11, 5),
        value_serializer=ANY, 
    )


def test_send_to_kafka(kafka_manager):
    """Test the send_to_kafka method."""
    
    manager, mock_producer, _ = kafka_manager
    manager.send_to_kafka("test-message", b"test-key")

    # Verify the producer's send method is called with the correct arguments
    mock_producer.send.assert_called_once_with(
        "test-topic", value="test-message", key=b"test-key"
    )


def test_send_to_kafka_exception_handling(kafka_manager):
    """Test the send_to_kafka method when KafkaProducer.send raises an exception."""
    
    manager, mock_producer, _ = kafka_manager
    mock_producer.send.side_effect = Exception("Kafka send error")

    with pytest.raises(Exception, match="Kafka send error"):
        manager.send_to_kafka("test-message", b"test-key")


def test_flush(kafka_manager):
    """Test the flush method."""
    
    manager, mock_producer, _ = kafka_manager
    manager.flush()

    # Verify the producer's flush method is called
    mock_producer.flush.assert_called_once()


def test_flush_exception_handling(kafka_manager):
    """Test the flush method when KafkaProducer.flush raises an exception."""
    
    manager, mock_producer, _ = kafka_manager
    mock_producer.flush.side_effect = Exception("Kafka flush error")

    with pytest.raises(Exception, match="Kafka flush error"):
        manager.flush()
