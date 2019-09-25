package by.liauko.siarhei.fcc.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import by.liauko.siarhei.fcc.R
import by.liauko.siarhei.fcc.util.DateConverter
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
            if (id != defaultId) {
                intent.putExtra("id", id)
                intent.putExtra("title", title.text.toString())
                intent.putExtra("mileage", mileage.text.toString())
                intent.putExtra("text", text.text.toString())
                intent.putExtra("time", calendar.timeInMillis)
                setResult(RESULT_OK, intent)
                finish()
            } else {
                setResult(RESULT_CANCELED)
                finish()
            }
        }
        if (id == defaultId) {
            toolbar.inflateMenu(R.menu.log_menu_save)
            toolbar.setOnMenuItemClickListener {
                var result = false
                when (it.itemId) {
                    R.id.log_menu_save -> {
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

                return@setOnMenuItemClickListener result
            }
        }
    }

    private fun initElements() {
        title = findViewById(R.id.log_title)
        text = findViewById(R.id.log_text)
        mileage = findViewById(R.id.log_mileage)
        date = findViewById(R.id.log_date)
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

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.log_date -> {
                    DatePickerDialog(this@LogDataActivity, this,
                        calendar.get(YEAR), calendar.get(MONTH), calendar.get(DAY_OF_MONTH))
                        .show()
                }
            }
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        calendar.set(YEAR, year)
        calendar.set(MONTH, month)
        calendar.set(DAY_OF_MONTH, dayOfMonth)
        updateDateButtonText()
    }
}
