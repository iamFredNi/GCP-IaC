import pytest
from unittest.mock import MagicMock, patch
from src.kafka_utility.kafka_manager import KafkaManager
from src.gdelt.file_manager import FileManager
from src.gdelt.gdelt_processor import GdeltProcessor
from src.gdelt.main_app import MainApp


@pytest.fixture
def kafka_manager_mock():
    """Mock for KafkaManager."""
    return MagicMock(spec=KafkaManager)


@pytest.fixture
def file_manager_mock():
    """Mock for FileManager."""
    return MagicMock(spec=FileManager)


@pytest.fixture
def gdelt_processor_mock():
    """Mock for GdeltProcessor."""
    return MagicMock(spec=GdeltProcessor)


@pytest.fixture
def main_app(kafka_manager_mock, file_manager_mock, gdelt_processor_mock):
    """Fixture for MainApp with mocked dependencies."""
    with patch("src.gdelt.main_app.KafkaManager", return_value=kafka_manager_mock), \
         patch("src.gdelt.main_app.FileManager", return_value=file_manager_mock), \
         patch("src.gdelt.main_app.GdeltProcessor", return_value=gdelt_processor_mock):
        app = MainApp(
            kafka_host="localhost",
            kafka_port=9092,
            topic="gdelt-topic",
            url_lastupdate="http://example.com/lastupdate",
            destination_folder="/tmp/test_folder",
        )
        return app



def test_main_app_initialization(main_app, kafka_manager_mock, file_manager_mock, gdelt_processor_mock):
    """Test that MainApp initializes its components correctly."""

    assert main_app.kafka_manager == kafka_manager_mock
    assert main_app.file_manager == file_manager_mock
    assert main_app.gdelt_processor == gdelt_processor_mock


@patch("requests.get")
def test_main_app_run(mock_get, main_app, file_manager_mock, gdelt_processor_mock):
    """Test the run method of MainApp."""
    
    # Mock __download_last_updates
    mock_get.return_value.status_code = 200
    mock_get.return_value.text = "http://example.com/file1.export.CSV.zip\nhttp://example.com/file2.gkg.csv.zip"

    main_app.run()

    # Check that the GDELT processor's run method was called
    gdelt_processor_mock.run.assert_called_once()

    # Check that the file manager's clean_up method was called
    file_manager_mock.clean_up.assert_called_once()

    # Check the final print statement
    with patch("builtins.print") as mock_print:
        main_app.run()
        mock_print.assert_called_with("Pipeline GDELT terminé et fichiers supprimés.")



def test_main_app_dependency_initialization():
    """Test that dependencies are initialized with the correct parameters."""
    with patch("src.gdelt.main_app.KafkaManager") as MockKafkaManager, \
         patch("src.gdelt.main_app.FileManager") as MockFileManager, \
         patch("src.gdelt.main_app.GdeltProcessor") as MockGdeltProcessor:

        app = MainApp(
            kafka_host="localhost",
            kafka_port=9092,
            topic="gdelt-topic",
            url_lastupdate="http://example.com/lastupdate",
            destination_folder="/tmp/test_folder",
        )

        # Verify KafkaManager was initialized correctly
        MockKafkaManager.assert_called_once_with("gdelt-topic", "localhost", 9092)

        # Verify FileManager was initialized correctly
        MockFileManager.assert_called_once_with("/tmp/test_folder")

        # Verify GdeltProcessor was initialized correctly
        MockGdeltProcessor.assert_called_once_with(
            MockKafkaManager.return_value, "http://example.com/lastupdate", "/tmp/test_folder"
        )

