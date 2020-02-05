package by.liauko.siarhei.cl.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.util.DateConverter
import java.util.Calendar
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.MONTH
import java.util.Calendar.YEAR

class LogDataActivity : AppCompatActivity(), View.OnClickListener, DatePickerDialog.OnDateSetListener {

    private val defaultId = -1L

    private lateinit var calendar: Calendar
    private lateinit var title: EditText
    private lateinit var text: EditText
    private lateinit var mileage: EditText
    private lateinit var date: Button
    private lateinit var toolbar: Toolbar

    private var id = defaultId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_data)

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
        toolbar = findViewById(R.id.log_toolbar)
        toolbar.setTitle(intent.getIntExtra("title", R.string.activity_log_title_add))
        toolbar.setNavigationIcon(R.drawable.arrow_left_white)
        toolbar.setNavigationOnClickListener {
            handleBackAction()
        }
        toolbar.inflateMenu(R.menu.log_menu_save)
        toolbar.setOnMenuItemClickListener {
            var result = false
            when (it.itemId) {
                R.id.log_menu_save -> {
                    if (validateFields()) {
                        val intent = Intent()
                        intent.putExtra("title", title.text.toString())
                        intent.putExtra("mileage", mileage.text.toString())
                        intent.putExtra("text", text.text.toString())
                        intent.putExtra("time", calendar.timeInMillis)
                        setResult(RESULT_OK, intent)
                        finish()
                        result = true
                    }
                }
                R.id.log_menu_delete -> {
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
            toolbar.menu.findItem(R.id.log_menu_save).isVisible = true
            toolbar.menu.findItem(R.id.log_menu_delete).isVisible = false
        } else {
            toolbar.menu.findItem(R.id.log_menu_save).isVisible = false
            toolbar.menu.findItem(R.id.log_menu_delete).isVisible = true
        }
    }

    private fun initElements() {
        title = findViewById(R.id.log_title)
        text = findViewById(R.id.log_text)
        mileage = findViewById(R.id.log_mileage)
        date = findViewById(R.id.log_date)
        date.setOnClickListener(this)
    }

    private fun updateDateButtonText() {
        date.text = DateConverter.convert(calendar)
    }

    private fun fillData() {
        calendar.timeInMillis = intent.getLongExtra("time", calendar.timeInMillis)
        title.text.append(intent.getStringExtra("log_title"))
        mileage.text.append(intent.getLongExtra("mileage", 0L).toString())
        text.text.append(intent.getStringExtra("text"))
    }

    private fun validateFields(): Boolean {
        var result = true

        val text = title.text.trim()
        if (text.isEmpty()) {
            title.error = getString(R.string.activity_log_title_error)
            result = false
        }

        if (mileage.text.isNullOrEmpty() || mileage.text.toString().toLong() == 0L) {
            mileage.error = getString(R.string.activity_log_mileage_error)
            result = false
        }

        return result
    }

    private fun handleBackAction() {
        if (id != defaultId) {
            if (validateFields()) {
                val intent = Intent()
                intent.putExtra("id", id)
                intent.putExtra("title", title.text.toString())
                intent.putExtra("mileage", mileage.text.toString())
                intent.putExtra("text", text.text.toString())
                intent.putExtra("time", calendar.timeInMillis)
                setResult(RESULT_OK, intent)
                finish()
            }
        } else {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    override fun onClick(v: View?) {
        if (v != null && v.id == R.id.log_date) {
            DatePickerDialog(
                this@LogDataActivity,
                R.style.DatePickerDialog,
                this,
                calendar.get(YEAR),
                calendar.get(MONTH),
                calendar.get(DAY_OF_MONTH)
            ).show()
        }
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
}
