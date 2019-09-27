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
import by.liauko.siarhei.fcc.util.ApplicationUtil.type
import by.liauko.siarhei.fcc.util.DataType
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val preferences =  getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE)
        type = DataType.valueOf(preferences.getString("type", "LOG")!!)

        setSupportActionBar(findViewById(R.id.toolbar))

        bottomNavigationView = findViewById(R.id.bottom_navigation_view)
        bottomNavigationView.setOnNavigationItemSelectedListener(this)
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
        outState.putSerializable("type", type)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        type = savedInstanceState!!.getSerializable("type") as DataType
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