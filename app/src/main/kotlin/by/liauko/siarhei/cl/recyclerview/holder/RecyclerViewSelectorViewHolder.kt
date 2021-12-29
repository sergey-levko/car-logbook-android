package by.liauko.siarhei.cl.recyclerview.holder

import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.recyclerview.adapter.RecyclerViewSelectorAdapter

class RecyclerViewSelectorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val text: Button = itemView.findViewById(R.id.item_text_view)

    fun bind(item: String, listener: RecyclerViewSelectorAdapter.RecyclerViewOnItemClickListener) {
        itemView.setOnClickListener { listener.onItemClick(item) }
    }
}
