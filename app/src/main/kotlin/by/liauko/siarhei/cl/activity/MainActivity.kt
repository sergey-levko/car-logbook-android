package by.liauko.siarhei.cl.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.activity.dialog.PeriodSelectorDialog
import by.liauko.siarhei.cl.activity.fragment.DataFragment
import by.liauko.siarhei.cl.activity.fragment.SettingsFragment
import by.liauko.siarhei.cl.database.CarLogbookDatabase
import by.liauko.siarhei.cl.databinding.ActivityMainBinding
import by.liauko.siarhei.cl.job.ExportToPdfAsyncJob
import by.liauko.siarhei.cl.util.AppResultCodes.CAR_PROFILE_SHOW_LIST
import by.liauko.siarhei.cl.util.AppResultCodes.CAR_PROFILE_WELCOME
import by.liauko.siarhei.cl.util.AppResultCodes.LOG_EXPORT
import by.liauko.siarhei.cl.util.AppResultCodes.PERIOD_DIALOG_RESULT
import by.liauko.siarhei.cl.util.ApplicationUtil.EMPTY_STRING
import by.liauko.siarhei.cl.util.ApplicationUtil.dataPeriod
import by.liauko.siarhei.cl.util.ApplicationUtil.periodCalendar
import by.liauko.siarhei.cl.util.ApplicationUtil.profileId
import by.liauko.siarhei.cl.util.ApplicationUtil.profileName
import by.liauko.siarhei.cl.util.ApplicationUtil.type
import by.liauko.siarhei.cl.util.DataPeriod
import by.liauko.siarhei.cl.util.DataType
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        val preferences = getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE)

        val job = lifecycleScope.launch {
            type = DataType.valueOf(preferences.getString(getString(R.string.main_screen_key), "LOG") ?: "LOG")
            dataPeriod = DataPeriod.valueOf(preferences.getString(getString(R.string.period_key), "MONTH") ?: "MONTH")

            profileId = preferences.getLong(getString(R.string.car_profile_id_key), -1L)
            profileName = preferences.getString(getString(R.string.car_profile_name_key), getString(R.string.app_name)) ?: EMPTY_STRING

            initToolbar()
            initBottomNavigationView()
        }
        val content = findViewById<View>(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw() =
                    if (job.isCompleted) {
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else {
                        false
                    }
            }
        )
    }

    private fun initToolbar() {
        viewBinding.toolbar.inflateMenu(R.menu.menu_main_toolbar)
        viewBinding.toolbar.setOnMenuItemClickListener {
            var result = false
            when (it.itemId) {
                R.id.period_select_menu_date -> {
                    val dialog = PeriodSelectorDialog(this)
                    dialog.setOnDismissListener { loadFragment() }
                    dialog.show()
                    result = true
                }
                R.id.car_profile_menu -> {
                    startActivityForResult(Intent(applicationContext, CarProfilesActivity::class.java), CAR_PROFILE_SHOW_LIST)
                    result = true
                }
                R.id.export_to_pdf -> {
                    startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), LOG_EXPORT)
                    result = true
                }
            }

            return@setOnMenuItemClickListener result
        }
    }

    private fun initBottomNavigationView() {
        viewBinding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.log_menu_item -> {
                    type = DataType.LOG
                    loadDataFragment()
                    true
                }
                R.id.fuel_menu_item -> {
                    type = DataType.FUEL
                    loadDataFragment()
                    true
                }
                R.id.settings_menu_item -> {
                    periodCalendar = Calendar.getInstance()
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
        viewBinding.bottomNavigationView.selectedItemId = when (type) {
            DataType.LOG -> R.id.log_menu_item
            DataType.FUEL -> R.id.fuel_menu_item
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("item_id", viewBinding.bottomNavigationView.selectedItemId)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        viewBinding.bottomNavigationView.selectedItemId = savedInstanceState.getInt("item_id")
    }

    override fun onDestroy() {
        super.onDestroy()
        CarLogbookDatabase.closeDatabase()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frame_container, fragment)
            .commit()
    }

    private fun loadFragment() {
        when (viewBinding.bottomNavigationView.selectedItemId) {
            R.id.log_menu_item -> loadDataFragment()
            R.id.fuel_menu_item -> loadDataFragment()
            R.id.settings_menu_item -> loadFragment(SettingsFragment())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PERIOD_DIALOG_RESULT -> loadFragment()
                CAR_PROFILE_SHOW_LIST -> loadFragment()
                CAR_PROFILE_WELCOME -> loadFragment()
                LOG_EXPORT -> ExportToPdfAsyncJob(
                    this,
                    data?.data ?: Uri.EMPTY
                ).execute()
            }
        } else if (resultCode == RESULT_CANCELED && requestCode == CAR_PROFILE_SHOW_LIST) {
            loadFragment()
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

    private fun loadDataFragment() {
        if (profileId != -1L) {
            updateToolbar()
            loadFragment(DataFragment())
        } else {
            startActivityForResult(Intent(applicationContext, WelcomeActivity::class.java), CAR_PROFILE_WELCOME)
        }
    }

    private fun updateToolbar() {
        viewBinding.toolbar.title = profileName
        viewBinding.toolbar.menu.findItem(R.id.period_select_menu_date).isVisible = when (dataPeriod) {
            DataPeriod.ALL -> false
            else -> true
        }
        viewBinding.toolbar.menu.findItem(R.id.car_profile_menu).isVisible = true
        viewBinding.toolbar.menu.findItem(R.id.export_to_pdf).isVisible = when (type) {
            DataType.LOG -> true
            DataType.FUEL -> false
        }
    }
}
