package by.liauko.siarhei.cl.activity.dialog

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import by.liauko.siarhei.cl.databinding.DialogProgressBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProgressDialog(
    applicationContext: Context,
    message: String
) {

    private val builder: MaterialAlertDialogBuilder

    init {
        val viewBinding = DialogProgressBinding.inflate(LayoutInflater.from(applicationContext))
        viewBinding.progressDialogText.text = message
        builder = MaterialAlertDialogBuilder(applicationContext)
            .setView(viewBinding.root)
            .setCancelable(false)
    }

    fun show(): AlertDialog = builder.show()
}
