package by.liauko.siarhei.fcc.drive

import by.liauko.siarhei.fcc.backup.BackupEntity
import by.liauko.siarhei.fcc.backup.BackupUtil.driveRootFolderId
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.http.ByteArrayContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.gson.Gson
import java.io.IOException
import java.util.concurrent.Callable
import java.util.concurrent.Executors

/**
 * Contains methods for managing files and folders in Google Drive
 */
class DriveServiceHelper(private val mDriveService: Drive) {

    private val mExecutor = Executors.newSingleThreadExecutor()

    /**
     * Creates a JSON file in the user's My Drive folder and returns its file ID.
     *
     * @param folderId ID of the folder where JSON file will be created
     * @param title name of the file
     * @param data data which will be stored in JSON file
     *
     * @return instance of Task class which contains created file ID
     *
     * @author Siarhei Liauko
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

            return@Callable googleFile.id
        })
    }

    /**
     * Creates a folder if it not already exists in the user's My Drive folder and returns its ID.
     *
     * @param name name of the folder which will be created
     *
     * @return instance of Task class which contains folder ID
     *
     * @author Siarhei Liauko
     */
    fun createFolderIfNotExist(name: String): Task<String> {
        return Tasks.call(mExecutor, Callable {
            val fileList = findFolderByName(name)

            if (fileList.isNotEmpty()) {
                return@Callable fileList[0].id
            }

            val metadata = File()
                .setParents(listOf("root"))
                .setMimeType(DriveMimeTypes.TYPE_GOOGLE_DRIVE_FOLDER.mimeType)
                .setName(name)

            val googleFile = mDriveService.files().create(metadata).execute()
                ?: throw IOException("Null result when requesting folder creation.")

            return@Callable googleFile.id
        })
    }

    /**
     * Returns folder ID by its name
     *
     * @param name folder name
     *
     * @return instance of Task class which contains folder ID
     *
     * @author Siarhei Liauko
     */
    fun getFolderIdByName(name: String): Task<String> {
        return Tasks.call(mExecutor, Callable {
            val fileList = findFolderByName(name)
            return@Callable if (fileList.isNotEmpty()) fileList[0].id else driveRootFolderId
        })
    }

    /**
     * Returns file list which have application/json mime type and stored in folder which ID is equal folderId value.
     *
     * @param folderId ID of folder where stored files
     *
     * @return instance of Task class which contains list of pair values: first value is file name, the second - file ID
     *
     * @author Siarhei Liauko
     */
    fun getAllFilesInFolder(folderId: String): Task<ArrayList<Pair<String, String>>> {
        return Tasks.call(mExecutor, Callable {
            val filesData = ArrayList<Pair<String, String>>()
            if (folderId != driveRootFolderId) {
                val files = mDriveService.files().list()
                    .setQ("mimeType = '${DriveMimeTypes.TYPE_JSON_FILE.mimeType}' and '$folderId' in parents")
                    .setSpaces("drive")
                    .execute()
                    .files

                for (file in files) {
                    filesData.add(Pair(file.name, file.id))
                }
            }

            return@Callable filesData
        })
    }

    /**
     * Reads data from JSON file which stored in Google Drive folder
     *
     * @param fileId ID of the file which contains data
     *
     * @return instance of Task class which contains instance of BackupEntity class
     *
     * @author Siarhei Liauko
     */
    fun readBackupFile(fileId: String): Task<BackupEntity> {
        return Tasks.call(mExecutor, Callable {
            mDriveService.files().get(fileId)
                .executeMediaAsInputStream()
                .bufferedReader()
                .use {
                    return@Callable Gson().fromJson<BackupEntity>(it.readLine(), BackupEntity::class.java)
                }
        })
    }

    /**
     * Deletes file from Google Drive storage
     *
     * @param id the file ID which will be deleted
     *
     * @author Siarhei Liauko
     */
    fun deleteFile(id: String): Task<Void> {
        return Tasks.call(mExecutor, Callable {
            return@Callable mDriveService.files().delete(id).execute()
        })
    }

    private fun findFolderByName(name: String) = mDriveService.files().list()
        .setQ("mimeType = '${DriveMimeTypes.TYPE_GOOGLE_DRIVE_FOLDER.mimeType}' and name = '$name'")
        .setSpaces("drive")
        .execute()
        .files
}