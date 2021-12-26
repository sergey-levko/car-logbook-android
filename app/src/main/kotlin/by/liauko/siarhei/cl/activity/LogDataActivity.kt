package by.liauko.siarhei.cl.activity

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.databinding.ActivityLogDataBinding
import by.liauko.siarhei.cl.util.DateConverter
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.Calendar
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.MONTH
import java.util.Calendar.YEAR

class LogDataActivity : AppCompatActivity(),
    DatePickerDialog.OnDateSetListener {

    private val defaultId = -1L

    private lateinit var calendar: Calendar
    private lateinit var viewBinding: ActivityLogDataBinding

    private var id = defaultId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityLogDataBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

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
        viewBinding.logToolbar.setTitle(intent.getIntExtra("title", R.string.activity_data_title_add))
        viewBinding.logToolbar.setNavigationIcon(R.drawable.arrow_left_white)
        viewBinding.logToolbar.setNavigationContentDescription(R.string.back_button_content_descriptor)
        viewBinding.logToolbar.setNavigationOnClickListener {
            handleBackAction()
        }
        viewBinding.logToolbar.inflateMenu(R.menu.menu_data_activity)
        viewBinding.logToolbar.setOnMenuItemClickListener {
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
            viewBinding.logToolbar.menu.findItem(R.id.data_menu_save).isVisible = true
            viewBinding.logToolbar.menu.findItem(R.id.data_menu_delete).isVisible = false
        } else {
            viewBinding.logToolbar.menu.findItem(R.id.data_menu_save).isVisible = false
            viewBinding.logToolbar.menu.findItem(R.id.data_menu_delete).isVisible = true
        }
    }

    private fun initElements() {
        viewBinding.logDate.inputType = InputType.TYPE_NULL
        viewBinding.logDate.setOnClickListener { showDatePickerDialog() }
        viewBinding.logDate.setOnFocusChangeListener { view, isFocused ->
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

    private fun updateDateText() {
        viewBinding.logDate.text?.clear()
        viewBinding.logDate.text?.append(DateConverter.convert(calendar))
    }

    private fun fillData() {
        calendar.timeInMillis = intent.getLongExtra("time", calendar.timeInMillis)
        viewBinding.logTitle.text?.append(intent.getStringExtra("log_title"))
        viewBinding.logMileage.text?.append(intent.getLongExtra("mileage", 0L).toString())
        viewBinding.logText.text?.append(intent.getStringExtra("text"))
    }

    private fun validateFields(): Boolean {
        var result = true

        if (viewBinding.logTitle.text.isNullOrBlank()) {
            viewBinding.logTitle.error = getString(R.string.activity_log_title_error)
            result = false
        }

        if (viewBinding.logMileage.text.isNullOrBlank() || viewBinding.logMileage.text.toString().toLong() == 0L) {
            viewBinding.logMileage.error = getString(R.string.activity_log_mileage_error)
            result = false
        }

        return result
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
        intent.putExtra("title", viewBinding.logTitle.text.toString())
        intent.putExtra("mileage", viewBinding.logMileage.text.toString())
        intent.putExtra("text", viewBinding.logText.text.toString())
        intent.putExtra("time", calendar.timeInMillis)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        calendar.set(YEAR, year)
        calendar.set(MONTH, month)
        calendar.set(DAY_OF_MONTH, dayOfMonth)
        updateDateText()
    }

    override fun onBackPressed() {
        handleBackAction()
    }
}
