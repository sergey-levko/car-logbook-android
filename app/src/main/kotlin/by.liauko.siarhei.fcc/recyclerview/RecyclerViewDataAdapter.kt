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
import by.liauko.siarhei.fcc.util.ApplicationUtil.type
import by.liauko.siarhei.fcc.util.DataType
import by.liauko.siarhei.fcc.util.DateConverter
import java.util.Calendar

class RecyclerViewDataAdapter(
    val dataSet: ArrayList<AppData>,
    val resources: Resources,
    val repositoryCollection: AppRepositoryCollection,
    private val noDataTextView: TextView,
    private val listener: RecyclerViewOnItemClickListener
) : RecyclerView.Adapter<RecyclerViewDataViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewDataViewHolder {
        when (type) {
            DataType.LOG -> {
                return LogDataViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.recycler_view_item_log,
                        parent,
                        false
                    )
                )
            }
            DataType.FUEL -> {
                return FuelDataViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.recycler_view_item_fuel,
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerViewDataViewHolder, position: Int) {
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

    private fun bindFuelItem(item: FuelConsumptionData, holder: RecyclerViewDataViewHolder) {
        holder as FuelDataViewHolder
        holder.result.text = String.format("%.2f %s", item.fuelConsumption, resources.getString(R.string.consumption_value))
        holder.parameters.text = String.format("%.2f %s / %.1f %s", item.litres, resources.getString(R.string.liters), item.distance, resources.getString(R.string.km))
    }

    private fun bindLogItem(item: LogData, holder: RecyclerViewDataViewHolder) {
        holder as LogDataViewHolder
        holder.title.text = item.title
        holder.details.text = item.text
        holder.mileage.text = String.format("%d %s", item.mileage, resources.getString(R.string.km))
    }

    private fun showNoDataText() {
        noDataTextView.visibility = View.VISIBLE
    }

    private fun hideNoDataText() {
        noDataTextView.visibility = View.GONE
    }

    interface RecyclerViewOnItemClickListener {
        fun onItemClick(item: AppData)
    }
}