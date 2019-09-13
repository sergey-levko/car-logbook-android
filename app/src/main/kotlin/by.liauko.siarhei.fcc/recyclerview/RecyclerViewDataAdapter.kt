package by.liauko.siarhei.fcc.recyclerview

import android.content.res.Resources
import android.database.sqlite.SQLiteDatabase
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import by.liauko.siarhei.fcc.R
import by.liauko.siarhei.fcc.entity.Data
import by.liauko.siarhei.fcc.entity.FuelConsumptionData
import by.liauko.siarhei.fcc.entity.LogData
import by.liauko.siarhei.fcc.util.DateConverter
import java.util.Calendar

class RecyclerViewDataAdapter(val dataSet: ArrayList<Data>,
                              val resources: Resources,
                              val database: SQLiteDatabase,
                              private val listener: RecyclerViewOnItemClickListener)
    : RecyclerView.Adapter<DataViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        DataViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_item, parent, false))

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        val item = dataSet[position]
        if (item is LogData) {
            bindLogItem(item, holder)
        } else if (item is FuelConsumptionData) {
            bindFuelItem(item, holder)
        }

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

    fun restoreItem(data: Data, position: Int) {
        dataSet.add(position, data)
        notifyItemInserted(position)
    }

    private fun bindFuelItem(item: FuelConsumptionData, holder: DataViewHolder) {
        holder.result.text = String.format("%.2f %s", item.fuelConsumption, resources.getString(R.string.consumption_value))
        holder.parameters.text = String.format("%.2f %s / %.1f %s", item.litres, resources.getString(R.string.liters), item.distance, resources.getString(R.string.km))
    }

    private fun bindLogItem(item: LogData, holder: DataViewHolder) {
        holder.result.text = item.title
        holder.parameters.text = String.format("%d %s", item.mileage, resources.getString(R.string.km))
    }
}

interface RecyclerViewOnItemClickListener {
    fun onItemClick(item: Data)
}

class DataViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val result: TextView = itemView.findViewById(R.id.result) as TextView
    val parameters: TextView = itemView.findViewById(R.id.parameters) as TextView
    val date: TextView = itemView.findViewById(R.id.date) as TextView

    fun bind(item: Data, listener: RecyclerViewOnItemClickListener) {
        itemView.setOnClickListener { listener.onItemClick(item) }
    }
}