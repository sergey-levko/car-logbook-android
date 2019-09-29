package by.liauko.siarhei.fcc.activity

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import by.liauko.siarhei.fcc.R
import by.liauko.siarhei.fcc.database.CarLogDatabase
import by.liauko.siarhei.fcc.fragment.DataFragment
import by.liauko.siarhei.fcc.fragment.SettingsFragment
import by.liauko.siarhei.fcc.util.AppTheme
import by.liauko.siarhei.fcc.util.ApplicationUtil.appTheme
import by.liauko.siarhei.fcc.util.ApplicationUtil.type
import by.liauko.siarhei.fcc.util.DataType
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        val preferences =  getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE)
        type = DataType.valueOf(preferences.getString(getString(R.string.main_screen_key), "LOG")!!)
        appTheme = AppTheme.valueOf(preferences.getString(getString(R.string.theme_key), "KITTY")!!)
        setTheme(appTheme.appId)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        setSupportActionBar(findViewById(R.id.toolbar))

        bottomNavigationView = findViewById(R.id.bottom_navigation_view)
        bottomNavigationView.setOnNavigationItemSelectedListener(this)
        bottomNavigationView.selectedItemId = when (type) {
            DataType.LOG -> R.id.log_menu_item
            DataType.FUEL -> R.id.fuel_menu_item
        }
    }

    override fun onResume() {
        super.onResume()
        when (bottomNavigationView.selectedItemId) {
            R.id.log_menu_item -> loadFragment(DataFragment(), R.string.data_fragment_log_title)
            R.id.fuel_menu_item -> loadFragment(DataFragment(), R.string.data_fragment_fuel_title)
            R.id.settings_menu_item -> loadFragment(SettingsFragment(), R.string.settings_fragment_title)
        }
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

        when (item.itemId) {
            R.id.log_menu_item -> {
                type = DataType.LOG
                loadFragment(DataFragment(), R.string.data_fragment_log_title)
                result = true
            }
            R.id.fuel_menu_item -> {
                type = DataType.FUEL
                loadFragment(DataFragment(), R.string.data_fragment_fuel_title)
                result = true
            }
            R.id.settings_menu_item -> {
                loadFragment(SettingsFragment(), R.string.settings_fragment_title)
                result = true
            }
        }
        return result
    }

    private fun loadFragment(fragment: Fragment, titleId: Int) {
        setTitle(titleId)
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frame_container, fragment)
            .commit()
    }
}