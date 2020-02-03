package by.liauko.siarhei.fcc.activity

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
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.MONTH
import java.util.Calendar.YEAR

class FuelDataDialogActivity : AppCompatActivity(), View.OnClickListener, DatePickerDialog.OnDateSetListener {

    private val defaultId = -1L

    private lateinit var litres: EditText
    private lateinit var distance: EditText
    private lateinit var dateButton: Button
    private lateinit var calendar: Calendar

    private var id = defaultId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fuel_data)
        val parameters = window.attributes
        parameters.width = WindowManager.LayoutParams.MATCH_PARENT
        window.attributes = parameters

        setTitle(intent.getIntExtra("title", R.string.data_dialog_title_add))
        calendar = Calendar.getInstance()
        initElements()

        id = intent.getLongExtra("id", defaultId)
        if (id != defaultId) {
            fillData()
        }
        updateDateButtonText()
    }

    private fun initElements() {
        litres = findViewById(R.id.litres)
        distance = findViewById(R.id.distance)
        dateButton = findViewById(R.id.fuel_date)
        dateButton.setOnClickListener(this)

        val positiveButton = findViewById<Button>(R.id.fuel_dialog_positive_button)
        positiveButton.setOnClickListener(this)
        positiveButton.setText(intent.getIntExtra("positive_button", R.string.data_dialog_positive_button_add))

        findViewById<Button>(R.id.fuel_dialog_negative_button).setOnClickListener(this)
    }

    private fun fillData() {
        litres.text.append(intent.getDoubleExtra("litres", 0.0).toString())
        distance.text.append(intent.getDoubleExtra("distance", 0.0).toString())
        calendar.timeInMillis = intent.getLongExtra("time", calendar.timeInMillis)
    }

    private fun validateFields(): Boolean {
        var result = true

        if (litres.text.isNullOrEmpty() || litres.text.toString().toDouble() == 0.0) {
            litres.error = getString(R.string.data_dialog_volume_parameter_error)
            result = false
        }

        if (distance.text.isNullOrEmpty() || distance.text.toString().toDouble() == 0.0) {
            distance.error = getString(R.string.data_dialog_distance_parameter_error)
            result = false
        }

        return result
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.fuel_date -> {
                    DatePickerDialog(
                        this@FuelDataDialogActivity,
                        this,
                        calendar.get(YEAR),
                        calendar.get(MONTH),
                        calendar.get(DAY_OF_MONTH)
                    ).show()
                }
                R.id.fuel_dialog_positive_button -> {
                    if (validateFields()) {
                        val intent = Intent()
                        intent.putExtra("id", id)
                        intent.putExtra("litres", litres.text.toString())
                        intent.putExtra("distance", distance.text.toString())
                        intent.putExtra("time", calendar.timeInMillis)
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                }
                R.id.fuel_dialog_negative_button -> {
                    setResult(RESULT_CANCELED)
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
