package by.liauko.siarhei.cl.activity.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.liauko.siarhei.cl.R
import by.liauko.siarhei.cl.activity.FuelDataActivity
import by.liauko.siarhei.cl.activity.LogDataActivity
import by.liauko.siarhei.cl.databinding.FragmentDataBinding
import by.liauko.siarhei.cl.model.DataModel
import by.liauko.siarhei.cl.model.FuelDataModel
import by.liauko.siarhei.cl.model.LogDataModel
import by.liauko.siarhei.cl.recyclerview.adapter.RecyclerViewDataAdapter
import by.liauko.siarhei.cl.repository.AppDataRepositoryFactory
import by.liauko.siarhei.cl.util.AppResultCodes.FUEL_CONSUMPTION_ADD
import by.liauko.siarhei.cl.util.AppResultCodes.FUEL_CONSUMPTION_EDIT
import by.liauko.siarhei.cl.util.AppResultCodes.LOG_ADD
import by.liauko.siarhei.cl.util.AppResultCodes.LOG_EDIT
import by.liauko.siarhei.cl.util.ApplicationUtil.EMPTY_STRING
import by.liauko.siarhei.cl.util.ApplicationUtil.dataPeriod
import by.liauko.siarhei.cl.util.ApplicationUtil.profileId
import by.liauko.siarhei.cl.util.ApplicationUtil.profileName
import by.liauko.siarhei.cl.util.ApplicationUtil.type
import by.liauko.siarhei.cl.util.DataPeriod
import by.liauko.siarhei.cl.util.DataType
import by.liauko.siarhei.cl.viewmodel.AppDataViewModel
import by.liauko.siarhei.cl.viewmodel.factory.AppDataViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.runBlocking
import java.util.Calendar

class DataFragment : Fragment() {

    private lateinit var viewModel: AppDataViewModel
    private lateinit var rvAdapter: RecyclerViewDataAdapter

    private var bindingObject: FragmentDataBinding? = null
    private val viewBinding get() = bindingObject!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bindingObject = FragmentDataBinding.inflate(inflater, container, false)

        val modelFactory = AppDataViewModelFactory(AppDataRepositoryFactory.getRepository(requireContext(), type))
        viewModel = ViewModelProvider(this, modelFactory).get(AppDataViewModel::class.java)
        runBlocking { viewModel.loadItems() }
        viewModel.items.observe(viewLifecycleOwner) {
            val result = DiffUtil.calculateDiff(object: DiffUtil.Callback() {
                override fun getOldListSize() = viewModel.oldItems.size

                override fun getNewListSize() = it.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                    viewModel.oldItems[oldItemPosition].id == it[newItemPosition].id

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                    viewModel.oldItems[oldItemPosition] == it[newItemPosition]
            })
            result.dispatchUpdatesTo(rvAdapter)
            rvAdapter.refreshNoDataTextVisibility()
        }

        initRecyclerView()

        viewBinding.addFab.setOnClickListener {
            when (type) {
                DataType.LOG -> startActivityForResult(
                    Intent(requireContext(), LogDataActivity::class.java),
                    LOG_ADD
                )
                DataType.FUEL -> startActivityForResult(
                    Intent(requireContext(), FuelDataActivity::class.java),
                    FUEL_CONSUMPTION_ADD
                )
            }
        }

        return viewBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bindingObject = null
    }

    private fun initRecyclerView() {
        rvAdapter =
            RecyclerViewDataAdapter(
                viewModel.items.value ?: emptyList(),
                resources,
                viewBinding.noDataText,
                object : RecyclerViewDataAdapter.RecyclerViewOnItemClickListener {
                    override fun onItemClick(item: DataModel) {
                        if (item is LogDataModel) {
                            callLogEditActivityForResult(LogDataActivity::class.java, item)
                        } else if (item is FuelDataModel) {
                            callFuelConsumptionEditActivityForResult(FuelDataActivity::class.java, item)
                        }
                    }
                })

        viewBinding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = rvAdapter
        }
        viewBinding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    viewBinding.addFab.shrink()
                } else if (dy < 0) {
                    viewBinding.addFab.extend()
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val time = data.getLongExtra("time", Calendar.getInstance().timeInMillis)
            when (requestCode) {
                FUEL_CONSUMPTION_ADD -> {
                    val litres = data.getStringExtra("litres")?.toDouble() ?: Double.MIN_VALUE
                    val mileage = data.getStringExtra("mileage")?.toInt() ?: Int.MIN_VALUE
                    val distance = data.getStringExtra("distance")?.toDouble() ?: Double.MIN_VALUE
                    val fuelConsumption = litres * 100 / distance
                    viewModel.add(
                        FuelDataModel(null, time, fuelConsumption, litres, mileage, distance, profileId)
                    )
                }
                FUEL_CONSUMPTION_EDIT -> {
                    val id = data.getLongExtra("id", -1L)
                    val item = viewModel.get(id) as FuelDataModel
                    val index = viewModel.indexOf(item)

                    if (data.getBooleanExtra("remove", false)) {
                        viewModel.delete(index)
                        showRemoveItemSnackbar(item, index)
                    } else {
                        val litres = data.getStringExtra("litres")?.toDouble() ?: Double.MIN_VALUE
                        val mileage = data.getStringExtra("mileage")?.toInt() ?: Int.MIN_VALUE
                        val distance = data.getStringExtra("distance")?.toDouble() ?: Double.MIN_VALUE
                        val fuelConsumption = litres * 100 / distance
                        item.fuelConsumption = fuelConsumption
                        item.litres = litres
                        item.mileage = mileage
                        item.distance = distance
                        item.time = time
                        rvAdapter.notifyItemChanged(index, item)
                        viewModel.update(item)
                    }
                }
                LOG_ADD -> {
                    val title = data.getStringExtra("title")?.trim() ?: EMPTY_STRING
                    val text = data.getStringExtra("text")?.trim() ?: EMPTY_STRING
                    val mileage = data.getStringExtra("mileage")?.toLong() ?: Long.MIN_VALUE
                    viewModel.add(LogDataModel(null, time, title, text, mileage, profileId))
                }
                LOG_EDIT -> {
                    val id = data.getLongExtra("id", -1L)
                    val item = viewModel.get(id) as LogDataModel
                    val index = viewModel.indexOf(item)

                    if (data.getBooleanExtra("remove", false)) {
                        viewModel.delete(index)
                        showRemoveItemSnackbar(item, index)
                    } else {
                        val title = data.getStringExtra("title") ?: EMPTY_STRING
                        val text = data.getStringExtra("text") ?: EMPTY_STRING
                        val mileage = data.getStringExtra("mileage")?.toLong() ?: Long.MIN_VALUE
                        item.title = title
                        item.text = text
                        item.mileage = mileage
                        rvAdapter.notifyItemChanged(index, item)
                        viewModel.update(item)
                    }
                }
            }
        }
    }

    private fun callLogEditActivityForResult(activityClass: Class<*>, item: LogDataModel) {
        val intent = Intent(requireContext(), activityClass)
        intent.putExtra("title", R.string.activity_data_title_edit)
        intent.putExtra("id", item.id)
        intent.putExtra("time", item.time)
        intent.putExtra("log_title", item.title)
        intent.putExtra("mileage", item.mileage)
        intent.putExtra("text", item.text)
        startActivityForResult(intent, LOG_EDIT)
    }

    private fun callFuelConsumptionEditActivityForResult(activityClass: Class<*>, item: FuelDataModel) {
        val intent = Intent(requireContext(), activityClass)
        intent.putExtra("title", R.string.activity_data_title_edit)
        intent.putExtra("id", item.id)
        intent.putExtra("time", item.time)
        intent.putExtra("litres", item.litres)
        intent.putExtra("mileage", item.mileage)
        intent.putExtra("distance", item.distance)
        startActivityForResult(intent, FUEL_CONSUMPTION_EDIT)
    }

    private fun showRemoveItemSnackbar(item: DataModel, index: Int) {
        Snackbar.make(viewBinding.recyclerviewCoordinatorLayout, R.string.data_fragment_snackbar_message,
            Snackbar.LENGTH_LONG
        ).setAction(R.string.data_fragment_snackbar_undo) {
            viewModel.restore(index, item)
        }.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)
                if (event != DISMISS_EVENT_ACTION) {
                    viewModel.deleteFromRepo(item)
                }
            }
        }).show()
    }
}
