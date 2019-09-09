package by.liauko.siarhei.fcc.adapter

import android.content.res.Resources
import android.database.sqlite.SQLiteDatabase
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import by.liauko.siarhei.fcc.R
import by.liauko.siarhei.fcc.entity.FuelConsumptionData
import by.liauko.siarhei.fcc.util.DateConverter

class DataAdapter(val dataSet: MutableList<FuelConsumptionData>, val resources: Resources, val database: SQLiteDatabase)
    : RecyclerView.Adapter<DataAdapter.DataViewHolder>() {

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fuelConsumption: TextView = itemView.findViewById(R.id.result) as TextView
        val parameters: TextView = itemView.findViewById(R.id.parameters) as TextView
        val date: TextView = itemView.findViewById(R.id.date) as TextView

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        DataViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_item, parent, false))

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        val item = dataSet[position]
        holder.fuelConsumption.text = String.format("%.2f %s", item.fuelConsumption, resources.getString(R.string.consumption_value))
        holder.parameters.text = String.format("%.2f %s / %.1f %s", item.litres, resources.getString(R.string.liters), item.distance, resources.getString(R.string.km))
        holder.date.text = DateConverter.convert(item.date)
    }

    override fun getItemCount() = dataSet.size

    fun refreshRecyclerView() {
        dataSet.sortByDescending { it.date }
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