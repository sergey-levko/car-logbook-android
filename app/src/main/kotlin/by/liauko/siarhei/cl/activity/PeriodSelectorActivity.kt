package by.liauko.siarhei.cl.activity

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.databinding.DialogPeriodSelectorBinding
import by.liauko.siarhei.cl.util.AppResultCodes
import by.liauko.siarhei.cl.util.ApplicationUtil
import by.liauko.siarhei.cl.util.DataPeriod
import java.util.Calendar

class PeriodSelectorActivity : AppCompatActivity(),
    View.OnClickListener {

    private val minYear = 1970
    private val currentYear = Calendar.getInstance()[Calendar.YEAR]
    private val currentMonth = Calendar.getInstance()[Calendar.MONTH]

    private lateinit var viewBinding: DialogPeriodSelectorBinding
    private lateinit var months: ArrayList<Button>

    private var previousYear = currentYear
    private var selectedMonth = currentMonth
    private var selectedYear = currentYear
    private var accentColorId = -1
    private var textColorId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DialogPeriodSelectorBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        val parameters = window.attributes
        parameters.width = WindowManager.LayoutParams.MATCH_PARENT
        window.attributes = parameters

        val value = TypedValue()
        theme.resolveAttribute(R.attr.colorSecondary, value, true)
        accentColorId = value.data

        initPeriodSelector()
    }

    private fun initPeriodSelector() {
        selectedMonth = ApplicationUtil.periodCalendar[Calendar.MONTH]
        selectedYear = ApplicationUtil.periodCalendar[Calendar.YEAR]
        previousYear = selectedYear

        viewBinding.periodDialogPositiveButton.setOnClickListener(this)
        when (ApplicationUtil.dataPeriod) {
            DataPeriod.MONTH -> {
                initMonthButtons()
                viewBinding.periodDialogPositiveButton.setText(R.string.period_dialog_positive_button_month)
            }
            DataPeriod.YEAR -> {
                viewBinding.monthsGridLayout.visibility = View.GONE
                viewBinding.periodDialogPositiveButton.setText(R.string.period_dialog_positive_button_year)
            }
            DataPeriod.ALL -> return
        }

        viewBinding.periodDialogNegativeButton.setOnClickListener(this)

        viewBinding.previousYearButton.setOnClickListener(this)
        viewBinding.nextYearButton.setOnClickListener(this)
        updateYearButtonsState()

        viewBinding.yearTextView.setOnClickListener {
            val intent = Intent(applicationContext, YearSelectorDialogActivity::class.java)
            intent.putExtra("year", selectedYear)
            startActivityForResult(intent, AppResultCodes.YEAR_DIALOG_RESULT)
        }
        viewBinding.yearTextView.text = selectedYear.toString()
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
    }

    private fun initMonthButtons() {
        months = mutableListOf<Button>() as ArrayList<Button>
        months.add(viewBinding.jan)
        months.add(viewBinding.feb)
        months.add(viewBinding.mar)
        months.add(viewBinding.apr)
        months.add(viewBinding.may)
        months.add(viewBinding.jun)
        months.add(viewBinding.jul)
        months.add(viewBinding.aug)
        months.add(viewBinding.sep)
        months.add(viewBinding.oct)
        months.add(viewBinding.nov)
        months.add(viewBinding.dec)
        for (button in months) {
            button.setOnClickListener(this)
        }
        textColorId = months[0].currentTextColor
        months[selectedMonth].setTextColor(accentColorId)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.period_dialog_positive_button -> {
                    when (ApplicationUtil.dataPeriod) {
                        DataPeriod.MONTH -> ApplicationUtil.periodCalendar = Calendar.getInstance()
                        DataPeriod.YEAR -> ApplicationUtil.periodCalendar.set(Calendar.YEAR, selectedYear)
                        DataPeriod.ALL -> return
                    }
                    setResult(RESULT_OK)
                    finish()
                }
                R.id.period_dialog_negative_button -> {
                    setResult(RESULT_CANCELED)
                    finish()
                }
                R.id.previous_year_button -> {
                    handlePreviousYearButtonClick()
                }
                R.id.next_year_button -> {
                    handleNextYearButtonClick()
                }
                else -> {
                    months[selectedMonth].setTextColor(textColorId)
                    selectedMonth = months.indexOf(v)
                    ApplicationUtil.periodCalendar.set(Calendar.MONTH, selectedMonth)
                    ApplicationUtil.periodCalendar.set(Calendar.YEAR, selectedYear)
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                AppResultCodes.YEAR_DIALOG_RESULT -> updateYear(data?.getStringExtra("year"))
            }
        }
    }

    private fun handlePreviousYearButtonClick() {
        selectedYear--
        viewBinding.yearTextView.text = selectedYear.toString()
        if (selectedYear == minYear) {
            viewBinding.previousYearButton.isEnabled = false
        }
        if (DataPeriod.MONTH == ApplicationUtil.dataPeriod && selectedYear == previousYear - 1) {
            months[selectedMonth].setTextColor(textColorId)
        }
        if (DataPeriod.MONTH == ApplicationUtil.dataPeriod && selectedYear == previousYear) {
            months[selectedMonth].setTextColor(accentColorId)
        }
        if (!viewBinding.nextYearButton.isEnabled) {
            viewBinding.nextYearButton.isEnabled = true
        }
    }

    private fun handleNextYearButtonClick() {
        selectedYear++
        viewBinding.yearTextView.text = selectedYear.toString()
        if (selectedYear == currentYear) {
            viewBinding.nextYearButton.isEnabled = false
        }
        if (DataPeriod.MONTH == ApplicationUtil.dataPeriod && selectedYear == previousYear + 1) {
            months[selectedMonth].setTextColor(textColorId)
        }
        if (DataPeriod.MONTH == ApplicationUtil.dataPeriod && selectedYear == previousYear) {
            months[selectedMonth].setTextColor(accentColorId)
        }
        if (!viewBinding.previousYearButton.isEnabled) {
            viewBinding.previousYearButton.isEnabled = true
        }
    }

    private fun updateYear(value: String?) {
        val newYear = value ?: currentYear.toString()
        viewBinding.yearTextView.text = newYear
        selectedYear = newYear.toInt()
        updateYearButtonsState()
        if (DataPeriod.MONTH == ApplicationUtil.dataPeriod) {
            if (previousYear == selectedYear) {
                months[selectedMonth].setTextColor(accentColorId)
            } else {
                months[selectedMonth].setTextColor(textColorId)
            }
        } else {
            ApplicationUtil.periodCalendar.set(Calendar.YEAR, selectedYear)
            setResult(RESULT_OK)
            finish()
        }
    }
}
