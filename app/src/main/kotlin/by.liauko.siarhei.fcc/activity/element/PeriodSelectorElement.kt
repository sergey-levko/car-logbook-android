package by.liauko.siarhei.fcc.activity.element

import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import by.liauko.siarhei.fcc.R
import by.liauko.siarhei.fcc.activity.MainActivity
import by.liauko.siarhei.fcc.activity.YearSelectorDialogActivity
import by.liauko.siarhei.fcc.util.ApplicationUtil
import by.liauko.siarhei.fcc.util.DataPeriod
import java.util.Calendar

class PeriodSelectorElement(private val parent: MainActivity, private val rootView: ViewGroup)
    : View.OnClickListener,
    Animation.AnimationListener {
    private val layoutInflater = parent.applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var view: View? = null
    private val minYear = 1970
    private val currentYear = Calendar.getInstance()[Calendar.YEAR]
    private val currentMonth = Calendar.getInstance()[Calendar.MONTH]

    private lateinit var periodSelector: LinearLayout
    private lateinit var yearTextView: TextView
    private lateinit var previousYearButton: ImageButton
    private lateinit var nextYearButton: ImageButton
    private lateinit var months: ArrayList<Button>

    private var year = currentYear
    private var selectedMonth = currentMonth
    private var selectedYear = currentYear
    private var accentColorId = -1
    private var textColorId = -1

    val requestCodePeriodDialog = 1

    var isShown = false

    init {
        parent.applicationContext.setTheme(ApplicationUtil.appTheme.appId)
        val value = TypedValue()
        parent.theme.resolveAttribute(R.attr.colorAccent, value, true)
        accentColorId = value.data
    }

    fun show() {
        view = layoutInflater.inflate(R.layout.period_selector, rootView, false)
        initPeriodSelector()
        rootView.addView(view)
        isShown = true
    }

    fun hide() {
        if (isShown) {
            val periodSelector = view!!.findViewById<LinearLayout>(R.id.period_selector)
            val translateAnimation = TranslateAnimation(0f, 0f, 0f, -periodSelector.height.toFloat())
            translateAnimation.duration = 500
            translateAnimation.setAnimationListener(this)
            periodSelector.startAnimation(translateAnimation)
        }
    }

    override fun onAnimationEnd(animation: Animation?) {
        rootView.removeView(view)
        view = null
        isShown = false
    }

    override fun onAnimationRepeat(animation: Animation?) {}

    override fun onAnimationStart(animation: Animation?) {}

    fun updateYear(value: String) {
        yearTextView.text = value
        year = value.toInt()
        updateYearButtonsState()
        if (year == selectedYear) {
            months[selectedMonth].setTextColor(accentColorId)
        } else {
            months[selectedMonth].setTextColor(textColorId)
        }
    }

    private fun updateYearButtonsState() {
        if (year == currentYear) {
            nextYearButton.isEnabled = false
            previousYearButton.isEnabled = true
        } else if (year == minYear) {
            nextYearButton.isEnabled = true
            previousYearButton.isEnabled = false
        } else {
            nextYearButton.isEnabled = true
            nextYearButton.isEnabled = true
        }
    }

    private fun initPeriodSelector() {
        periodSelector = view!!.findViewById(R.id.period_selector)
        periodSelector.setOnClickListener{}

        selectedMonth = ApplicationUtil.periodCalendar[Calendar.MONTH]
        selectedYear = ApplicationUtil.periodCalendar[Calendar.YEAR]

        val positiveButton = view!!.findViewById<Button>(R.id.period_dialog_positive_button)
        positiveButton.setOnClickListener(this)
        view!!.findViewById<Button>(R.id.period_dialog_negative_button).setOnClickListener(this)

        previousYearButton = view!!.findViewById(R.id.previous_year_button)
        previousYearButton.setOnClickListener(this)
        nextYearButton = view!!.findViewById(R.id.next_year_button)
        nextYearButton.setOnClickListener(this)
        updateYearButtonsState()

        yearTextView = view!!.findViewById(R.id.year_text_view)
        yearTextView.setOnClickListener {
            val intent = Intent(parent.applicationContext, YearSelectorDialogActivity::class.java)
            intent.putExtra("year", year)
            parent.startActivityForResult(intent, requestCodePeriodDialog)
        }
        yearTextView.text = year.toString()

        when (ApplicationUtil.dataPeriod) {
            DataPeriod.MONTH -> {
                initMonthButtons()
                positiveButton.setText(R.string.period_dialog_positive_button_month)
            }
            DataPeriod.YEAR -> {
                view!!.findViewById<GridLayout>(R.id.months_grid_layout).visibility = View.GONE
                positiveButton.setText(R.string.period_dialog_positive_button_year)
            }
            DataPeriod.ALL -> return
        }

        view!!.findViewById<View>(R.id.period_selector_cover_view).setOnClickListener { hide() }
    }

    private fun initMonthButtons() {
        months = mutableListOf<Button>() as ArrayList<Button>
        months.add(view!!.findViewById(R.id.jan))
        months.add(view!!.findViewById(R.id.feb))
        months.add(view!!.findViewById(R.id.mar))
        months.add(view!!.findViewById(R.id.apr))
        months.add(view!!.findViewById(R.id.may))
        months.add(view!!.findViewById(R.id.jun))
        months.add(view!!.findViewById(R.id.jul))
        months.add(view!!.findViewById(R.id.aug))
        months.add(view!!.findViewById(R.id.sep))
        months.add(view!!.findViewById(R.id.oct))
        months.add(view!!.findViewById(R.id.nov))
        months.add(view!!.findViewById(R.id.dec))
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
                        DataPeriod.MONTH -> {
                            ApplicationUtil.periodCalendar = Calendar.getInstance()
                            year = currentYear
                        }
                        DataPeriod.YEAR -> ApplicationUtil.periodCalendar.set(Calendar.YEAR, year)
                        DataPeriod.ALL -> return
                    }
                    selectedMonth = currentMonth
                    selectedYear = year
                    hide()
                    parent.loadFragment()
                }
                R.id.period_dialog_negative_button -> {
                    hide()
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
                    selectedYear = year
                    ApplicationUtil.periodCalendar.set(Calendar.MONTH, selectedMonth)
                    ApplicationUtil.periodCalendar.set(Calendar.YEAR, selectedYear)
                    hide()
                    parent.loadFragment()
                }
            }
        }
    }

    private fun handlePreviousYearButtonClick() {
        year--
        yearTextView.text = year.toString()
        if (year == minYear) {
            previousYearButton.isEnabled = false
        }
        if (year == selectedYear - 1 && DataPeriod.MONTH == ApplicationUtil.dataPeriod) {
            months[selectedMonth].setTextColor(textColorId)
        }
        if (year == selectedYear && DataPeriod.MONTH == ApplicationUtil.dataPeriod) {
            months[selectedMonth].setTextColor(accentColorId)
        }
        if (!nextYearButton.isEnabled) {
            nextYearButton.isEnabled = true
        }
    }

    private fun handleNextYearButtonClick() {
        year++
        yearTextView.text = year.toString()
        if (year == currentYear) {
            nextYearButton.isEnabled = false
        }
        if (year == selectedYear + 1 && DataPeriod.MONTH == ApplicationUtil.dataPeriod) {
            months[selectedMonth].setTextColor(textColorId)
        }
        if (year == selectedYear && DataPeriod.MONTH == ApplicationUtil.dataPeriod) {
            months[selectedMonth].setTextColor(accentColorId)
        }
        if (!previousYearButton.isEnabled) {
            previousYearButton.isEnabled = true
        }
    }
}