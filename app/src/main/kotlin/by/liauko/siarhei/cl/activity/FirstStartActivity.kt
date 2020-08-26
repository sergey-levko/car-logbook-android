package by.liauko.siarhei.cl.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.backup.BackupService
import by.liauko.siarhei.cl.backup.BackupTask
import by.liauko.siarhei.cl.backup.PermissionService
import by.liauko.siarhei.cl.backup.adapter.toBackupAdapter
import by.liauko.siarhei.cl.database.entity.CarProfileEntity
import by.liauko.siarhei.cl.repository.CarProfileRepository
import by.liauko.siarhei.cl.util.AppResultCodes.BACKUP_OPEN_DOCUMENT
import by.liauko.siarhei.cl.util.AppResultCodes.CAR_PROFILE_ADD
import by.liauko.siarhei.cl.util.AppResultCodes.GOOGLE_SIGN_IN
import by.liauko.siarhei.cl.util.ApplicationUtil
import by.liauko.siarhei.cl.util.ApplicationUtil.EMPTY_STRING
import by.liauko.siarhei.cl.util.ApplicationUtil.profileId
import by.liauko.siarhei.cl.util.ApplicationUtil.profileName
import by.liauko.siarhei.cl.util.ImportFromFileAsyncTask

class FirstStartActivity : AppCompatActivity(),
    View.OnClickListener {

    private lateinit var carProfileRepository: CarProfileRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_start)

        carProfileRepository = CarProfileRepository(applicationContext)

        findViewById<Button>(R.id.activity_first_start_create).setOnClickListener(this)
        findViewById<Button>(R.id.activity_first_start_file_import).setOnClickListener(this)
        findViewById<Button>(R.id.activity_first_start_drive_import).setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.activity_first_start_create -> startActivityForResult(Intent(applicationContext, CarDataActivity::class.java), CAR_PROFILE_ADD)
                R.id.activity_first_start_drive_import -> {
                    if (!PermissionService.checkInternetConnection(applicationContext)) {
                        Toast.makeText(
                            applicationContext,
                            R.string.settings_preference_backup_internet_access_toast_text,
                            Toast.LENGTH_LONG
                        ).show()
                    } else if (BackupService.driveServiceHelper == null) {
                        BackupService.backupTask = BackupTask.IMPORT
                        BackupService.googleAuth(this.toBackupAdapter())
                    } else {
                        BackupService.importDataFromDrive(this, this)
                    }
                }
                R.id.activity_first_start_file_import -> {
                    BackupService.openDocument(this.toBackupAdapter())
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CAR_PROFILE_ADD -> {
                    if (data != null) {
                        val name = data.getStringExtra("car_name") ?: EMPTY_STRING
                        val body = data.getStringExtra("body_type") ?: "SEDAN"
                        val fuel = data.getStringExtra("fuel_type") ?: "GASOLINE"
                        val volume = data.getStringExtra("engine_volume")?.toDouble()
                        val carProfile = CarProfileEntity(null, name, body, fuel, volume)
                        val id = carProfileRepository.insert(carProfile)
                        if (id != -1L) {
                            profileId = id
                            profileName = name
                            getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE)
                                .edit()
                                .putLong(getString(R.string.car_profile_id_key), profileId)
                                .putString(getString(R.string.car_profile_name_key), profileName)
                                .apply()
                            setResult(RESULT_OK)
                            finish()
                        } else {
                            showFailDialog()
                        }
                    } else {
                        showFailDialog()
                    }
                }
                GOOGLE_SIGN_IN -> BackupService.googleSignInResult(this, data, this)
                BACKUP_OPEN_DOCUMENT -> ImportFromFileAsyncTask(data?.data ?: Uri.EMPTY, this, this).execute()
            }
        }
    }

    private fun showFailDialog() {
        ApplicationUtil.createAlertDialog(
            applicationContext,
            R.string.default_car_profile_dialog_title,
            R.string.default_car_profile_dialog_message
        ).show()
    }
}