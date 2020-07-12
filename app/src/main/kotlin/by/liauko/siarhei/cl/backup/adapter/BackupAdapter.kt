package by.liauko.siarhei.cl.backup.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent

interface BackupAdapter {

    fun getContext(): Context

    fun getContextForAuth(): Context

    fun getActivity(): Activity?

    fun getActivityForPermissions(): Activity

    fun startActivityForResult(intent: Intent, requestCode: Int)
}