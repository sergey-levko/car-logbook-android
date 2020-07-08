package by.liauko.siarhei.cl.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.database.entity.CarProfileEntity
import by.liauko.siarhei.cl.entity.CarProfileData
import by.liauko.siarhei.cl.recyclerview.adapter.RecyclerViewCarProfileAdapter
import by.liauko.siarhei.cl.repository.CarProfileRepository
import by.liauko.siarhei.cl.repository.FuelConsumptionRepository
import by.liauko.siarhei.cl.repository.LogRepository
import by.liauko.siarhei.cl.util.AppResultCodes.CAR_PROFILE_ADD
import by.liauko.siarhei.cl.util.AppResultCodes.CAR_PROFILE_EDIT
import by.liauko.siarhei.cl.util.ApplicationUtil.EMPTY_STRING
import by.liauko.siarhei.cl.util.ApplicationUtil.profileId
import by.liauko.siarhei.cl.util.ApplicationUtil.profileName
import by.liauko.siarhei.cl.util.CarBodyType
import by.liauko.siarhei.cl.util.CarFuelType
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CarProfilesActivity : AppCompatActivity() {

    private lateinit var carProfileRepository: CarProfileRepository
    private lateinit var fab: FloatingActionButton
    private lateinit var rvAdapter: RecyclerViewCarProfileAdapter
    private lateinit var items: ArrayList<CarProfileData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_profiles)

        carProfileRepository = CarProfileRepository(applicationContext)

        items = arrayListOf()
        initToolbar()
        initRecyclerView()

        fab = findViewById(R.id.add_car_profile_fab)
        fab.setOnClickListener {
            startActivityForResult(
                Intent(applicationContext, CarDataActivity::class.java),
                CAR_PROFILE_ADD
            )
        }

        select()
    }

    private fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.car_profile_toolbar)
        toolbar.setNavigationIcon(R.drawable.arrow_left_white)
        toolbar.setNavigationOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun initRecyclerView() {
        rvAdapter = RecyclerViewCarProfileAdapter(
            resources,
            items,
            object: RecyclerViewCarProfileAdapter.RecyclerViewOnItemClickListener {
                override fun onItemClick(item: CarProfileData, isSelect: Boolean) {
                    if (isSelect) {
                        profileId = item.id
                        profileName = item.name
                        saveProfileInfo()
                        setResult(RESULT_OK, Intent())
                        finish()
                    } else {
                        val intent = Intent(applicationContext, CarDataActivity::class.java)
                        intent.putExtra("id", item.id)
                        intent.putExtra("car_name", item.name)
                        intent.putExtra("body_type", item.bodyType.name)
                        intent.putExtra("fuel_type", item.fuelType.name)
                        intent.putExtra("engine_volume", item.engineVolume?.toString())
                        startActivityForResult(intent, CAR_PROFILE_EDIT)
                    }
                }
            }
        )

        findViewById<RecyclerView>(R.id.car_profile_recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = rvAdapter
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                CAR_PROFILE_ADD -> {
                    val name = data.getStringExtra("car_name") ?: EMPTY_STRING
                    val body = data.getStringExtra("body_type") ?: "SEDAN"
                    val fuel = data.getStringExtra("fuel_type") ?: "GASOLINE"
                    val volume = data.getStringExtra("engine_volume")?.toDouble()
                    val carProfile = CarProfileEntity(null, name, body, fuel, volume)
                    val id = carProfileRepository.insert(carProfile)
                    if (id != -1L) {
                        items.add(CarProfileData(id, name, CarBodyType.valueOf(body), CarFuelType.valueOf(fuel), volume))
                        rvAdapter.notifyDataSetChanged()
                    }
                }
                CAR_PROFILE_EDIT -> {
                    val id = data.getLongExtra("id", -1L)
                    val item = items.find { it.id == id }!!

                    if (data.getBooleanExtra("remove", false)) {
                        val position = items.indexOf(item)
                        rvAdapter.removeItem(position)
                        carProfileRepository.delete(item)
                        LogRepository(applicationContext).deleteAllByProfileId(id)
                        FuelConsumptionRepository(applicationContext).deleteAllByProfileId(id)
                        if (items.isEmpty()) {
                            val defaultTitle = getString(R.string.app_name)
                            getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE)
                                .edit()
                                .putLong(getString(R.string.car_profile_id_key), -1L)
                                .putString(getString(R.string.car_profile_name_key), defaultTitle)
                                .apply()
                            profileId = -1L
                            profileName = defaultTitle
                            saveProfileInfo()
                        } else if (profileId == id) {
                            profileId = items.first().id
                            profileName = items.first().name
                            saveProfileInfo()
                        }
                    } else {
                        val name = data.getStringExtra("car_name") ?: EMPTY_STRING
                        val body = data.getStringExtra("body_type") ?: "SEDAN"
                        val fuel = data.getStringExtra("fuel_type") ?: "GASOLINE"
                        val volume = data.getStringExtra("engine_volume")?.toDouble()
                        item.name = name
                        item.bodyType = CarBodyType.valueOf(body)
                        item.fuelType = CarFuelType.valueOf(fuel)
                        item.engineVolume = volume
                        carProfileRepository.update(item)
                        rvAdapter.notifyDataSetChanged()
                    }

                }
            }
        }
    }

    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        finish()
    }

    private fun select() {
        items.clear()
        items.addAll(carProfileRepository.selectAll())
        rvAdapter.notifyDataSetChanged()
    }

    private fun saveProfileInfo() {
        getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE)
            .edit()
            .putLong(getString(R.string.car_profile_id_key), profileId)
            .putString(getString(R.string.car_profile_name_key), profileName)
            .apply()
    }
}