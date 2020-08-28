package by.liauko.siarhei.cl.activity.dialog

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import by.liauko.siarhei.cl.R

class ProgressDialog(
    context: Context,
    private val message: String
) : AlertDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_progress)
        findViewById<TextView>(R.id.progress_dialog_text)!!.text = message
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }
}
