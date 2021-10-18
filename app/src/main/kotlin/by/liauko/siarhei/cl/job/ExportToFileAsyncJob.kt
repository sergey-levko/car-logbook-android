package by.liauko.siarhei.cl.job

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.activity.dialog.ProgressDialog
import by.liauko.siarhei.cl.backup.BackupEntity
import by.liauko.siarhei.cl.util.ApplicationUtil
import by.liauko.siarhei.cl.util.MimeTypes
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Perform writing and saving application data to .clbdata file asynchronously
 *
 * @author Siarhei Liauko
 * @since 4.3
 */
class ExportToFileAsyncJob(
    private val directoryUri: Uri,
    private val context: Context,
    private val backUpData: BackupEntity,
    private val progressDialog: ProgressDialog
) : AbstractAsyncJob() {

    override suspend fun doInBackground() {
        val file = DocumentFile.fromTreeUri(context, directoryUri)?.createFile(
            MimeTypes.TYPE_CLBDATA_FILE,
            "car-logbook-${
                SimpleDateFormat(
                    "yyyy-MM-dd-HH-mm",
                    Locale.getDefault()
                ).format(Date())
            }.clbdata"
        )

        if (file?.uri != null) {
            writeToFile(file.uri)
        }
    }

    override fun onPostExecute() {
        progressDialog.dismiss()
        ApplicationUtil.createAlertDialog(
            context,
            R.string.dialog_backup_alert_title_success,
            R.string.dialog_backup_alert_export_success
        ).show()
    }

    private fun writeToFile(uri: Uri) = runBlocking {
        context.contentResolver.openOutputStream(uri)?.use {
            it.write(Gson().toJson(backUpData).toByteArray())
            it.flush()
        }
    }
}
