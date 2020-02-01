package by.liauko.siarhei.fcc.drive

import by.liauko.siarhei.fcc.backup.BackupEntity
import by.liauko.siarhei.fcc.backup.BackupService.DRIVE_ROOT_FOLDER_ID
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
                .setMimeType(DriveMimeTypes.TYPE_JSON_FILE)
                .setName(title)
            val content = ByteArrayContent.fromString(DriveMimeTypes.TYPE_JSON_FILE, data)

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
                .setMimeType(DriveMimeTypes.TYPE_GOOGLE_DRIVE_FOLDER)
                .setName(name)

            val googleFile = mDriveService.files().create(metadata).execute()
                ?: throw IOException("Null result when requesting folder creation.")

            googleFile.id
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
            if (fileList.isNotEmpty()) fileList[0].id else DRIVE_ROOT_FOLDER_ID
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
    fun getAllFilesInFolder(folderId: String): Task<DriveFileInfoList> {
        return Tasks.call(mExecutor, Callable {
            val filesData = DriveFileInfoList()
            if (folderId != DRIVE_ROOT_FOLDER_ID) {
                val files = mDriveService.files().list()
                    .setQ("mimeType = '${DriveMimeTypes.TYPE_JSON_FILE}' and '$folderId' in parents")
                    .setSpaces("drive")
                    .execute()
                    .files

                for (file in files) {
                    filesData.add(Pair(file.name, file.id))
                }
            }

            filesData
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
                    Gson().fromJson<BackupEntity>(it.readLine(), BackupEntity::class.java)
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
            mDriveService.files().delete(id).execute()
        })
    }

    private fun findFolderByName(name: String) = mDriveService.files().list()
        .setQ("mimeType = '${DriveMimeTypes.TYPE_GOOGLE_DRIVE_FOLDER}' and name = '$name'")
        .setSpaces("drive")
        .execute()
        .files
}

typealias DriveFileInfoList = ArrayList<Pair<String, String>>