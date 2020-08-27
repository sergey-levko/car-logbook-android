package by.liauko.siarhei.cl.backup.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent

class ActivityAdapter(private val activity: Activity): BackupAdapter {

    override fun getContext(): Context = activity

    override fun getContextForAuth(): Context = activity.applicationContext

    override fun getActivity(): Activity? = activity

    override fun getActivityForPermissions(): Activity = activity

    override fun startActivityForResult(intent: Intent, requestCode: Int) {
        activity.startActivityForResult(intent, requestCode)
    }
}

fun Activity.toBackupAdapter() = ActivityAdapter(this)
