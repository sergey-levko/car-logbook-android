package by.liauko.siarhei.cl.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.databinding.ActivityCarDataBinding
import by.liauko.siarhei.cl.util.CarBodyType
import by.liauko.siarhei.cl.util.CarFuelType
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CarDataActivity : AppCompatActivity() {

    private val defaultId = -1L

    private lateinit var bodyTypes: Array<String>
    private lateinit var fuelTypes: Array<String>
    private lateinit var carInfo: CarInfo
    private lateinit var viewBinding: ActivityCarDataBinding

    private var id = defaultId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCarDataBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        bodyTypes = resources.getStringArray(R.array.body_types)
        fuelTypes = resources.getStringArray(R.array.fuel_type)

        id = intent.getLongExtra("id", defaultId)

        initToolbar()
        initCarInfo()
        initElements()
        if (id != defaultId) {
            fillData()
        }
    }

    private fun initToolbar() {
        viewBinding.carToolbar.setTitle(if (id == defaultId) R.string.activity_car_title_create else R.string.activity_car_title_edit)
        viewBinding.carToolbar.setNavigationIcon(R.drawable.arrow_left_white)
        viewBinding.carToolbar.setNavigationContentDescription(R.string.back_button_content_descriptor)
        viewBinding.carToolbar.setNavigationOnClickListener {
            handleBackAction()
        }
        viewBinding.carToolbar.inflateMenu(R.menu.menu_data_activity)
        viewBinding.carToolbar.setOnMenuItemClickListener {
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
                                .show()
                        }
                        .setNegativeButton(R.string.no) { dialog, _ -> dialog.cancel() }
                        .show()
                }
            }

            return@setOnMenuItemClickListener result
        }
        if (id == defaultId) {
            viewBinding.carToolbar.menu.findItem(R.id.data_menu_save).isVisible = true
            viewBinding.carToolbar.menu.findItem(R.id.data_menu_delete).isVisible = false
        } else {
            viewBinding.carToolbar.menu.findItem(R.id.data_menu_save).isVisible = false
            viewBinding.carToolbar.menu.findItem(R.id.data_menu_delete).isVisible = true
        }
    }

    private fun initElements() {
        val bodyAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, bodyTypes)
        val fuelAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, fuelTypes)

        viewBinding.carBody.text.append(bodyTypes[carInfo.body.ordinal])
        viewBinding.carBody.inputType = InputType.TYPE_NULL
        viewBinding.carBody.setAdapter(bodyAdapter)
        viewBinding.carBody.setOnFocusChangeListener { view, isFocus -> if (isFocus) hideKeyboard(view) }
        viewBinding.carBody.setOnItemClickListener { _, _, i, _ -> carInfo.body = CarBodyType.values()[i] }

        viewBinding.carFuelType.text.append(fuelTypes[carInfo.fuelType.ordinal])
        viewBinding.carFuelType.inputType = InputType.TYPE_NULL
        viewBinding.carFuelType.setAdapter(fuelAdapter)
        viewBinding.carFuelType.setOnFocusChangeListener { view, isFocus -> if (isFocus) hideKeyboard(view) }
        viewBinding.carFuelType.setOnItemClickListener { _, _, i, _ ->
            carInfo.fuelType = CarFuelType.values()[i]
            if (carInfo.fuelType == CarFuelType.ELECTRICITY
                && viewBinding.carEngineLayout.visibility == View.VISIBLE) {
                viewBinding.carEngineLayout.visibility = View.GONE
            } else {
                viewBinding.carEngineLayout.visibility = View.VISIBLE
            }
        }
    }

    private fun initCarInfo() {
        carInfo = if (id != defaultId) {
            CarInfo(
                CarBodyType.valueOf(intent.getStringExtra("body_type")!!),
                CarFuelType.valueOf(intent.getStringExtra("fuel_type")!!)
            )
        } else {
            CarInfo(
                CarBodyType.SEDAN,
                CarFuelType.GASOLINE
            )
        }
    }

    private fun fillData() {
        viewBinding.carName.text?.append(intent.getStringExtra("car_name"))
        val volume = intent.getStringExtra("engine_volume")
        if (volume != null) {
            viewBinding.carEngine.text?.append(volume)
            viewBinding.carEngineLayout.visibility = View.VISIBLE
        } else {
            viewBinding.carEngineLayout.visibility = View.GONE
        }
    }

    private fun validateFields(): Boolean {
        var result = true

        if (viewBinding.carName.text.isNullOrBlank()) {
            viewBinding.carName.error = getString(R.string.activity_car_name_error)
            result = false
        }

        if (viewBinding.carEngineLayout.visibility != View.GONE
            && (viewBinding.carEngine.text.isNullOrBlank() || viewBinding.carEngine.text.toString().toDouble() <= 0.0)
        ) {
            viewBinding.carEngine.error = getString(R.string.activity_car_engine_error)
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
        intent.putExtra("car_name", viewBinding.carName.text.toString())
        resources.getStringArray(R.array.body_types)
        intent.putExtra("body_type", carInfo.body.name)
        intent.putExtra("fuel_type", carInfo.fuelType.name)
        if (viewBinding.carEngine.text?.isNotBlank() != false) {
            intent.putExtra("engine_volume", viewBinding.carEngine.text.toString())
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
