package by.liauko.siarhei.cl.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.databinding.DialogYearSelectorBinding
import java.util.Calendar

class YearSelectorDialogActivity : AppCompatActivity(), View.OnClickListener {

    private val currentYear = Calendar.getInstance()[Calendar.YEAR]
    private val minYear = 1970

    private lateinit var viewBinding: DialogYearSelectorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DialogYearSelectorBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        val parameters = window.attributes
        parameters.width = WindowManager.LayoutParams.MATCH_PARENT
        window.attributes = parameters

        setTitle(R.string.year_selector_dialog_title)

        viewBinding.yearSelectorInputLayout.isErrorEnabled = true
        viewBinding.yearSelectorValue.append(intent.getIntExtra("year", currentYear).toString())
        viewBinding.yearSelectorValue.requestFocus()
        viewBinding.yearDialogPositiveButton.setOnClickListener(this)
        viewBinding.yearDialogNegativeButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.year_dialog_positive_button -> {
                    val value = viewBinding.yearSelectorValue.text.toString()
                    if (value.isEmpty()
                        || (value.toInt() < minYear || value.toInt() > currentYear)) {
                        viewBinding.yearSelectorInputLayout.error = "${getString(R.string.year_selector_dialog_error_text)} $currentYear"
                    } else {
                        val intent = Intent()
                        intent.putExtra("year", value)
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                }
                R.id.year_dialog_negative_button -> {
                    setResult(RESULT_CANCELED)
                    finish()
                }
            }
        }
    }
}
