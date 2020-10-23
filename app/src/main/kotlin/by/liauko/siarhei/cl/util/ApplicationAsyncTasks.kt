package by.liauko.siarhei.cl.util

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.documentfile.provider.DocumentFile
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.activity.dialog.ProgressDialog
import by.liauko.siarhei.cl.backup.BackupEntity
import by.liauko.siarhei.cl.backup.BackupService
import by.liauko.siarhei.cl.database.CarLogbookDatabase
import by.liauko.siarhei.cl.entity.CarProfileData
import by.liauko.siarhei.cl.entity.LogData
import by.liauko.siarhei.cl.util.ApplicationUtil.EMPTY_STRING
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Perform writing and saving log data to PDF file asynchronously
 */
class ExportToPdfAsyncTask(
    private val context: Context,
    private val directoryUri: Uri,
    private val data: List<LogData>,
    private val carData: CarProfileData
) : AsyncTask<Unit, Unit, Unit>() {

    private val a4Width = 2480
    private val a4Height = 3508
    private val space = " "

    private lateinit var progressDialog: ProgressDialog

    override fun onPreExecute() {
        super.onPreExecute()

        progressDialog = ApplicationUtil.createProgressDialog(
            context,
            R.string.dialog_backup_progress_export_text
        )
        progressDialog.show()
    }

    override fun doInBackground(vararg params: Unit?) {
        val bodyTypes = context.resources.getStringArray(R.array.body_types)
        val fuelTypes = context.resources.getStringArray(R.array.fuel_type)

        val document = PdfDocument()
        val page = document.startPage(PdfDocument.PageInfo.Builder(a4Width, a4Height, 1).create())
        val canvas = page.canvas

        // Print document title
        val titlePaint = createPaint(80f, Paint.Align.CENTER)
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(carData.name, a4Width / 2f, 150f, titlePaint)

        // Print information about car
        val engineVolume = if (carData.engineVolume != null) carData.engineVolume.toString() + space else EMPTY_STRING
        val carInfoText = context.getString(R.string.log_export_car_body_text) + space +
                bodyTypes[carData.bodyType.ordinal] + "," + space +
                context.getString(R.string.log_export_engine_text) + space +
                engineVolume +
                fuelTypes[carData.fuelType.ordinal]
        canvas.drawText(carInfoText, a4Width/2f, 240f, createPaint(60f, Paint.Align.CENTER))

        // Print table header or notification that nothing to show
        val finalPage: PdfDocument.Page
        if (data.isNotEmpty()) {
            finalPage = printLogData(document, page)
        } else {
            finalPage = page
            canvas.drawText(context.getString(R.string.log_export_no_entries), a4Width / 2f, 370f, createPaint(50f, Paint.Align.CENTER))
        }

        document.finishPage(finalPage)

        // Save data to file
        val file = DocumentFile.fromTreeUri(context, directoryUri)?.createFile(
            MimeTypes.TYPE_PDF_FILE,
            "${carData.name.replace(" ", "-")}-log-data-${SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).format(Date())}"
        )
        if (file?.uri != null) {
            context.contentResolver.openOutputStream(file.uri)?.use {
                document.writeTo(it)
            }
        }
    }

    override fun onPostExecute(result: Unit?) {
        super.onPostExecute(result)

        progressDialog.dismiss()
        ApplicationUtil.createAlertDialog(
            context,
            R.string.dialog_backup_alert_title_success,
            R.string.dialog_backup_alert_export_success
        ).show()
    }

    private fun printLogData(document: PdfDocument, page: PdfDocument.Page): PdfDocument.Page {
        var resultPage = page
        var canvas = page.canvas
        var pageNumber = 1

        drawTableHeader(canvas, 300f, 400f)

        // Prepare style for log data text
        val textPaint = createPaint(50f, Paint.Align.LEFT)

        // Prepare style for long text
        val longTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        longTextPaint.textSize = 50f

        // Print log data
        var previousPosition = 450f
        for (entity in data) {
            val text = entity.title + if (entity.text?.isNotBlank() != false) "\n" + entity.text else EMPTY_STRING
            val staticLayout = createStaticLayout(text, longTextPaint)
            var yValue = previousPosition + 50
            previousPosition += staticLayout.height + 50

            // Check if page is end. If true, start new page
            if (yValue > a4Height - 100 || previousPosition > a4Height - 100) {
                document.finishPage(resultPage)
                pageNumber++
                resultPage = document.startPage(PdfDocument.PageInfo.Builder(a4Width, a4Height, pageNumber).create())
                canvas = resultPage.canvas
                drawTableHeader(canvas, 100f, 200f)
                previousPosition = 300f + staticLayout.height
                yValue = 300f
            }

            val date = Date(entity.time)
            canvas.drawText(SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date), 60f, yValue, textPaint)
            canvas.drawText(entity.mileage.toString(), 360f, yValue, textPaint)

            canvas.save()
            canvas.translate(660f, yValue - 50)
            staticLayout.draw(canvas)
            canvas.restore()
        }

        return resultPage
    }

    private fun drawTableHeader(canvas: Canvas, top: Float, bottom: Float) {
        val tablePaint = Paint()
        tablePaint.style = Paint.Style.STROKE
        tablePaint.strokeWidth = 2f
        canvas.drawRect(40f, top, a4Width-40f, bottom, tablePaint)

        val tableHeaderPaint = createPaint(50f, Paint.Align.LEFT)
        canvas.drawText(context.getString(R.string.log_export_created_date_label), 60f, bottom - 30, tableHeaderPaint)
        canvas.drawText(context.getString(R.string.log_export_mileage_label), 360f, bottom - 30, tableHeaderPaint)
        canvas.drawText(context.getString(R.string.log_export_description_label), 660f, bottom - 30, tableHeaderPaint)

        canvas.drawLine(340f, top + 10, 340f, bottom - 10, tableHeaderPaint)
        canvas.drawLine(640f, top + 10, 640f, bottom - 10, tableHeaderPaint)
    }

    @Suppress("deprecation")
    private fun createStaticLayout(text: String, textPaint: TextPaint) =
        // StaticLayout.Builder class was added in API level 23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, textPaint, a4Width - 720)
                .setIncludePad(false)
                .build()
        } else {
            StaticLayout(
                text,
                textPaint,
                a4Width - 720,
                Layout.Alignment.ALIGN_NORMAL,
                1f,
                0f,
                false
            )
        }

    private fun createPaint(textSize: Float, textAlign: Paint.Align): Paint {
        val textPaint = Paint()
        textPaint.textSize = textSize
        textPaint.textAlign = textAlign

        return textPaint
    }
}

/**
 * Perform writing and saving application data to .clbdata file asynchronously
 */
class ExportToFileAsyncTask(
    private val directoryUri: Uri,
    private val context: Context,
    private val backUpData: BackupEntity,
    private val progressDialog: ProgressDialog
) : AsyncTask<Unit, Unit, Unit>() {

    override fun doInBackground(vararg params: Unit?) {
        val file = DocumentFile.fromTreeUri(context, directoryUri)?.createFile(
            MimeTypes.TYPE_CLBDATA_FILE,
            "car-logbook-${SimpleDateFormat(
                "yyyy-MM-dd-HH-mm",
                Locale.getDefault()
            ).format(Date())}.clbdata"
        )
        if (file?.uri != null) {
            context.contentResolver.openOutputStream(file.uri)?.use {
                it.write(Gson().toJson(backUpData).toByteArray())
                it.flush()
            }
        }
    }

    override fun onPostExecute(result: Unit?) {
        super.onPostExecute(result)

        progressDialog.dismiss()
        ApplicationUtil.createAlertDialog(
            context,
            R.string.dialog_backup_alert_title_success,
            R.string.dialog_backup_alert_export_success
        ).show()
    }
}

/**
 * Perform reading and restoring application data from .clbdata file asynchronously
 */
class ImportFromFileAsyncTask(
    private val fileUri: Uri,
    private val context: Context,
    private val activity: Activity?
) : AsyncTask<Unit, Unit, Unit>() {

    private val emptyJsonObject = "{}"

    private lateinit var progressDialog: ProgressDialog

    override fun onPreExecute() {
        super.onPreExecute()

        progressDialog = ApplicationUtil.createProgressDialog(
            context,
            R.string.dialog_backup_progress_import_text
        )
        progressDialog.show()
    }

    override fun doInBackground(vararg params: Unit?) {
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

    override fun onPostExecute(result: Unit?) {
        super.onPostExecute(result)

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
