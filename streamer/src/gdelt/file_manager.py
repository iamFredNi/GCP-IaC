import os
import requests
import zipfile
import glob


class FileManager:
    """
    La classe FileManager est responsable de la gestion des fichiers GDELT.
    Elle est initialisée avec un dossier de destination, télécharge des fichiers
    à partir d'une URL donnée, et fournit des méthodes pour obtenir les fichiers
    CSV à jour de GDELT indexés dans le fichier principal et nettoyer le dossier
    de destination.

    Attributs:

        destination_folder (str): Le dossier où les fichiers téléchargés sont stockés.

    Méthodes:

        download_file(url): Télécharge le fichier à partir de l'URL donnée, le stocke dans le dossier de destination, et le décompresse s'il s'agit d'un fichier zip.
        get_csv_files(): Recherche et retourne tous les fichiers CSV dans le dossier de destination.
        clean_up(): Supprime tous les fichiers dans le dossier de destination.

    """

    def __init__(self, destination_folder):
        self.destination_folder = destination_folder
        os.makedirs(self.destination_folder, exist_ok=True)

    def download_file(self, url):
        """
        Télécharge le fichier de l'adresse donnée. Le stocke dans self.destination_folder.
        Si le fichier est un fichier zip, le dezip au même endroit.
        """
        local_filename = os.path.join(self.destination_folder, url.split("/")[-1])

        # Téléchargement du fichier
        with requests.get(url, stream=True) as r:
            if r.status_code == 200:
                with open(local_filename, "wb") as f:
                    for chunk in r.iter_content(chunk_size=8192):
                        f.write(chunk)
                print(f"Fichier téléchargé : {local_filename}")

                # Si c'est un fichier ZIP, le décompresser
                if local_filename.endswith(".zip"):
                    with zipfile.ZipFile(local_filename, "r") as zip_ref:
                        zip_ref.extractall(self.destination_folder)
                    print(f"Décompressé : {local_filename}")
                os.chmod(f"{local_filename}", 0o644)
            else:
                print(f"Erreur lors du téléchargement de {url} : {r.status_code}")

    def get_csv_files(self):
        """
        Cherche et retourne tous les fichiers CSV dans le dossier renseigné à
        la création de l'objet
        """
        # Cherche tous les fichiers CSV dans le dossier avec .csv et .CSV
        csv_files = glob.glob(
            os.path.join(self.destination_folder, "*.csv")
        ) + glob.glob(os.path.join(self.destination_folder, "*.CSV"))

        # Renommer tous les fichiers .CSV en .csv
        for file_path in csv_files:
            if file_path.endswith(".CSV"):
                new_file_path = file_path[:-4] + ".csv"  # Remplace .CSV par .csv
                os.rename(file_path, new_file_path)
                print(f"Renommé : {file_path} -> {new_file_path}")

        # Met à jour la liste des fichiers pour ne garder que les .csv
        csv_files = glob.glob(os.path.join(self.destination_folder, "*.csv"))

        return csv_files

    def clean_up(self):
        """Supprime tous les fichiers dans le dossier de destination"""
        files = glob.glob(os.path.join(self.destination_folder, "*"))
        for f in files:
            os.remove(f)
        print("Tous les fichiers du dossier ont été supprimés.")
