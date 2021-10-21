package by.liauko.siarhei.cl.activity.dialog

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import by.liauko.siarhei.cl.databinding.DialogProgressBinding

class ProgressDialog(
    context: Context,
    private val message: String
) : AlertDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewBinding = DialogProgressBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        viewBinding.progressDialogText.text = message
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }
}
