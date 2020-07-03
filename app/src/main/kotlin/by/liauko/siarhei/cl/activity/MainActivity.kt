package by.liauko.siarhei.cl.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.activity.element.PeriodSelectorElement
import by.liauko.siarhei.cl.activity.fragment.DataFragment
import by.liauko.siarhei.cl.activity.fragment.SettingsFragment
import by.liauko.siarhei.cl.database.CarLogDatabase
import by.liauko.siarhei.cl.util.AppResultCodes.CAR_PROFILE_SHOW_LIST
import by.liauko.siarhei.cl.util.AppResultCodes.PERIOD_DIALOG_RESULT
import by.liauko.siarhei.cl.util.ApplicationUtil.dataPeriod
import by.liauko.siarhei.cl.util.ApplicationUtil.periodCalendar
import by.liauko.siarhei.cl.util.ApplicationUtil.profileId
import by.liauko.siarhei.cl.util.ApplicationUtil.profileName
import by.liauko.siarhei.cl.util.ApplicationUtil.type
import by.liauko.siarhei.cl.util.DataPeriod
import by.liauko.siarhei.cl.util.DataType
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
        dataPeriod = DataPeriod.valueOf(preferences.getString(getString(R.string.period_key), "MONTH") ?: "MONTH")
        val defaultUiMode = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P)
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
        else
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        val uiMode = preferences.getInt(getString(R.string.dark_mode_key), defaultUiMode)
        AppCompatDelegate.setDefaultNightMode(uiMode)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        profileId = preferences.getLong(getString(R.string.car_profile_id_key), -1L)
        profileName = preferences.getString(getString(R.string.car_profile_name_key), getString(R.string.app_name))
        if (profileId == -1L) {
            //TODO: show activity for creation or importing car profile
        }

        periodSelector = PeriodSelectorElement(this, findViewById(R.id.main_coordinator_layout))
        initToolbar()
        initBottomNavigationView()
    }

    private fun initToolbar() {
        toolbar = findViewById(R.id.toolbar)
        toolbar.title = profileName ?: getString(R.string.app_name)
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
                R.id.car_profile_menu -> {
                    startActivityForResult(Intent(applicationContext, CarProfilesActivity::class.java), CAR_PROFILE_SHOW_LIST)
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
        toolbar.menu.findItem(R.id.car_profile_menu).isVisible = true
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
        if (savedInstanceState != null) {
            super.onRestoreInstanceState(savedInstanceState)
        }
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
                toolbar.menu.findItem(R.id.car_profile_menu).isVisible = false
                periodCalendar = Calendar.getInstance()
                loadFragment(SettingsFragment())
                result = true
            }
        }
        return result
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
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