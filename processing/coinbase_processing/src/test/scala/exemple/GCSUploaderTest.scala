package test.scala.exemple

import org.scalatest.funsuite.AnyFunSuite
import org.mockito.Mockito._
import com.google.cloud.storage.{BlobId, BlobInfo, Storage}
import main.scala.exemple.GCSUploader
import main.scala.exemple.Main.getClass

import java.io.{File, InputStream}
import java.nio.charset.StandardCharsets
import java.nio.file.Files

class GCSUploaderTest extends AnyFunSuite {

  test("upload should successfully send data to GCS") {
    // Créez un mock pour la classe Storage
    val mockStorage = mock(classOf[Storage])

    val resourceStream: InputStream = getClass.getClassLoader.getResourceAsStream("mmsdtd-5a84c3067a4a.json")
    require(resourceStream != null, "Le fichier JSON est introuvable dans resources.")

    // Écrire la ressource dans un fichier temporaire
    val tempFile = File.createTempFile("gcp-keyfile", ".json")
    Files.copy(resourceStream, tempFile.toPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
    tempFile.deleteOnExit()

    // Utilisez Reflection pour modifier le champ `storage` de l'instance de GCSUploader
    val uploader = new GCSUploader("test-bucket", "test-project", tempFile.getAbsolutePath)
    val storageField = classOf[GCSUploader].getDeclaredField("storage")
    storageField.setAccessible(true)
    storageField.set(uploader, mockStorage)

    // Préparez les données du test
    val data = "test data"
    val fileName = "test.json"
    val contentType = "application/json"

    // Exécutez la méthode upload
    uploader.upload(data, fileName, contentType)

    // Vérifiez que `storage.create` a bien été appelé avec les bons arguments
    verify(mockStorage).create(
      BlobInfo.newBuilder(BlobId.of("test-bucket", fileName)).setContentType(contentType).build(),
      data.getBytes(StandardCharsets.UTF_8)
    )
  }
}
