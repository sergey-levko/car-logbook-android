package by.liauko.siarhei.cl.backup

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import androidx.core.app.ActivityCompat

object PermissionService {

    fun checkPermissions(activity: Activity) {
        val internetPermission = ActivityCompat.checkSelfPermission(activity.applicationContext, Manifest.permission.INTERNET)
        if (internetPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.INTERNET),
                internetPermission
            )
        }
    }

    // NetworkInfo class is deprecated in Android 10
    @Suppress("DEPRECATION")
    fun checkInternetConnection(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo?.isConnectedOrConnecting == true
    }
}