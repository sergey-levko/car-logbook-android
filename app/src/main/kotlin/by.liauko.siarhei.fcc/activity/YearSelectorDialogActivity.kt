package by.liauko.siarhei.fcc.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import by.liauko.siarhei.fcc.R
import by.liauko.siarhei.fcc.util.ApplicationUtil.appTheme
import java.util.Calendar

class YearSelectorDialogActivity: AppCompatActivity(), View.OnClickListener {
    private val currentYear = Calendar.getInstance()[Calendar.YEAR]
    private val minYear = 1970

    private lateinit var yearEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(appTheme.dialogId)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.year_selector_dialog)
        val parameters = window.attributes
        parameters.width = WindowManager.LayoutParams.MATCH_PARENT
        window.attributes = parameters

        setTitle(R.string.year_selector_dialog_title)

        yearEditText = findViewById(R.id.year_value)
        yearEditText.append(intent.getIntExtra("year", currentYear).toString())
        findViewById<Button>(R.id.year_dialog_positive_button).setOnClickListener(this)
        findViewById<Button>(R.id.year_dialog_negative_button).setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.year_dialog_positive_button -> {
                    val value = yearEditText.text.toString()
                    if (!value.isEmpty()
                        && (value.toInt() < minYear || value.toInt() > currentYear)) {
                        yearEditText.error = "${getString(R.string.year_selector_dialog_error_text)} $currentYear"
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
