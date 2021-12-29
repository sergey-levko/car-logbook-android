package by.liauko.siarhei.cl.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.databinding.ActivityCarProfilesBinding
import by.liauko.siarhei.cl.model.CarProfileModel
import by.liauko.siarhei.cl.recyclerview.adapter.RecyclerViewCarProfileAdapter
import by.liauko.siarhei.cl.util.AppResultCodes.CAR_PROFILE_ADD
import by.liauko.siarhei.cl.util.AppResultCodes.CAR_PROFILE_EDIT
import by.liauko.siarhei.cl.util.ApplicationUtil.EMPTY_STRING
import by.liauko.siarhei.cl.util.ApplicationUtil.profileId
import by.liauko.siarhei.cl.util.ApplicationUtil.profileName
import by.liauko.siarhei.cl.util.CarBodyType
import by.liauko.siarhei.cl.util.CarFuelType
import by.liauko.siarhei.cl.viewmodel.CarProfileViewModel
import kotlinx.coroutines.runBlocking

class CarProfilesActivity : AppCompatActivity() {

    private val viewModel: CarProfileViewModel by viewModels()

    private lateinit var rvAdapter: RecyclerViewCarProfileAdapter
    private lateinit var viewBinding: ActivityCarProfilesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCarProfilesBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        runBlocking { viewModel.loadCarProfiles() }
        viewModel.profiles.observe(this) {
            val result = DiffUtil.calculateDiff(object: DiffUtil.Callback() {
                override fun getOldListSize() = viewModel.oldItems.size

                override fun getNewListSize() = it.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                    viewModel.oldItems[oldItemPosition].id == it[newItemPosition].id

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                    viewModel.oldItems[oldItemPosition] == it[newItemPosition]
            })
            result.dispatchUpdatesTo(rvAdapter)
        }

        initToolbar()
        initRecyclerView()

        viewBinding.addCarProfileFab.setOnClickListener {
            startActivityForResult(
                Intent(applicationContext, CarDataActivity::class.java),
                CAR_PROFILE_ADD
            )
        }
    }

    private fun initToolbar() {
        viewBinding.carProfileToolbar.setNavigationIcon(R.drawable.arrow_left_white)
        viewBinding.carProfileToolbar.setNavigationContentDescription(R.string.back_button_content_descriptor)
        viewBinding.carProfileToolbar.setNavigationOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun initRecyclerView() {
        rvAdapter = RecyclerViewCarProfileAdapter(
            resources,
            viewModel.profiles.value ?: emptyList(),
            object: RecyclerViewCarProfileAdapter.RecyclerViewOnItemClickListener {
                override fun onItemClick(item: CarProfileModel, isSelect: Boolean) {
                    if (isSelect) {
                        profileId = item.id!!
                        profileName = item.name
                        saveProfileInfo()
                        setResult(RESULT_OK)
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

        viewBinding.carProfileRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = rvAdapter
        }
        viewBinding.carProfileRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    viewBinding.addCarProfileFab.shrink()
                } else if (dy < 0) {
                    viewBinding.addCarProfileFab.extend()
                }
            }
        })
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
                    viewModel.add(CarProfileModel(null, name, CarBodyType.valueOf(body), CarFuelType.valueOf(fuel), volume))
                }
                CAR_PROFILE_EDIT -> {
                    val id = data.getLongExtra("id", -1L)

                    if (data.getBooleanExtra("remove", false)) {
                        viewModel.delete(id)
                        if (id == profileId) {
                            profileId = viewModel.get(0).id!!
                            profileName = viewModel.get(0).name
                            rvAdapter.notifyItemChanged(0)
                        }
                    } else {
                        val item = viewModel.get(id)!!
                        val index = viewModel.indexOf(item)
                        val name = data.getStringExtra("car_name") ?: EMPTY_STRING
                        val body = data.getStringExtra("body_type") ?: "SEDAN"
                        val fuel = data.getStringExtra("fuel_type") ?: "GASOLINE"
                        val volume = data.getStringExtra("engine_volume")?.toDouble()
                        item.name = name
                        item.bodyType = CarBodyType.valueOf(body)
                        item.fuelType = CarFuelType.valueOf(fuel)
                        item.engineVolume = volume
                        rvAdapter.notifyItemChanged(index, item)
                        viewModel.update(item)
                    }
                    saveProfileInfo()
                }
            }
        }
    }

    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        finish()
    }

    private fun saveProfileInfo() {
        getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE)
            .edit()
            .putLong(getString(R.string.car_profile_id_key), profileId)
            .putString(getString(R.string.car_profile_name_key), profileName)
            .apply()
    }
}
