package by.liauko.siarhei.fcc.drive

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.http.ByteArrayContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import java.io.IOException
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class DriveServiceHelper(private val mDriveService: Drive) {

    private val mExecutor = Executors.newSingleThreadExecutor()

    /**
     * Creates a JSON file in the user's My Drive folder and returns its file ID.
     *
     * @param folderId ID of the folder where JSON file will be created
     * @param title name of the file
     * @param data data which will be stored in JSON file
     *
     * @return instance of Task class
     */
    fun createFile(folderId: String, title: String, data: String): Task<String> {
        return Tasks.call(mExecutor, Callable {
            val metadata = File()
                .setParents(listOf(folderId))
                .setMimeType(DriveMimeTypes.TYPE_JSON_FILE.mimeType)
                .setName(title)
            val content = ByteArrayContent.fromString(DriveMimeTypes.TYPE_JSON_FILE.mimeType, data)

            val googleFile = mDriveService.files().create(metadata, content).execute()
                ?: throw IOException("Null result when requesting file creation.")

            googleFile.id
        })
    }

    /**
     * Creates a folder if it not already exists in the user's My Drive folder and returns its ID.
     *
     * @param name name of the folder which will be created
     *
     * @return instance of Task class
     */
    fun createFolderIfNotExist(name: String): Task<String> {
        return Tasks.call(mExecutor, Callable {
            val fileList = mDriveService.files().list()
                .setQ("mimeType = '${DriveMimeTypes.TYPE_GOOGLE_DRIVE_FOLDER.mimeType}' and name = '$name'")
                .setSpaces("drive")
                .execute()

            if (fileList.files.isNotEmpty()) {
                Log.e("FOUND", fileList.files[0].id)
                return@Callable fileList.files[0].id
            }

            val metadata = File()
                .setParents(listOf("root"))
                .setMimeType(DriveMimeTypes.TYPE_GOOGLE_DRIVE_FOLDER.mimeType)
                .setName(name)

            val googleFile = mDriveService.files().create(metadata).execute()
                ?: throw IOException("Null result when requesting folder creation.")

            Log.e("CREATED", googleFile.id)
            googleFile.id
        })
    }
}