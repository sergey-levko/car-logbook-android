package by.liauko.siarhei.cl.activity.dialog

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.databinding.DialogPeriodSelectorBinding
import by.liauko.siarhei.cl.recyclerview.adapter.RecyclerViewSelectorAdapter
import by.liauko.siarhei.cl.util.ApplicationUtil
import by.liauko.siarhei.cl.util.DataPeriod
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Calendar

class PeriodSelectorDialog(
    private val applicationContext: Context
) {

    private val builder: MaterialAlertDialogBuilder
    private val months = arrayListOf<String>()
    private val years = arrayListOf<String>()
    private val minYear = 1970
    private val currentYear = Calendar.getInstance()[Calendar.YEAR]
    private val viewBinding = DialogPeriodSelectorBinding.inflate(LayoutInflater.from(applicationContext))

    private lateinit var dialog: AlertDialog
    private lateinit var rvAdapter: RecyclerViewSelectorAdapter

    private var selectedMonth = ApplicationUtil.periodCalendar[Calendar.MONTH]
    private var selectedYear = ApplicationUtil.periodCalendar[Calendar.YEAR]
    private var previousYear = selectedYear
    private var yearSelector = false

    init {
        builder = MaterialAlertDialogBuilder(applicationContext)
            .setView(viewBinding.root)
            .setPositiveButton(
                if (ApplicationUtil.dataPeriod == DataPeriod.MONTH)
                    applicationContext.getString(R.string.period_dialog_positive_button_month)
                else
                    applicationContext.getString(R.string.period_dialog_positive_button_year)
            ) { _, _ ->
                if (ApplicationUtil.dataPeriod == DataPeriod.MONTH)
                    ApplicationUtil.periodCalendar = Calendar.getInstance()
                else
                    ApplicationUtil.periodCalendar.set(
                        Calendar.YEAR,
                        currentYear
                    )
            }
            .setNegativeButton(applicationContext.getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }

        initSelectorLists()
        initRecyclerView()
        initPeriodSelector()
    }

    fun setOnDismissListener(listener: DialogInterface.OnDismissListener) {
        builder.setOnDismissListener(listener)
    }

    fun show() {
        dialog = builder.show()
    }

    private fun initSelectorLists() {
        if (ApplicationUtil.dataPeriod == DataPeriod.MONTH) {
            months.addAll(applicationContext.resources.getStringArray(R.array.month_names))
        }
        for (year in currentYear downTo minYear) {
            years.add(year.toString())
        }
    }

    private fun initPeriodSelector() {
        if (ApplicationUtil.dataPeriod == DataPeriod.MONTH) {
            viewBinding.selectYearPanel.visibility = View.VISIBLE
        } else {
            viewBinding.selectYearPanel.visibility = View.GONE
        }

        viewBinding.previousYearButton.setOnClickListener {
            handlePreviousYearButtonClick()
        }
        viewBinding.nextYearButton.setOnClickListener {
            handleNextYearButtonClick()
        }
        updateYearButtonsState()

        viewBinding.yearText.text = selectedYear.toString()
        viewBinding.yearText.setOnClickListener {
            yearSelector = if (yearSelector) {
                rvAdapter.updateItems(months, selectedMonth)
                viewBinding.yearText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.menu_down, 0)
                false
            } else {
                rvAdapter.updateItems(years, selectedYear.toString())
                viewBinding.yearText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.menu_up, 0)
                true
            }
        }
    }

    private fun updateYearButtonsState() {
        when (selectedYear) {
            currentYear -> {
                viewBinding.nextYearButton.isEnabled = false
                viewBinding.previousYearButton.isEnabled = true
            }
            minYear -> {
                viewBinding.nextYearButton.isEnabled = true
                viewBinding.previousYearButton.isEnabled = false
            }
            else -> {
                viewBinding.nextYearButton.isEnabled = true
                viewBinding.nextYearButton.isEnabled = true
            }
        }

        viewBinding.yearText.text = selectedYear.toString()
        viewBinding.yearText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.menu_down, 0)
    }

    private fun initRecyclerView() {
        rvAdapter =
            RecyclerViewSelectorAdapter(
                applicationContext,
                if (ApplicationUtil.dataPeriod == DataPeriod.MONTH) months[selectedMonth] else selectedYear.toString(),
                if (ApplicationUtil.dataPeriod == DataPeriod.MONTH) months else years,
                object : RecyclerViewSelectorAdapter.RecyclerViewOnItemClickListener {
                    override fun onItemClick(item: String) {
                        if (ApplicationUtil.dataPeriod == DataPeriod.MONTH && !yearSelector) {
                            ApplicationUtil.periodCalendar.set(Calendar.MONTH, months.indexOf(item))
                            ApplicationUtil.periodCalendar.set(Calendar.YEAR, selectedYear)
                            dialog.dismiss()
                        } else {
                            selectedYear = item.toInt()
                            if (ApplicationUtil.dataPeriod == DataPeriod.MONTH) {
                                rvAdapter.updateItems(months)
                                updateYearButtonsState()
                                if (selectedYear == previousYear) {
                                    rvAdapter.updateItemState(selectedMonth)
                                } else {
                                    rvAdapter.clearSelection(selectedMonth)
                                }
                                yearSelector = false
                            } else {
                                ApplicationUtil.periodCalendar.set(Calendar.YEAR, selectedYear)
                                dialog.dismiss()
                            }
                        }
                    }
                })

        viewBinding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, 3)
            adapter = rvAdapter
        }
    }

    private fun handlePreviousYearButtonClick() {
        selectedYear--
        viewBinding.yearText.text = selectedYear.toString()
        if (selectedYear == minYear) {
            viewBinding.previousYearButton.isEnabled = false
        }
        if (selectedYear == previousYear - 1) {
            rvAdapter.clearSelection(selectedMonth)
        }
        if (selectedYear == previousYear) {
            rvAdapter.updateItemState(selectedMonth)
        }
        if (!viewBinding.nextYearButton.isEnabled) {
            viewBinding.nextYearButton.isEnabled = true
        }
    }

    private fun handleNextYearButtonClick() {
        selectedYear++
        viewBinding.yearText.text = selectedYear.toString()
        if (selectedYear == currentYear) {
            viewBinding.nextYearButton.isEnabled = false
        }
        if (selectedYear == previousYear + 1) {
            rvAdapter.clearSelection(selectedMonth)
        }
        if (selectedYear == previousYear) {
            rvAdapter.updateItemState(selectedMonth)
        }
        if (!viewBinding.previousYearButton.isEnabled) {
            viewBinding.previousYearButton.isEnabled = true
        }
    }
}
