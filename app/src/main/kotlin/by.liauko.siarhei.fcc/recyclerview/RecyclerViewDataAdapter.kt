package by.liauko.siarhei.fcc.recyclerview

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import by.liauko.siarhei.fcc.R
import by.liauko.siarhei.fcc.entity.AppData
import by.liauko.siarhei.fcc.entity.FuelConsumptionData
import by.liauko.siarhei.fcc.entity.LogData
import by.liauko.siarhei.fcc.repository.AppRepositoryCollection
import by.liauko.siarhei.fcc.util.DateConverter
import java.util.Calendar

class RecyclerViewDataAdapter(val dataSet: ArrayList<AppData>,
                              val resources: Resources,
                              val repositoryCollection: AppRepositoryCollection,
                              private val noDataTextView: TextView,
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
        if (dataSet.isEmpty()) {
            showNoDataText()
        } else {
            hideNoDataText()
            dataSet.sortByDescending { it.time }
        }
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        dataSet.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, dataSet.size)
        if (dataSet.isEmpty()) showNoDataText()
    }

    fun restoreItem(data: AppData, position: Int) {
        dataSet.add(position, data)
        notifyItemInserted(position)
        hideNoDataText()
    }

    private fun bindFuelItem(item: FuelConsumptionData, holder: DataViewHolder) {
        holder.result.text = String.format("%.2f %s", item.fuelConsumption, resources.getString(R.string.consumption_value))
        holder.details.visibility = View.GONE
        holder.parameters.text = String.format("%.2f %s / %.1f %s", item.litres, resources.getString(R.string.liters), item.distance, resources.getString(R.string.km))
    }

    private fun bindLogItem(item: LogData, holder: DataViewHolder) {
        holder.result.text = item.title
        holder.details.visibility = View.VISIBLE
        holder.details.text = item.text
        holder.parameters.text = String.format("%d %s", item.mileage, resources.getString(R.string.km))
    }

    private fun showNoDataText() {
        noDataTextView.visibility = View.VISIBLE
    }

    private fun hideNoDataText() {
        noDataTextView.visibility = View.GONE
    }
}

interface RecyclerViewOnItemClickListener {
    fun onItemClick(item: AppData)
}

class DataViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val result: TextView = itemView.findViewById(R.id.result)
    val details: TextView = itemView.findViewById(R.id.details)
    val parameters: TextView = itemView.findViewById(R.id.parameters)
    val date: TextView = itemView.findViewById(R.id.date)

    fun bind(item: AppData, listener: RecyclerViewOnItemClickListener) {
        itemView.setOnClickListener { listener.onItemClick(item) }
    }
}