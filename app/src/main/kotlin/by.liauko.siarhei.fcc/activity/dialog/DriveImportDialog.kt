package by.liauko.siarhei.fcc.activity.dialog

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.liauko.siarhei.fcc.R
import by.liauko.siarhei.fcc.backup.BackupUtil
import by.liauko.siarhei.fcc.drive.DriveServiceHelper
import by.liauko.siarhei.fcc.recyclerview.RecyclerViewImportFileAdapter

class DriveImportDialog(
    private val appContext: Context,
    private val driveServiceHelper: DriveServiceHelper,
    private val files: ArrayList<Pair<String, String>>
) : AlertDialog(appContext) {

    private lateinit var rvAdapter: RecyclerViewImportFileAdapter
    private lateinit var noFileTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_export)
        initRecyclerView()
        findViewById<Button>(R.id.dialog_import_negative_button)!!.setOnClickListener { dismiss() }
    }

    private fun initRecyclerView() {
        noFileTextView = findViewById(R.id.no_files_for_import)!!

        rvAdapter = RecyclerViewImportFileAdapter(appContext, files, noFileTextView, driveServiceHelper,
            object: RecyclerViewImportFileAdapter.RecyclerViewOnItemClickListener {
                override fun onItemClick(item: Pair<String, String>) {
                    dismiss()
                    BackupUtil.importDataFromDrive(
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