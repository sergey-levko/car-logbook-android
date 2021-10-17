package by.liauko.siarhei.cl.job

import android.app.Activity
import android.content.Context
import android.net.Uri
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.activity.dialog.ProgressDialog
import by.liauko.siarhei.cl.backup.BackupEntity
import by.liauko.siarhei.cl.backup.BackupService
import by.liauko.siarhei.cl.database.CarLogbookDatabase
import by.liauko.siarhei.cl.util.ApplicationUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson

class ImportFromFileAsyncJob(
    private val fileUri: Uri,
    private val context: Context,
    private val activity: Activity?
) : AbstractAsyncJob() {

    private val emptyJsonObject = "{}"

    private lateinit var progressDialog: ProgressDialog

    override fun onPreExecute() {
        progressDialog = ApplicationUtil.createProgressDialog(
            context,
            R.string.dialog_backup_progress_import_text
        )
        progressDialog.show()
    }

    override fun doInBackground() {
        context.contentResolver.openInputStream(fileUri)?.bufferedReader().use {
            BackupService.restoreData(
                CarLogbookDatabase.invoke(context),
                Gson().fromJson<BackupEntity>(
                    it?.readLine() ?: emptyJsonObject,
                    BackupEntity::class.java
                )
            )
        }
        BackupService.saveProfileValues(context)
    }

    override fun onPostExecute() {
        progressDialog.dismiss()
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.dialog_backup_alert_title_success)
            .setMessage(R.string.dialog_backup_alert_import_success)
            .setPositiveButton(
                context.getString(
                    R.string.dialog_backup_alert_ok_button
                )
            ) { dialog, _ ->
                activity?.setResult(Activity.RESULT_OK)
                activity?.finish()
                dialog.dismiss()
            }
            .create().show()
    }
}
