package by.liauko.siarhei.cl.activity.dialog

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.backup.BackupService
import by.liauko.siarhei.cl.drive.DriveFileInfoList
import by.liauko.siarhei.cl.drive.DriveServiceHelper
import by.liauko.siarhei.cl.recyclerview.RecyclerViewImportFileAdapter

class DriveImportDialog(
    private val appContext: Context,
    private val driveServiceHelper: DriveServiceHelper,
    private val files: DriveFileInfoList
) : AlertDialog(appContext, R.style.FullScreenDialogDefault) {

    private lateinit var rvAdapter: RecyclerViewImportFileAdapter
    private lateinit var noFileTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_import)
        initRecyclerView()
        findViewById<Button>(R.id.dialog_import_negative_button)!!.setOnClickListener { dismiss() }
    }

    private fun initRecyclerView() {
        noFileTextView = findViewById(R.id.no_files_for_import)!!

        rvAdapter = RecyclerViewImportFileAdapter(appContext, files, noFileTextView, driveServiceHelper,
            object : RecyclerViewImportFileAdapter.RecyclerViewOnItemClickListener {
                override fun onItemClick(item: Pair<String, String>) {
                    dismiss()
                    BackupService.importFromDrive(
                        item.second,
                        context,
                        driveServiceHelper
                    )
                }
            }
        )

        findViewById<RecyclerView>(R.id.import_data_recycler_view)!!.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = rvAdapter
        }
    }
}