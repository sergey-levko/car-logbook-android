package by.liauko.siarhei.fcc.activity

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.liauko.siarhei.fcc.R
import by.liauko.siarhei.fcc.database.FuelConsumptionCalculatorDBHelper
import by.liauko.siarhei.fcc.database.util.CarLogDBUtil
import by.liauko.siarhei.fcc.entity.Data
import by.liauko.siarhei.fcc.entity.DataType
import by.liauko.siarhei.fcc.entity.FuelConsumptionData
import by.liauko.siarhei.fcc.entity.LogData
import by.liauko.siarhei.fcc.recyclerview.RecyclerViewDataAdapter
import by.liauko.siarhei.fcc.recyclerview.RecyclerViewOnItemClickListener
import by.liauko.siarhei.fcc.recyclerview.RecyclerViewSwipeController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import java.util.Calendar

class MainActivity : AppCompatActivity(), View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {
    private val requestCodeAddFuelConsumption = 1
    private val requestCodeEditFuelConsumption = 2
    private val requestCodeAddLog = 3
    private val requestCodeEditLog = 4

    private lateinit var items: ArrayList<Data>
    private lateinit var rvAdapter: RecyclerViewDataAdapter
    private lateinit var database: SQLiteDatabase
    private lateinit var dbUtil: CarLogDBUtil
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var fab: FloatingActionButton

    private var type = DataType.LOG

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        items = arrayListOf()
        initToolbar()
        initDatabase()
        initRecyclerView()
        initNavigationView()

        fab = findViewById(R.id.add_fab)
        fab.setOnClickListener(this)

        select(type)
    }

    private fun initToolbar() {
        toolbar = findViewById(R.id.toolbar)
        toolbar.setTitle(R.string.activity_main_log_title)
        setSupportActionBar(toolbar)
    }

    private fun initDatabase() {
        val dbHelper = FuelConsumptionCalculatorDBHelper(applicationContext)
        database = dbHelper.writableDatabase
        dbUtil = CarLogDBUtil(database)
    }

    private fun initRecyclerView() {
        rvAdapter = RecyclerViewDataAdapter(items, resources, database, object: RecyclerViewOnItemClickListener {
            override fun onItemClick(item: Data) {
                if (item is LogData) {
                    callLogEditActivityForResult(LogDataActivity::class.java, item)
                } else if (item is FuelConsumptionData) {
                    callFuelConsumptionEditActivityForResult(FuelDataDialogActivity::class.java, item)
                }
            }
        })

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = rvAdapter
        }
        recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    fab.hide()
                } else if (dy < 0) {
                    fab.show()
                }
            }
        })

        val helper = ItemTouchHelper(
            RecyclerViewSwipeController(
                rvAdapter
            )
        )
        helper.attachToRecyclerView(recyclerView)
    }

    private fun initNavigationView() {
        drawerLayout = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.activity_main_navigation_view_open, R.string.activity_main_navigation_view_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener(this)
    }

    override fun onClick(v: View?) {
        when (type) {
            DataType.LOG -> startActivityForResult(Intent(this, LogDataActivity::class.java), requestCodeAddLog)
            DataType.FUEL -> startActivityForResult(Intent(this, FuelDataDialogActivity::class.java), requestCodeAddFuelConsumption)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var result = false

        when (item.itemId) {
            R.id.log_menu_item -> {
                type = DataType.LOG
                toolbar.setTitle(R.string.activity_main_log_title)
                result = true
            }
            R.id.fuel_menu_item -> {
                type = DataType.FUEL
                toolbar.setTitle(R.string.activity_main_fuel_title)
                result = true
            }
        }
        select(type)
        drawerLayout.closeDrawers()
        return result
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            val time = data.getLongExtra("time", Calendar.getInstance().timeInMillis)
            when (requestCode) {
                requestCodeAddFuelConsumption -> {
                    val litres = data.getStringExtra("litres").toDouble()
                    val distance = data.getStringExtra("distance").toDouble()
                    val fuelConsumption = litres * 100 / distance
                    val id = dbUtil.insertFuelData(litres, distance, fuelConsumption, time)
                    if (id != -1L) {
                        items.add(FuelConsumptionData(id, time, fuelConsumption, litres, distance))
                        rvAdapter.refreshRecyclerView()
                    }
                }
                requestCodeEditFuelConsumption -> {
                    val id = data.getLongExtra("id", -1L)
                    val litres = data.getStringExtra("litres").toDouble()
                    val distance = data.getStringExtra("distance").toDouble()
                    val fuelConsumption = litres * 100 / distance
                    val item = items.find { it.id == id } as FuelConsumptionData
                    item.litres = litres
                    item.distance = distance
                    item.fuelConsumption = fuelConsumption
                    item.time = time
                    dbUtil.updateFuelItem(item)
                    rvAdapter.refreshRecyclerView()
                }
                requestCodeAddLog -> {
                    val title = data.getStringExtra("title")
                    val text = data.getStringExtra("text")
                    val mileage = data.getStringExtra("mileage").toLong()
                    val id = dbUtil.insertLogData(title, text, time, mileage)
                    if (id != -1L) {
                        items.add(LogData(id, time, title, text, mileage))
                        rvAdapter.refreshRecyclerView()
                    }
                }
                requestCodeEditLog -> {
                    val id = data.getLongExtra("id", -1L)
                    val title = data.getStringExtra("title")
                    val text = data.getStringExtra("text")
                    val mileage = data.getStringExtra("mileage").toLong()
                    val item = items.find { it.id == id } as LogData
                    item.title = title
                    item.text = text
                    item.mileage = mileage
                    dbUtil.updateLogItem(item)
                    rvAdapter.refreshRecyclerView()
                }
            }
        }
    }

    private fun callLogEditActivityForResult(activityClass: Class<*>, item: LogData) {
        val intent = Intent(this, activityClass)
        intent.putExtra("title", R.string.activity_log_title_edit)
        intent.putExtra("id", item.id)
        intent.putExtra("time", item.time)
        intent.putExtra("log_title", item.title)
        intent.putExtra("mileage", item.mileage)
        intent.putExtra("text", item.text)
        startActivityForResult(intent, requestCodeEditLog)
    }

    private fun callFuelConsumptionEditActivityForResult(activityClass: Class<*>, item: FuelConsumptionData) {
        val intent = Intent(this, activityClass)
        intent.putExtra("title", R.string.data_dialog_title_edit)
        intent.putExtra("positive_button", R.string.data_dialog_positive_button_edit)
        intent.putExtra("id", item.id)
        intent.putExtra("time", item.time)
        intent.putExtra("litres", item.litres)
        intent.putExtra("distance", item.distance)
        startActivityForResult(intent, requestCodeEditFuelConsumption)
    }

    private fun select(type: DataType) {
        items.clear()
        when (type) {
            DataType.LOG -> items.addAll(dbUtil.selectLogData())
            DataType.FUEL -> items.addAll(dbUtil.selectFuelData())
        }
        rvAdapter.refreshRecyclerView()
    }
}