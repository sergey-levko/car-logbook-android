package by.liauko.siarhei.cl.activity

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.repository.FuelConsumptionRepository
import by.liauko.siarhei.cl.util.DateConverter
import by.liauko.siarhei.cl.viewmodel.FuelDataViewModel
import by.liauko.siarhei.cl.viewmodel.factory.LastMileageViewModelFactory
import java.util.Calendar
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.MONTH
import java.util.Calendar.YEAR

class FuelDataActivity : AppCompatActivity(),
    DatePickerDialog.OnDateSetListener {

    private val defaultId = -1L

    private lateinit var model: FuelDataViewModel

    private lateinit var litres: EditText
    private lateinit var mileage: EditText
    private lateinit var distance: EditText
    private lateinit var date: EditText
    private lateinit var calendar: Calendar

    private var id = defaultId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fuel_data)

        val modelFactory = LastMileageViewModelFactory(FuelConsumptionRepository(applicationContext))
        model = modelFactory.create(FuelDataViewModel::class.java)
        model.loadLastMileage()
        model.mileage.observe(this) {
            mileage.setText(if (it != 0) it.toString() else "")
        }
        model.distance.observe(this) {
            distance.setText(if (it != 0.0) it.toString() else "")
        }

        id = intent.getLongExtra("id", defaultId)

        calendar = Calendar.getInstance()
        initToolbar()
        initElements()
        if (id != defaultId) {
            fillData()
        }
        updateDateButtonText()
    }

    private fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.fuel_toolbar)
        toolbar.setTitle(intent.getIntExtra("title", R.string.activity_fuel_title_add))
        toolbar.setNavigationIcon(R.drawable.arrow_left_white)
        toolbar.setNavigationOnClickListener {
            handleBackAction()
        }
        toolbar.inflateMenu(R.menu.data_activity_menu)
        toolbar.setOnMenuItemClickListener {
            var result = false
            when (it.itemId) {
                R.id.data_menu_save -> {
                    if (validateFields()) {
                        val intent = Intent()
                        fillIntent(intent)
                        setResult(RESULT_OK, intent)
                        finish()
                        result = true
                    }
                }
                R.id.data_menu_delete -> {
                    val intent = Intent()
                    intent.putExtra("remove", true)
                    intent.putExtra("id", id)
                    setResult(RESULT_OK, intent)
                    finish()
                    result = true
                }
            }

            return@setOnMenuItemClickListener result
        }
        if (id == defaultId) {
            toolbar.menu.findItem(R.id.data_menu_save).isVisible = true
            toolbar.menu.findItem(R.id.data_menu_delete).isVisible = false
        } else {
            toolbar.menu.findItem(R.id.data_menu_save).isVisible = false
            toolbar.menu.findItem(R.id.data_menu_delete).isVisible = true
        }
    }

    private fun initElements() {
        litres = findViewById(R.id.litres)
        mileage = findViewById(R.id.fuel_mileage)
        distance = findViewById(R.id.distance)
        mileage.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && distance.text.toString().isBlank()) {
                model.handleMileageChange(mileage.text.toString())
            }
        }
        distance.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && mileage.text.toString().isBlank()) {
                model.handleDistanceChange(distance.text.toString())
            }
        }

        date = findViewById(R.id.fuel_date)
        date.inputType = InputType.TYPE_NULL
        date.setOnClickListener { showDatePickerDialog(it) }
        date.setOnFocusChangeListener { view, isFocused ->
            if (isFocused) {
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(view.windowToken, 0)
                showDatePickerDialog(view)
            }
        }
    }

    private fun showDatePickerDialog(view: View) {
        DatePickerDialog(
            view.context,
            R.style.Theme_App_DatePicker,
            this,
            calendar.get(YEAR),
            calendar.get(MONTH),
            calendar.get(DAY_OF_MONTH)
        ).show()
    }

    private fun fillData() {
        model.mileage.postValue(intent.getIntExtra("mileage", 0))
        model.distance.postValue(intent.getDoubleExtra("distance", 0.0))

        litres.text.append(intent.getDoubleExtra("litres", 0.0).toString())
        calendar.timeInMillis = intent.getLongExtra("time", calendar.timeInMillis)
    }

    private fun validateFields(): Boolean {
        var result = true

        if (litres.text.isNullOrEmpty() || litres.text.toString().toDouble() == 0.0) {
            litres.error = getString(R.string.activity_fuel_volume_parameter_error)
            result = false
        }

        if (distance.text.isNullOrEmpty() || distance.text.toString().toDouble() == 0.0) {
            distance.error = getString(R.string.activity_fuel_distance_parameter_error)
            result = false
        }

        if (mileage.text.isNullOrEmpty()) {
            mileage.error = getString(R.string.activity_fuel_mileage_error)
            result = false
        }

        return result
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        calendar.set(YEAR, year)
        calendar.set(MONTH, month)
        calendar.set(DAY_OF_MONTH, dayOfMonth)
        updateDateButtonText()
    }

    override fun onBackPressed() {
        handleBackAction()
    }

    private fun updateDateButtonText() {
        date.text.clear()
        date.text.append(DateConverter.convert(calendar))
    }

    private fun handleBackAction() {
        if (id != defaultId) {
            if (validateFields()) {
                val intent = Intent()
                intent.putExtra("id", id)
                fillIntent(intent)
                setResult(RESULT_OK, intent)
                finish()
            }
        } else {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun fillIntent(intent: Intent) {
        intent.putExtra("litres", litres.text.toString())
        intent.putExtra("mileage", mileage.text.toString())
        intent.putExtra("distance", distance.text.toString())
        intent.putExtra("time", calendar.timeInMillis)
    }
}
