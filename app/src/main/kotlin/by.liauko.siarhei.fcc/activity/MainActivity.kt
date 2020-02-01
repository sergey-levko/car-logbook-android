package by.liauko.siarhei.fcc.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import by.liauko.siarhei.fcc.R
import by.liauko.siarhei.fcc.activity.element.PeriodSelectorElement
import by.liauko.siarhei.fcc.database.CarLogDatabase
import by.liauko.siarhei.fcc.activity.fragment.DataFragment
import by.liauko.siarhei.fcc.activity.fragment.SettingsFragment
import by.liauko.siarhei.fcc.util.AppResultCodes.PERIOD_DIALOG_RESULT
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
    BottomNavigationView.OnNavigationItemSelectedListener,
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private lateinit var toolbar: Toolbar
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var periodSelector: PeriodSelectorElement


    override fun onCreate(savedInstanceState: Bundle?) {
        val preferences =  getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE)
        type = DataType.valueOf(preferences.getString(getString(R.string.main_screen_key), "LOG") ?: "LOG")
        appTheme = AppTheme.valueOf(preferences.getString(getString(R.string.theme_key), "KITTY") ?: "KITTY")
        dataPeriod = DataPeriod.valueOf(preferences.getString(getString(R.string.period_key), "MONTH") ?: "MONTH")
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
        updateToolbarMenuState()
    }

    private fun updateToolbarMenuState() {
        toolbar.menu.findItem(R.id.period_select_menu_date).isVisible = when (dataPeriod) {
            DataPeriod.ALL -> false
            else -> true
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("item_id", bottomNavigationView.selectedItemId)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        bottomNavigationView.selectedItemId = savedInstanceState?.getInt("item_id") ?: R.id.log_menu_item
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
                updateToolbarMenuState()
                type = DataType.LOG
                loadFragment(DataFragment())
                result = true
            }
            R.id.fuel_menu_item -> {
                updateToolbarMenuState()
                type = DataType.FUEL
                loadFragment(DataFragment())
                result = true
            }
            R.id.settings_menu_item -> {
                toolbar.menu.findItem(R.id.period_select_menu_date).isVisible = false
                periodCalendar = Calendar.getInstance()
                loadFragment(SettingsFragment())
                result = true
            }
        }
        return result
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frame_container, fragment)
            .commit()
    }

    fun loadFragment() {
        when (bottomNavigationView.selectedItemId) {
            R.id.log_menu_item -> loadFragment(DataFragment())
            R.id.fuel_menu_item -> loadFragment(DataFragment())
            R.id.settings_menu_item -> loadFragment(SettingsFragment())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null && requestCode == PERIOD_DIALOG_RESULT) {
            periodSelector.updateYear(data.getStringExtra("year"))
        }
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat?,
        pref: Preference
    ): Boolean {
        val fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, pref.fragment)
        fragment.arguments = pref.extras
        fragment.setTargetFragment(caller, 0)
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frame_container, fragment)
            .addToBackStack(null)
            .commit()

        return true
    }
}