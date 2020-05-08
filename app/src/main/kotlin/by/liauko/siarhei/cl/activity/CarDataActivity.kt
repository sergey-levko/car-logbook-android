package by.liauko.siarhei.cl.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.util.CarBodyType
import by.liauko.siarhei.cl.util.CarFuelType
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout

class CarDataActivity : AppCompatActivity() {

    private val defaultId = -1L

    private lateinit var bodyTypes: Array<String>
    private lateinit var fuelTypes: Array<String>
    private lateinit var name: EditText
    private lateinit var bodyType: AutoCompleteTextView
    private lateinit var fuelType: AutoCompleteTextView
    private lateinit var engineVolume: EditText
    private lateinit var engineVolumeLayout: TextInputLayout
    private lateinit var toolbar: Toolbar
    private lateinit var carInfo: CarInfo

    private var id = defaultId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_data)

        bodyTypes = resources.getStringArray(R.array.body_types)
        fuelTypes = resources.getStringArray(R.array.fuel_type)

        id = intent.getLongExtra("id", defaultId)

        initToolbar()
        initElements()
        initCarInfo()
    }

    private fun initToolbar() {
        toolbar = findViewById(R.id.car_toolbar)
        toolbar.setTitle(if (id == defaultId) R.string.activity_car_title_create else R.string.activity_car_title_edit)
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
                    MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.dialog_delete_car_title)
                        .setMessage(R.string.dialog_delete_car_message)
                        .setPositiveButton(R.string.yes) { dialog, _ ->
                            val intent = Intent()
                            intent.putExtra("remove", true)
                            intent.putExtra("id", id)
                            setResult(RESULT_OK, intent)
                            dialog.dismiss()
                            finish()
                            result = true
                            Toast.makeText(this, R.string.dialog_delete_car_toast_message, Toast.LENGTH_LONG)
                                .show();
                        }
                        .setNegativeButton(R.string.no) { dialog, _ -> dialog.cancel() }
                        .show()
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
        val bodyAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, bodyTypes)
        val fuelAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, fuelTypes)

        name = findViewById(R.id.car_name)

        bodyType = findViewById(R.id.car_body)
        bodyType.text.append(bodyTypes.first())
        bodyType.inputType = InputType.TYPE_NULL
        bodyType.setAdapter(bodyAdapter)
        bodyType.setOnFocusChangeListener { view, _ -> hideKeyboard(view) }
        bodyType.setOnItemClickListener { _, _, i, _ -> carInfo.body = CarBodyType.values()[i] }

        fuelType = findViewById(R.id.car_fuel_type)
        fuelType.text.append(fuelTypes.first())
        fuelType.inputType = InputType.TYPE_NULL
        fuelType.setAdapter(fuelAdapter)
        fuelType.setOnFocusChangeListener { view, _ -> hideKeyboard(view) }
        fuelType.setOnItemClickListener { _, _, i, _ ->
            carInfo.fuelType = CarFuelType.values()[i]
            if (carInfo.fuelType == CarFuelType.ELECTRICITY
                && engineVolumeLayout.visibility == View.VISIBLE) {
                engineVolumeLayout.visibility = View.GONE
            } else {
                engineVolumeLayout.visibility = View.VISIBLE
            }
        }

        engineVolume = findViewById(R.id.car_engine)
        engineVolumeLayout = findViewById(R.id.car_engine_layout)
        engineVolumeLayout.visibility = View.VISIBLE
    }

    private fun initCarInfo() {
        if (id != defaultId) {
            carInfo = CarInfo(
                CarBodyType.valueOf(intent.getStringExtra("body_type")!!),
                CarFuelType.valueOf(intent.getStringExtra("fuel_type")!!)
            )
            fillData()
        } else {
            carInfo = CarInfo(
                CarBodyType.SEDAN,
                CarFuelType.GASOLINE
            )
        }
    }

    private fun fillData() {
        name.text.append(intent.getStringExtra("car_name"))
        bodyType.text.clear()
        bodyType.text.append(bodyTypes[carInfo.body.ordinal])
        fuelType.text.clear()
        fuelType.text.append(fuelTypes[carInfo.fuelType.ordinal])
        val volume = intent.getStringExtra("engine_volume")
        if (volume != null) {
            engineVolume.text.append(volume)
            engineVolumeLayout.visibility = View.VISIBLE
        } else {
            engineVolumeLayout.visibility = View.GONE
        }
    }

    private fun validateFields(): Boolean {
        var result = true

        if (name.text.isBlank()) {
            name.error = getString(R.string.activity_car_name_error)
            result = false
        }

        if (engineVolumeLayout.visibility != View.GONE && (engineVolume.text.isNullOrBlank() || engineVolume.text.toString().toDouble() <= 0.0)) {
            engineVolume.error = getString(R.string.activity_car_engine_error)
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
        intent.putExtra("car_name", name.text.toString())
        resources.getStringArray(R.array.body_types)
        intent.putExtra("body_type", carInfo.body.name)
        intent.putExtra("fuel_type", carInfo.fuelType.name)
        if (engineVolume.text.isNotBlank()) {
            intent.putExtra("engine_volume", engineVolume.text.toString().toDouble())
        }
    }

    private fun hideKeyboard(view: View) {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onBackPressed() {
        handleBackAction()
    }
}

private data class CarInfo(
    var body: CarBodyType,
    var fuelType: CarFuelType
)