package by.liauko.siarhei.cl.recyclerview

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import by.liauko.siarhei.cl.R

class RecyclerViewImportFileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val fileName: TextView = itemView.findViewById(R.id.file_name_text_view)
    val removeButton: ImageButton = itemView.findViewById(R.id.remove_file_button)

    fun bind(item: Pair<String, String>, listener: RecyclerViewImportFileAdapter.RecyclerViewOnItemClickListener) {
        itemView.setOnClickListener { listener.onItemClick(item) }
    }
}