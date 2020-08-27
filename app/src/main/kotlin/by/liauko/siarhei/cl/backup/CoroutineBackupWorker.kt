package by.liauko.siarhei.cl.backup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.drive.DriveServiceHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes

class CoroutineBackupWorker(private val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            val driverServiceHelper = initDriveServiceHelper()
            BackupService.exportToDrive(context, driverServiceHelper)
        } catch (e: Exception) {
            return Result.retry()
        }

        return Result.success()
    }

    private fun initDriveServiceHelper(): DriveServiceHelper {
        val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context)
        val credential = GoogleAccountCredential.usingOAuth2(context, listOf(DriveScopes.DRIVE))
        credential.selectedAccount = googleSignInAccount?.account
        val googleDriveService = Drive.Builder(
            NetHttpTransport(),
            GsonFactory(),
            credential
        ).setApplicationName(context.getString(R.string.app_name)).build()

        return DriveServiceHelper(googleDriveService)
    }
}
