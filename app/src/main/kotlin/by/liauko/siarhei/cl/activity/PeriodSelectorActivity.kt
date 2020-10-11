package by.liauko.siarhei.cl.activity

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.util.AppResultCodes
import by.liauko.siarhei.cl.util.ApplicationUtil
import by.liauko.siarhei.cl.util.DataPeriod
import java.util.Calendar

class PeriodSelectorActivity : AppCompatActivity(),
    View.OnClickListener {

    private val minYear = 1970
    private val currentYear = Calendar.getInstance()[Calendar.YEAR]
    private val currentMonth = Calendar.getInstance()[Calendar.MONTH]

    private lateinit var yearTextView: TextView
    private lateinit var previousYearButton: ImageButton
    private lateinit var nextYearButton: ImageButton
    private lateinit var months: ArrayList<Button>

    private var previousYear = currentYear
    private var selectedMonth = currentMonth
    private var selectedYear = currentYear
    private var accentColorId = -1
    private var textColorId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.period_selector)

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

        val positiveButton = findViewById<Button>(R.id.period_dialog_positive_button)
        positiveButton.setOnClickListener(this)
        when (ApplicationUtil.dataPeriod) {
            DataPeriod.MONTH -> {
                initMonthButtons()
                positiveButton.setText(R.string.period_dialog_positive_button_month)
            }
            DataPeriod.YEAR -> {
                findViewById<GridLayout>(R.id.months_grid_layout).visibility = View.GONE
                positiveButton.setText(R.string.period_dialog_positive_button_year)
            }
            DataPeriod.ALL -> return
        }

        findViewById<Button>(R.id.period_dialog_negative_button).setOnClickListener(this)

        previousYearButton = findViewById(R.id.previous_year_button)
        previousYearButton.setOnClickListener(this)
        nextYearButton = findViewById(R.id.next_year_button)
        nextYearButton.setOnClickListener(this)
        updateYearButtonsState()

        yearTextView = findViewById(R.id.year_text_view)
        yearTextView.setOnClickListener {
            val intent = Intent(applicationContext, YearSelectorDialogActivity::class.java)
            intent.putExtra("year", selectedYear)
            startActivityForResult(intent, AppResultCodes.YEAR_DIALOG_RESULT)
        }
        yearTextView.text = selectedYear.toString()
    }

    private fun updateYearButtonsState() {
        when (selectedYear) {
            currentYear -> {
                nextYearButton.isEnabled = false
                previousYearButton.isEnabled = true
            }
            minYear -> {
                nextYearButton.isEnabled = true
                previousYearButton.isEnabled = false
            }
            else -> {
                nextYearButton.isEnabled = true
                nextYearButton.isEnabled = true
            }
        }
    }

    private fun initMonthButtons() {
        months = mutableListOf<Button>() as ArrayList<Button>
        months.add(findViewById(R.id.jan))
        months.add(findViewById(R.id.feb))
        months.add(findViewById(R.id.mar))
        months.add(findViewById(R.id.apr))
        months.add(findViewById(R.id.may))
        months.add(findViewById(R.id.jun))
        months.add(findViewById(R.id.jul))
        months.add(findViewById(R.id.aug))
        months.add(findViewById(R.id.sep))
        months.add(findViewById(R.id.oct))
        months.add(findViewById(R.id.nov))
        months.add(findViewById(R.id.dec))
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
        yearTextView.text = selectedYear.toString()
        if (selectedYear == minYear) {
            previousYearButton.isEnabled = false
        }
        if (DataPeriod.MONTH == ApplicationUtil.dataPeriod && selectedYear == previousYear - 1) {
            months[selectedMonth].setTextColor(textColorId)
        }
        if (DataPeriod.MONTH == ApplicationUtil.dataPeriod && selectedYear == previousYear) {
            months[selectedMonth].setTextColor(accentColorId)
        }
        if (!nextYearButton.isEnabled) {
            nextYearButton.isEnabled = true
        }
    }

    private fun handleNextYearButtonClick() {
        selectedYear++
        yearTextView.text = selectedYear.toString()
        if (selectedYear == currentYear) {
            nextYearButton.isEnabled = false
        }
        if (DataPeriod.MONTH == ApplicationUtil.dataPeriod && selectedYear == previousYear + 1) {
            months[selectedMonth].setTextColor(textColorId)
        }
        if (DataPeriod.MONTH == ApplicationUtil.dataPeriod && selectedYear == previousYear) {
            months[selectedMonth].setTextColor(accentColorId)
        }
        if (!previousYearButton.isEnabled) {
            previousYearButton.isEnabled = true
        }
    }

    private fun updateYear(value: String?) {
        val newYear = value ?: currentYear.toString()
        yearTextView.text = newYear
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