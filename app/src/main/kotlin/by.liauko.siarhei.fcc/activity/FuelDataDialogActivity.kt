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
import java.util.Calendar
import java.util.Calendar.YEAR
import java.util.Calendar.MONTH
import java.util.Calendar.DAY_OF_MONTH

class FuelDataDialogActivity : AppCompatActivity(), View.OnClickListener, DatePickerDialog.OnDateSetListener {
    private lateinit var litres: EditText
    private lateinit var distance: EditText
    private lateinit var dateButton: Button
    private lateinit var calendar: Calendar

    private var id = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fuel_data)
        val parameters = window.attributes
        parameters.width = WindowManager.LayoutParams.MATCH_PARENT
        window.attributes = parameters

        setTitle(intent.getIntExtra("title", R.string.data_dialog_title_add))
        calendar = Calendar.getInstance()
        initElements()

        id = intent.getLongExtra("id", -1L)
        if (id != -1L) {
            fillData()
        }
        updateDateButtonText()
    }

    private fun initElements() {
        litres = findViewById(R.id.litres)
        distance = findViewById(R.id.distance)
        dateButton = findViewById(R.id.date_button)
        dateButton.setOnClickListener(this)

        val positiveButton = findViewById<Button>(R.id.positive_button)
        positiveButton.setOnClickListener(this)
        positiveButton.setText(intent.getIntExtra("positive_button", R.string.data_dialog_positive_button_add))

        val negativeButton = findViewById<Button>(R.id.negative_button)
        negativeButton.setOnClickListener(this)
    }

    private fun fillData() {
        litres.text.append(intent.getDoubleExtra("litres", 0.0).toString())
        distance.text.append(intent.getDoubleExtra("distance", 0.0).toString())
        calendar.timeInMillis = intent.getLongExtra("time", calendar.timeInMillis)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.date_button -> {
                    DatePickerDialog(this@FuelDataDialogActivity, this,
                        calendar.get(YEAR), calendar.get(MONTH), calendar.get(DAY_OF_MONTH))
                        .show()
                }
                R.id.positive_button -> {
                    val intent = Intent()
                    intent.putExtra("id", id)
                    intent.putExtra("litres", litres.text.toString())
                    intent.putExtra("distance", distance.text.toString())
                    intent.putExtra("time", calendar.timeInMillis)
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
