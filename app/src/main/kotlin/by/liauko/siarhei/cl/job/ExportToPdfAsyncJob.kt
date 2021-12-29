package by.liauko.siarhei.cl.job

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.StaticLayout
import android.text.TextPaint
import androidx.appcompat.app.AlertDialog
import androidx.documentfile.provider.DocumentFile
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.model.LogDataModel
import by.liauko.siarhei.cl.repository.CarProfileRepository
import by.liauko.siarhei.cl.repository.LogRepository
import by.liauko.siarhei.cl.util.ApplicationUtil
import by.liauko.siarhei.cl.util.MimeTypes
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Perform writing and saving log data to PDF file asynchronously
 *
 * @author Siarhei Liauko
 * @since 4.3
 */
class ExportToPdfAsyncJob(
    private val context: Context,
    private val directoryUri: Uri
) : AbstractAsyncJob() {

    private val a4Width = 2480
    private val a4Height = 3508
    private val space = " "

    private lateinit var progressDialog: AlertDialog

    override fun onPreExecute() {
        progressDialog = ApplicationUtil.createProgressDialog(
            context,
            R.string.dialog_backup_progress_export_text
        ).show()
    }

    override suspend fun doInBackground() {
        val logData = LogRepository(context).selectAllByProfileId(ApplicationUtil.profileId).sortedBy { it.time }
        val carData = CarProfileRepository(context).selectById(ApplicationUtil.profileId)

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
        val engineVolume = if (carData.engineVolume != null) carData.engineVolume.toString() + space else ApplicationUtil.EMPTY_STRING
        val carInfoText = context.getString(R.string.log_export_car_body_text) + space +
                bodyTypes[carData.bodyType.ordinal] + "," + space +
                context.getString(R.string.log_export_engine_text) + space +
                engineVolume +
                fuelTypes[carData.fuelType.ordinal]
        canvas.drawText(carInfoText, a4Width/2f, 240f, createPaint(60f, Paint.Align.CENTER))

        // Print table header or notification that nothing to show
        val finalPage: PdfDocument.Page
        if (logData.isNotEmpty()) {
            finalPage = printLogData(logData, document, page)
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
            writeToFile(file.uri, document)
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

    private fun printLogData(data: List<LogDataModel>, document: PdfDocument, page: PdfDocument.Page): PdfDocument.Page {
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
            val text = entity.title + if (entity.text?.isNotBlank() != false) "\n" + entity.text else ApplicationUtil.EMPTY_STRING
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

    private fun createStaticLayout(text: String, textPaint: TextPaint) =
        StaticLayout.Builder.obtain(text, 0, text.length, textPaint, a4Width - 720)
            .setIncludePad(false)
            .build()

    private fun createPaint(textSize: Float, textAlign: Paint.Align): Paint {
        val textPaint = Paint()
        textPaint.textSize = textSize
        textPaint.textAlign = textAlign

        return textPaint
    }

    private fun writeToFile(uri: Uri, document: PdfDocument) = runBlocking {
        context.contentResolver.openOutputStream(uri)?.use {
            document.writeTo(it)
        }
    }
}
