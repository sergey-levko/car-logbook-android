package by.liauko.siarhei.cl.recyclerview.holder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.model.DataModel
import by.liauko.siarhei.cl.recyclerview.adapter.RecyclerViewDataAdapter

abstract class RecyclerViewDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    abstract val date: TextView

    fun bind(item: DataModel, listener: RecyclerViewDataAdapter.RecyclerViewOnItemClickListener) {
        itemView.setOnClickListener { listener.onItemClick(item) }
    }
}

class LogDataViewHolder(itemView: View) : RecyclerViewDataViewHolder(itemView) {

    val title: TextView = itemView.findViewById(R.id.log_item_title)
    val details: TextView = itemView.findViewById(R.id.log_item_details)
    val mileage: TextView = itemView.findViewById(R.id.log_item_mileage)
    override val date: TextView = itemView.findViewById(R.id.log_item_date)
}

class FuelDataViewHolder(itemView: View) : RecyclerViewDataViewHolder(itemView) {

    val result: TextView = itemView.findViewById(R.id.fuel_item_result)
    val parameters: TextView = itemView.findViewById(R.id.fuel_item_parameters)
    override val date: TextView = itemView.findViewById(R.id.fuel_item_date)
}
