package main.scala.exemple

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.{BlobId, BlobInfo, Storage, StorageOptions}

import java.io.FileInputStream
import java.nio.charset.StandardCharsets

class GCSUploader(bucketName: String, projectId: String, credentialsPath: String) extends Loggable {
  private val storage: Storage = StorageOptions.newBuilder()
    .setProjectId(projectId)
    .setCredentials(GoogleCredentials.fromStream(new FileInputStream(credentialsPath)))
    .build()
    .getService()

  def upload(data: String, fileName: String, contentType: String): Unit = {
    try {
      val blobId = BlobId.of(bucketName, fileName)
      val blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build()
      storage.create(blobInfo, data.getBytes(StandardCharsets.UTF_8))
      log(s"\t - Fichier envoyé dans GCS : $fileName")
    } catch {
      case e: Exception =>
        logError(s"Erreur lors de l'envoi du fichier $fileName dans GCS: ${e.getMessage}")
    }
  }
}
