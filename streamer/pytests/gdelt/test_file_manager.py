import pytest
import os
from unittest.mock import patch, MagicMock
from src.gdelt.file_manager import FileManager


@pytest.fixture
def temp_folder(tmp_path):
    """Fixture to create a temporary directory for testing."""
    return str(tmp_path)


def test_init(temp_folder):
    """Test that the destination folder is created during initialization."""
    
    fm = FileManager(temp_folder)
    assert os.path.exists(fm.destination_folder)
    assert os.path.isdir(fm.destination_folder)


@patch("requests.get")
@patch("zipfile.ZipFile")
def test_download_file(mock_zipfile, mock_get, temp_folder):
    """Test the download_file method with a mocked request."""
    
    fm = FileManager(temp_folder)
    url = "http://example.com/test.zip"

    # Mock the response object for requests.get
    mock_response = MagicMock()
    mock_response.status_code = 200
    mock_response.iter_content = lambda chunk_size: [b"dummy data"]
    mock_response.__enter__.return_value = mock_response
    mock_response.__exit__.return_value = None
    mock_get.return_value = mock_response

    # Mock the zipfile.ZipFile behavior
    mock_zip = MagicMock()
    mock_zip.__enter__.return_value = mock_zip
    mock_zip.__exit__.return_value = None
    mock_zipfile.return_value = mock_zip
    
    fm.download_file(url)

    # Verify the file was downloaded
    local_filename = os.path.join(temp_folder, "test.zip")
    assert os.path.exists(local_filename), f"File {local_filename} not found"

    # Verify zipfile was extracted
    if local_filename.endswith(".zip"):
        mock_zipfile.assert_called_once_with(local_filename, 'r')
        mock_zip.extractall.assert_called_once_with(temp_folder)


@patch("requests.get")
def test_download_file_failure(mock_get, temp_folder):
    """Test download_file handles failed downloads correctly."""
    
    fm = FileManager(temp_folder)
    url = "http://example.com/nonexistent.zip"

    # Mock a failed response
    mock_response = MagicMock()
    mock_response.status_code = 404
    mock_get.return_value = mock_response

    fm.download_file(url)

    # Verify no file is created
    local_filename = os.path.join(temp_folder, "nonexistent.zip")
    assert not os.path.exists(local_filename)


def test_get_csv_files(temp_folder):
    """Test that get_csv_files method retrieves all CSV files."""
    
    fm = FileManager(temp_folder)

    # Create mock CSV files
    csv_files = [os.path.join(temp_folder, f"file{i}.csv") for i in range(3)]
    for csv_file in csv_files:
        with open(csv_file, "w") as f:
            f.write("test data")

    retrieved_csv_files = fm.get_csv_files()
    assert set(retrieved_csv_files) == set(csv_files)


def test_get_csv_files_rename_csv(temp_folder):
    """Test that get_csv_files renames .CSV files to .csv."""
    
    fm = FileManager(temp_folder)

    # Create mock .CSV files
    csv_files = [os.path.join(temp_folder, f"file{i}.CSV") for i in range(3)]
    for csv_file in csv_files:
        with open(csv_file, "w") as f:
            f.write("test data")

    retrieved_csv_files = fm.get_csv_files()

    # Verify all .CSV files were renamed to .csv
    renamed_files = [file.replace(".CSV", ".csv") for file in csv_files]
    assert set(retrieved_csv_files) == set(renamed_files)
    for renamed_file in renamed_files:
        assert os.path.exists(renamed_file)


def test_clean_up(temp_folder):
    """Test the clean_up method."""
    
    fm = FileManager(temp_folder)

    # Create mock files in the folder
    files = [os.path.join(temp_folder, f"file{i}.txt") for i in range(3)]
    for file in files:
        with open(file, "w") as f:
            f.write("test data")

    # Verify files exist
    assert len(os.listdir(temp_folder)) == 3
    # Clean up and verify all files are removed
    fm.clean_up()
    assert len(os.listdir(temp_folder)) == 0
