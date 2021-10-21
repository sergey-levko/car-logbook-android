package by.liauko.siarhei.cl.activity.dialog

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.backup.BackupService
import by.liauko.siarhei.cl.databinding.DialogImportBinding
import by.liauko.siarhei.cl.drive.DriveFileInfoList
import by.liauko.siarhei.cl.drive.DriveServiceHelper
import by.liauko.siarhei.cl.recyclerview.adapter.RecyclerViewImportFileAdapter

class DriveImportDialog(
    private val appContext: Context,
    private val driveServiceHelper: DriveServiceHelper,
    private val files: DriveFileInfoList,
    private val activity: Activity?
) : AlertDialog(appContext, R.style.Theme_App_FullScreenDialog) {

    private lateinit var viewBinding: DialogImportBinding
    private lateinit var rvAdapter: RecyclerViewImportFileAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DialogImportBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        initRecyclerView()
        viewBinding.dialogImportNegativeButton.setOnClickListener { dismiss() }
    }

    private fun initRecyclerView() {
        rvAdapter =
            RecyclerViewImportFileAdapter(
                appContext,
                files,
                viewBinding.noFilesForImport,
                driveServiceHelper,
                object : RecyclerViewImportFileAdapter.RecyclerViewOnItemClickListener {
                    override fun onItemClick(item: Pair<String, String>) {
                        dismiss()
                        BackupService.importFromDrive(
                            item.second,
                            context,
                            driveServiceHelper,
                            activity
                        )
                    }
                }
            )

        viewBinding.importDataRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = rvAdapter
        }
    }
}
