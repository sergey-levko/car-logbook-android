package by.liauko.siarhei.cl.recyclerview.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.recyclerview.holder.RecyclerViewSelectorViewHolder

class RecyclerViewSelectorAdapter(
    private val context: Context,
    private var value: String,
    private var items: List<String>,
    private val listener: RecyclerViewOnItemClickListener
) : RecyclerView.Adapter<RecyclerViewSelectorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RecyclerViewSelectorViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_view_item_period_selector, parent, false)
        )

    override fun onBindViewHolder(holder: RecyclerViewSelectorViewHolder, position: Int) {
        holder.text.text = items[position]
        if (items[position] == value) {
            holder.text.setBackgroundColor(context.getColor(R.color.primary))
            holder.text.setTextColor(context.getColor(R.color.onSecondary))
        } else {
            holder.text.setBackgroundColor(context.getColor(R.color.datePickerBackground))
            holder.text.setTextColor(context.getColor(R.color.onBackground))
        }
        holder.bind(items[position], listener)
    }

    override fun getItemCount() = items.size

    fun updateItemState(position: Int) {
        value = items[position]
        notifyItemChanged(position)
    }

    fun clearSelection(position: Int) {
        value = ""
        notifyItemChanged(position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(items: List<String>) {
        this.items = items
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(items: List<String>, initialValue: String) {
        this.items = items
        value = initialValue
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(items: List<String>, initialPosition: Int) {
        this.items = items
        value = items[initialPosition]
        notifyDataSetChanged()
    }

    interface RecyclerViewOnItemClickListener {
        fun onItemClick(item: String)
    }
}
