package by.liauko.siarhei.cl.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.databinding.ActivityFuelDataBinding
import by.liauko.siarhei.cl.repository.FuelConsumptionRepository
import by.liauko.siarhei.cl.util.DateConverter
import by.liauko.siarhei.cl.viewmodel.FuelDataViewModel
import by.liauko.siarhei.cl.viewmodel.factory.LastMileageViewModelFactory
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.Calendar

class FuelDataActivity : AppCompatActivity() {

    private val defaultId = -1L

    private lateinit var viewModel: FuelDataViewModel
    private lateinit var viewBinding: ActivityFuelDataBinding
    private lateinit var calendar: Calendar

    private var id = defaultId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityFuelDataBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        val modelFactory = LastMileageViewModelFactory(FuelConsumptionRepository(applicationContext))
        viewModel = modelFactory.create(FuelDataViewModel::class.java)
        viewModel.loadLastMileage()
        viewModel.mileage.observe(this) {
            viewBinding.fuelMileage.setText(if (it != 0) it.toString() else "")
        }
        viewModel.distance.observe(this) {
            viewBinding.distance.setText(if (it != 0.0) it.toString() else "")
        }

        id = intent.getLongExtra("id", defaultId)

        calendar = Calendar.getInstance()
        initToolbar()
        initElements()
        if (id != defaultId) {
            fillData()
        }
        updateDateText()
    }

    private fun initToolbar() {
        viewBinding.fuelToolbar.setTitle(intent.getIntExtra("title", R.string.activity_data_title_add))
        viewBinding.fuelToolbar.setNavigationIcon(R.drawable.arrow_left_white)
        viewBinding.fuelToolbar.setNavigationContentDescription(R.string.back_button_content_descriptor)
        viewBinding.fuelToolbar.setNavigationOnClickListener {
            handleBackAction()
        }
        viewBinding.fuelToolbar.inflateMenu(R.menu.menu_data_activity)
        viewBinding.fuelToolbar.setOnMenuItemClickListener {
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
            viewBinding.fuelToolbar.menu.findItem(R.id.data_menu_save).isVisible = true
            viewBinding.fuelToolbar.menu.findItem(R.id.data_menu_delete).isVisible = false
        } else {
            viewBinding.fuelToolbar.menu.findItem(R.id.data_menu_save).isVisible = false
            viewBinding.fuelToolbar.menu.findItem(R.id.data_menu_delete).isVisible = true
        }
    }

    private fun initElements() {
        viewBinding.fuelMileage.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && viewBinding.distance.text.toString().isBlank()) {
                viewModel.handleMileageChange(viewBinding.fuelMileage.text.toString())
            }
        }
        viewBinding.distance.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && viewBinding.fuelMileage.text.toString().isBlank()) {
                viewModel.handleDistanceChange(viewBinding.distance.text.toString())
            }
        }

        viewBinding.fuelDate.inputType = InputType.TYPE_NULL
        viewBinding.fuelDate.setOnClickListener { showDatePickerDialog() }
        viewBinding.fuelDate.setOnFocusChangeListener { view, isFocused ->
            if (isFocused) {
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(view.windowToken, 0)
                showDatePickerDialog()
            }
        }
    }

    private fun showDatePickerDialog() {
        val dialogPicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.dialog_date_picker_title))
            .setSelection(calendar.timeInMillis)
            .setTheme(R.style.Theme_App_DatePicker)
            .build()
        dialogPicker.addOnPositiveButtonClickListener {
            calendar.timeInMillis = it
            updateDateText()
        }
        dialogPicker.show(supportFragmentManager, null)
    }

    private fun fillData() {
        viewModel.mileage.postValue(intent.getIntExtra("mileage", 0))
        viewModel.distance.postValue(intent.getDoubleExtra("distance", 0.0))

        viewBinding.litres.text?.append(intent.getDoubleExtra("litres", 0.0).toString())
        calendar.timeInMillis = intent.getLongExtra("time", calendar.timeInMillis)
    }

    private fun validateFields(): Boolean {
        var result = true

        if (viewBinding.litres.text.isNullOrEmpty() || viewBinding.litres.text.toString().toDouble() == 0.0) {
            viewBinding.litres.error = getString(R.string.activity_fuel_volume_parameter_error)
            result = false
        }

        if (viewBinding.distance.text.isNullOrEmpty() || viewBinding.distance.text.toString().toDouble() == 0.0) {
            viewBinding.distance.error = getString(R.string.activity_fuel_distance_parameter_error)
            result = false
        }

        if (viewBinding.fuelMileage.text.isNullOrEmpty()) {
            viewBinding.fuelMileage.error = getString(R.string.activity_fuel_mileage_error)
            result = false
        }

        return result
    }

    override fun onBackPressed() {
        handleBackAction()
    }

    private fun updateDateText() {
        viewBinding.fuelDate.text?.clear()
        viewBinding.fuelDate.text?.append(DateConverter.convert(calendar))
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
        intent.putExtra("litres", viewBinding.litres.text.toString())
        intent.putExtra("mileage", viewBinding.fuelMileage.text.toString())
        intent.putExtra("distance", viewBinding.distance.text.toString())
        intent.putExtra("time", calendar.timeInMillis)
    }
}
