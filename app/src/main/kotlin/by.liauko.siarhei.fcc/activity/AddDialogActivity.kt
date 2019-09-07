package by.liauko.siarhei.fcc.activity

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import by.liauko.siarhei.fcc.R
import by.liauko.siarhei.fcc.util.DateConverter
import java.util.*
import java.util.Calendar.*

class AddDialogActivity : AppCompatActivity(), View.OnClickListener, DatePickerDialog.OnDateSetListener {
    lateinit var fuel: EditText
    lateinit var distance: EditText
    lateinit var dateButton: Button
    lateinit var calendar: Calendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
        val parameters = window.attributes
        parameters.width = WindowManager.LayoutParams.MATCH_PARENT
        window.attributes = parameters

        setTitle(R.string.add_dialog_title)
        initDialogButtons()
        fuel = findViewById(R.id.fuel)
        distance = findViewById(R.id.distance)
        calendar = getInstance()
        dateButton = findViewById(R.id.date_button)
        updateDateButtonText()
        dateButton.setOnClickListener(this)
    }

    private fun initDialogButtons() {
        val positiveButton = findViewById<Button>(R.id.positive_button)
        positiveButton.setOnClickListener(this)

        val negativeButton = findViewById<Button>(R.id.negative_button)
        negativeButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.date_button -> {
                    DatePickerDialog(this@AddDialogActivity, this,
                        calendar.get(YEAR), calendar.get(MONTH), calendar.get(DAY_OF_MONTH))
                        .show()
                }
                R.id.positive_button -> {
                    val intent = Intent()
                    intent.putExtra("litres", fuel.text.toString())
                    intent.putExtra("distance", distance.text.toString())
                    intent.putExtra("year", calendar[YEAR])
                    intent.putExtra("month", calendar[MONTH])
                    intent.putExtra("day", calendar[DAY_OF_MONTH])
                    setResult(RESULT_OK, intent)
                    finish()
                }
                R.id.negative_button -> {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
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

    private fun updateDateButtonText() {
        dateButton.text = DateConverter.convert(calendar)
    }
}
