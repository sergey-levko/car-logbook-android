package by.liauko.siarhei.cl.recyclerview.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.drive.DriveFileInfoList
import by.liauko.siarhei.cl.drive.DriveServiceHelper
import by.liauko.siarhei.cl.recyclerview.holder.RecyclerViewImportFileViewHolder
import by.liauko.siarhei.cl.util.ApplicationUtil

class RecyclerViewImportFileAdapter(
    private val context: Context,
    private val dataSet: DriveFileInfoList,
    private val noFileTextView: TextView,
    private val driveServiceHelper: DriveServiceHelper,
    private val listener: RecyclerViewOnItemClickListener
) : RecyclerView.Adapter<RecyclerViewImportFileViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewImportFileViewHolder {
        if (dataSet.isEmpty()) {
            showNoFileText()
        } else {
            hideNoFileText()
            dataSet.sortByDescending { it.first }
        }

        return RecyclerViewImportFileViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.recycler_view_item_import_file,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerViewImportFileViewHolder, position: Int) {
        holder.fileName.text = dataSet[position].first
        holder.removeButton.setOnClickListener {
            val progressDialog = ApplicationUtil.createProgressDialog(
                context,
                R.string.dialog_progress_delete_file
            )
            progressDialog.show()
            driveServiceHelper.deleteFile(dataSet[position].second)
                .addOnCompleteListener {
                    removeItem(position)
                    progressDialog.dismiss()
                }
        }
        holder.bind(dataSet[position], listener)
    }

    override fun getItemCount() = dataSet.size

    private fun showNoFileText() {
        noFileTextView.visibility = View.VISIBLE
    }

    private fun hideNoFileText() {
        noFileTextView.visibility = View.GONE
    }

    private fun removeItem(position: Int) {
        dataSet.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, dataSet.size)
        if (dataSet.isEmpty()) showNoFileText()
    }

    interface RecyclerViewOnItemClickListener {
        fun onItemClick(item: Pair<String, String>)
    }
}