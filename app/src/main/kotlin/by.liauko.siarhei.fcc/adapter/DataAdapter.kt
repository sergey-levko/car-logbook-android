package by.liauko.siarhei.fcc.adapter

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import by.liauko.siarhei.fcc.R
import by.liauko.siarhei.fcc.entity.FuelConsumptionData
import by.liauko.siarhei.fcc.util.DateConverter

class DataAdapter(private val dataSet: List<FuelConsumptionData>, private val resources: Resources)
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
}