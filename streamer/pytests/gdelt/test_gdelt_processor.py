import pytest
from unittest.mock import MagicMock, patch
from src.gdelt.file_manager import FileManager
from src.gdelt.gdelt_processor import GdeltProcessor


@pytest.fixture
def kafka_manager_mock():
    """Fixture to create a mock KafkaManager."""
    
    kafka_manager = MagicMock()
    kafka_manager.send_to_kafka = MagicMock()
    kafka_manager.flush = MagicMock()
    return kafka_manager


@pytest.fixture
def file_manager_mock():
    """Fixture to create a mock FileManager."""
    
    file_manager = MagicMock(spec=FileManager)
    file_manager.get_csv_files = MagicMock(return_value=["file1.csv", "file2.csv"])
    return file_manager


@pytest.fixture
def gdelt_processor(kafka_manager_mock, file_manager_mock):
    """Fixture to create a GdeltProcessor instance with mocked dependencies."""
    
    url_lastupdate = "http://example.com/lastupdate"
    destination_folder = "/tmp/test_folder"
    processor = GdeltProcessor(kafka_manager_mock, url_lastupdate, destination_folder)
    processor.file_manager = file_manager_mock  
    return processor


@patch("requests.get")
def test_download_last_updates_success(mock_get, gdelt_processor):
    """Test __download_last_updates method with valid response."""
    
    mock_get.return_value.status_code = 200
    mock_get.return_value.text = "line1\nline2\nline3"

    lines = gdelt_processor._GdeltProcessor__download_last_updates()
    assert lines == ["line1", "line2", "line3"]
    mock_get.assert_called_once_with(gdelt_processor.url_lastupdate)


@patch("requests.get")
def test_download_last_updates_failure(mock_get, gdelt_processor):
    """Test __download_last_updates method with invalid response."""
    
    mock_get.return_value.status_code = 404

    with pytest.raises(Exception, match="Error while downloading GDELT last updates"):
        gdelt_processor._GdeltProcessor__download_last_updates()
    mock_get.assert_called_once_with(gdelt_processor.url_lastupdate)


def test_download_csv_files_from_lines(gdelt_processor):
    """Test __download_csv_files_from_lines method with valid and invalid lines."""
    
    lines = [
        "irrelevant.line",
        "http://example.com/file1.export.CSV.zip",
        "http://example.com/file2.gkg.csv.zip",
    ]
    gdelt_processor.file_manager.download_file = MagicMock()

    gdelt_processor._GdeltProcessor__download_csv_files_from_lines(lines)
    gdelt_processor.file_manager.download_file.assert_any_call(
        "http://example.com/file1.export.CSV.zip"
    )
    gdelt_processor.file_manager.download_file.assert_any_call(
        "http://example.com/file2.gkg.csv.zip"
    )
    assert gdelt_processor.file_manager.download_file.call_count == 2


def test_send_csv_files_to_kafka(gdelt_processor, kafka_manager_mock, tmp_path):
    """Test __send_csv_files_to_kafka method."""
    
    # Create temporary CSV files
    csv_file1 = tmp_path / "file1.csv"
    csv_file2 = tmp_path / "file2.csv"
    csv_file1.write_text("test content 1")
    csv_file2.write_text("test content 2")
    
    csv_files = [str(csv_file1), str(csv_file2)]
    gdelt_processor._GdeltProcessor__send_csv_files_to_kafka(csv_files)

    kafka_manager_mock.send_to_kafka.assert_any_call("test content 1", b"file1.csv")
    kafka_manager_mock.send_to_kafka.assert_any_call("test content 2", b"file2.csv")
    assert kafka_manager_mock.send_to_kafka.call_count == 2


def test_send_csv_file_to_kafka(gdelt_processor, kafka_manager_mock, tmp_path):
    """Test __send_csv_file_to_kafka method."""

    csv_file = tmp_path / "test.csv"
    csv_file.write_text("test content")

    gdelt_processor._GdeltProcessor__send_csv_file_to_kafka(str(csv_file))

    kafka_manager_mock.send_to_kafka.assert_called_once_with("test content", b"test.csv")


@patch("requests.get")
def test_run(mock_get, gdelt_processor, kafka_manager_mock, file_manager_mock, tmp_path):
    """Test run method."""
    
    # Mock __download_last_updates
    mock_get.return_value.status_code = 200
    mock_get.return_value.text = "http://example.com/file1.export.CSV.zip\nhttp://example.com/file2.gkg.csv.zip"

    # Mock CSV files
    csv_file1 = tmp_path / "file1.csv"
    csv_file2 = tmp_path / "file2.csv"
    csv_file1.write_text("test content 1")
    csv_file2.write_text("test content 2")

    file_manager_mock.get_csv_files.return_value = [str(csv_file1), str(csv_file2)]
    gdelt_processor.run()

    # Assertions
    mock_get.assert_called_once_with(gdelt_processor.url_lastupdate)
    kafka_manager_mock.send_to_kafka.assert_any_call("test content 1", b"file1.csv")
    kafka_manager_mock.send_to_kafka.assert_any_call("test content 2", b"file2.csv")
    assert kafka_manager_mock.send_to_kafka.call_count == 2
    kafka_manager_mock.flush.assert_called_once()
