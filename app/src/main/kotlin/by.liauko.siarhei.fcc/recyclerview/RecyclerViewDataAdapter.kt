package by.liauko.siarhei.fcc.recyclerview

import android.content.res.Resources
import android.database.sqlite.SQLiteDatabase
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import by.liauko.siarhei.fcc.R
import by.liauko.siarhei.fcc.entity.FuelConsumptionData
import by.liauko.siarhei.fcc.util.DateConverter
import java.util.Calendar

class RecyclerViewDataAdapter(val dataSet: MutableList<FuelConsumptionData>,
                              val resources: Resources,
                              val database: SQLiteDatabase,
                              private val listener: RecyclerViewOnItemClickListener)
    : RecyclerView.Adapter<FuelConsumptionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        FuelConsumptionViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_item, parent, false))

    override fun onBindViewHolder(holder: FuelConsumptionViewHolder, position: Int) {
        val item = dataSet[position]
        holder.fuelConsumption.text = String.format("%.2f %s", item.fuelConsumption, resources.getString(R.string.consumption_value))
        holder.parameters.text = String.format("%.2f %s / %.1f %s", item.litres, resources.getString(R.string.liters), item.distance, resources.getString(R.string.km))
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = item.time
        holder.date.text = DateConverter.convert(calendar)
        holder.bind(item, listener)
    }

    override fun getItemCount() = dataSet.size

    fun refreshRecyclerView() {
        dataSet.sortByDescending { it.time }
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        dataSet.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, dataSet.size)
    }

    fun restoreItem(data: FuelConsumptionData, position: Int) {
        dataSet.add(position, data)
        notifyItemInserted(position)
    }
}

interface RecyclerViewOnItemClickListener {
    fun onItemClick(item: FuelConsumptionData)
}

class FuelConsumptionViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val fuelConsumption: TextView = itemView.findViewById(R.id.result) as TextView
    val parameters: TextView = itemView.findViewById(R.id.parameters) as TextView
    val date: TextView = itemView.findViewById(R.id.date) as TextView

    fun bind(item: FuelConsumptionData, listener: RecyclerViewOnItemClickListener) {
        itemView.setOnClickListener { listener.onItemClick(item) }
    }
}