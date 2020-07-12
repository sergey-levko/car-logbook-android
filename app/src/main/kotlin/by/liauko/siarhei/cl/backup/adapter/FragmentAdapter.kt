package by.liauko.siarhei.cl.backup.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment

class FragmentAdapter(private val fragment: Fragment): BackupAdapter {

    override fun getContext(): Context = fragment.requireContext()

    override fun getContextForAuth(): Context = getContext()

    override fun getActivity(): Activity? = null

    override fun getActivityForPermissions(): Activity = fragment.requireActivity()

    override fun startActivityForResult(intent: Intent, requestCode: Int) {
        fragment.startActivityForResult(intent, requestCode)
    }
}

fun Fragment.toBackupAdapter() = FragmentAdapter(this)