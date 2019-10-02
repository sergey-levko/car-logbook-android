package by.liauko.siarhei.fcc.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import by.liauko.siarhei.fcc.R
import by.liauko.siarhei.fcc.database.CarLogDatabase
import by.liauko.siarhei.fcc.fragment.DataFragment
import by.liauko.siarhei.fcc.fragment.SettingsFragment
import by.liauko.siarhei.fcc.util.AppTheme
import by.liauko.siarhei.fcc.util.ApplicationUtil.appTheme
import by.liauko.siarhei.fcc.util.ApplicationUtil.dataPeriod
import by.liauko.siarhei.fcc.util.ApplicationUtil.period
import by.liauko.siarhei.fcc.util.ApplicationUtil.type
import by.liauko.siarhei.fcc.util.DataPeriod
import by.liauko.siarhei.fcc.util.DataType
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Calendar

class MainActivity : AppCompatActivity(),
    View.OnClickListener,
    BottomNavigationView.OnNavigationItemSelectedListener {
    private val requestCodePeriodDialog = 1

    private lateinit var toolbar: Toolbar
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var periodSelector: LinearLayout
    private lateinit var yearTextView: TextView
    private lateinit var previousYearButton: ImageButton
    private lateinit var nextYearButton: ImageButton
    private lateinit var months: ArrayList<Button>

    private var year = 1970
    private val minYear = 1970
    private val currentYear = Calendar.getInstance()[Calendar.YEAR]
    private val currentMonth = Calendar.getInstance()[Calendar.MONTH]
    private var selectedMonth = currentMonth
    private var selectedYear = currentYear
    private var accentColorId = -1
    private var textColorId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        val preferences =  getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE)
        type = DataType.valueOf(preferences.getString(getString(R.string.main_screen_key), "LOG")!!)
        appTheme = AppTheme.valueOf(preferences.getString(getString(R.string.theme_key), "KITTY")!!)
        dataPeriod = DataPeriod.valueOf(preferences.getString(getString(R.string.period_key), "MONTH")!!)
        setTheme(appTheme.appId)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initPeriodSelector()
        initToolbar()
        initBottomNavigationView()
    }

    private fun initToolbar() {
        toolbar = findViewById(R.id.toolbar)
        toolbar.inflateMenu(R.menu.period_select_menu)
        toolbar.setOnMenuItemClickListener {
            var result = false
            when (it.itemId) {
                R.id.period_select_menu_date -> {
                    when (periodSelector.visibility) {
                        View.VISIBLE -> periodSelector.visibility = View.GONE
                        else -> {
                            periodSelector.visibility = View.VISIBLE
                            showPeriodSelector()
                        }
                    }
                    result = true
                }
            }

            return@setOnMenuItemClickListener result
        }
    }

    private fun initBottomNavigationView() {
        bottomNavigationView = findViewById(R.id.bottom_navigation_view)
        bottomNavigationView.setOnNavigationItemSelectedListener(this)
        bottomNavigationView.selectedItemId = when (type) {
            DataType.LOG -> R.id.log_menu_item
            DataType.FUEL -> R.id.fuel_menu_item
        }
    }

    private fun initPeriodSelector() {
        val value = TypedValue()
        theme.resolveAttribute(R.attr.colorAccent, value, true)
        accentColorId = value.data

        periodSelector = findViewById(R.id.period_selector)

        val positiveButton = findViewById<Button>(R.id.period_dialog_positive_button)
        positiveButton.setOnClickListener(this)
        findViewById<Button>(R.id.period_dialog_negative_button).setOnClickListener(this)

        previousYearButton = findViewById(R.id.previous_year_button)
        previousYearButton.setOnClickListener(this)
        nextYearButton = findViewById(R.id.next_year_button)
        nextYearButton.setOnClickListener(this)
        nextYearButton.isEnabled = false
        yearTextView = findViewById(R.id.year_text_view)
        yearTextView.setOnClickListener {
            val intent = Intent(applicationContext, YearSelectorDialogActivity::class.java)
            intent.putExtra("year", year)
            startActivityForResult(intent, requestCodePeriodDialog)
        }

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
        positiveButton.setText(R.string.period_dialog_positive_button_month)
        findViewById<GridLayout>(R.id.months_grid_layout).visibility = View.GONE
        positiveButton.setText(R.string.period_dialog_positive_button_year)
    }

    private fun showPeriodSelector() {
        when (dataPeriod) {
            DataPeriod.MONTH -> {
                findViewById<GridLayout>(R.id.months_grid_layout).visibility = View.VISIBLE
                findViewById<Button>(R.id.period_dialog_positive_button).setText(R.string.period_dialog_positive_button_month)
            }
            DataPeriod.YEAR -> {
                findViewById<GridLayout>(R.id.months_grid_layout).visibility = View.GONE
                findViewById<Button>(R.id.period_dialog_positive_button).setText(R.string.period_dialog_positive_button_year)
            }
        }

        yearTextView.text = year.toString()
        months[selectedMonth].setTextColor(accentColorId)
    }

    override fun onResume() {
        super.onResume()
        loadFragment()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("item_id", bottomNavigationView.selectedItemId)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        bottomNavigationView.selectedItemId = savedInstanceState!!.getInt("item_id")
    }

    override fun onDestroy() {
        super.onDestroy()
        CarLogDatabase.closeDatabase()
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.period_dialog_positive_button -> {
                    period = when (dataPeriod) {
                        DataPeriod.MONTH -> Calendar.getInstance().timeInMillis
                        DataPeriod.YEAR -> {
                            val calendar = Calendar.getInstance()
                            calendar.set(Calendar.YEAR, year)
                            calendar.timeInMillis
                        }
                    }
                    selectedMonth = currentMonth
                    year = currentYear
                    selectedYear = currentYear
                    periodSelector.visibility = View.GONE
                    loadFragment()
                }
                R.id.period_dialog_negative_button -> {
                    periodSelector.visibility = View.GONE
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
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.MONTH, selectedMonth)
                    calendar.set(Calendar.YEAR, selectedYear)
                    period = calendar.timeInMillis
                    periodSelector.visibility = View.GONE
                    loadFragment()
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
        if (year == selectedYear - 1) {
            months[selectedMonth].setTextColor(textColorId)
        }
        if (year == selectedYear) {
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
        if (year == selectedYear + 1) {
            months[selectedMonth].setTextColor(textColorId)
        }
        if (year == selectedYear) {
            months[selectedMonth].setTextColor(accentColorId)
        }
        if (!previousYearButton.isEnabled) {
            previousYearButton.isEnabled = true
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var result = false
        periodSelector.visibility = View.GONE

        when (item.itemId) {
            R.id.log_menu_item -> {
                toolbar.menu.findItem(R.id.period_select_menu_date).isVisible = true
                type = DataType.LOG
                loadFragment(DataFragment(), R.string.data_fragment_log_title)
                result = true
            }
            R.id.fuel_menu_item -> {
                toolbar.menu.findItem(R.id.period_select_menu_date).isVisible = true
                type = DataType.FUEL
                loadFragment(DataFragment(), R.string.data_fragment_fuel_title)
                result = true
            }
            R.id.settings_menu_item -> {
                toolbar.menu.findItem(R.id.period_select_menu_date).isVisible = false
                selectedMonth = currentMonth
                selectedYear = currentYear
                period = Calendar.getInstance().timeInMillis
                loadFragment(SettingsFragment(), R.string.settings_fragment_title)
                result = true
            }
        }
        return result
    }

    private fun loadFragment(fragment: Fragment, titleId: Int) {
        toolbar.setTitle(titleId)
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frame_container, fragment)
            .commit()
    }

    private fun loadFragment() {
        when (bottomNavigationView.selectedItemId) {
            R.id.log_menu_item -> loadFragment(DataFragment(), R.string.data_fragment_log_title)
            R.id.fuel_menu_item -> loadFragment(DataFragment(), R.string.data_fragment_fuel_title)
            R.id.settings_menu_item -> loadFragment(SettingsFragment(), R.string.settings_fragment_title)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                requestCodePeriodDialog -> {
                    val value = data.getStringExtra("year")
                    yearTextView.text = value
                    year = value.toInt()
                    if (year == selectedYear) {
                        months[selectedMonth].setTextColor(accentColorId)
                        updateYearButtonsState(false, true)
                    } else if (year == minYear && previousYearButton.isEnabled) {
                        months[selectedMonth].setTextColor(textColorId)
                        updateYearButtonsState(true, false)
                    } else {
                        months[selectedMonth].setTextColor(textColorId)
                        updateYearButtonsState(true, true)
                    }
                }
            }
        }
    }

    private fun updateYearButtonsState(isNextButtonEnabled: Boolean, isPreviousButtonEnabled: Boolean) {
        nextYearButton.isEnabled = isNextButtonEnabled
        previousYearButton.isEnabled = isPreviousButtonEnabled
    }
}