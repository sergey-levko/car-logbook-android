package by.liauko.siarhei.fcc.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import by.liauko.siarhei.fcc.R
import by.liauko.siarhei.fcc.activity.element.PeriodSelectorElement
import by.liauko.siarhei.fcc.database.CarLogDatabase
import by.liauko.siarhei.fcc.fragment.DataFragment
import by.liauko.siarhei.fcc.fragment.SettingsFragment
import by.liauko.siarhei.fcc.util.AppTheme
import by.liauko.siarhei.fcc.util.ApplicationUtil.appTheme
import by.liauko.siarhei.fcc.util.ApplicationUtil.dataPeriod
import by.liauko.siarhei.fcc.util.ApplicationUtil.periodCalendar
import by.liauko.siarhei.fcc.util.ApplicationUtil.type
import by.liauko.siarhei.fcc.util.DataPeriod
import by.liauko.siarhei.fcc.util.DataType
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Calendar

class MainActivity : AppCompatActivity(),
    BottomNavigationView.OnNavigationItemSelectedListener {
    private lateinit var toolbar: Toolbar
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var periodSelector: PeriodSelectorElement


    override fun onCreate(savedInstanceState: Bundle?) {
        val preferences =  getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE)
        type = DataType.valueOf(preferences.getString(getString(R.string.main_screen_key), "LOG")!!)
        appTheme = AppTheme.valueOf(preferences.getString(getString(R.string.theme_key), "KITTY")!!)
        dataPeriod = DataPeriod.valueOf(preferences.getString(getString(R.string.period_key), "MONTH")!!)
        setTheme(appTheme.appId)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        periodSelector = PeriodSelectorElement(this, findViewById(R.id.main_coordinator_layout))
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
                    if (!periodSelector.isShown) {
                        periodSelector.show()
                    } else {
                        periodSelector.hide()
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var result = false
        periodSelector.hide()

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
                periodCalendar = Calendar.getInstance()
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

    fun loadFragment() {
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
                periodSelector.requestCodePeriodDialog -> {
                    periodSelector.updateYear(data.getStringExtra("year"))
                }
            }
        }
    }
}