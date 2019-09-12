package by.liauko.siarhei.fcc.activity

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.provider.BaseColumns._ID
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
import by.liauko.siarhei.fcc.database.entry.FCCEntry.columnNameConsumption
import by.liauko.siarhei.fcc.database.entry.FCCEntry.columnNameDistance
import by.liauko.siarhei.fcc.database.entry.FCCEntry.columnNameLitres
import by.liauko.siarhei.fcc.database.entry.FCCEntry.columnNameTime
import by.liauko.siarhei.fcc.database.entry.FCCEntry.tableName
import by.liauko.siarhei.fcc.entity.FuelConsumptionData
import by.liauko.siarhei.fcc.recyclerview.RecyclerViewDataAdapter
import by.liauko.siarhei.fcc.recyclerview.RecyclerViewOnItemClickListener
import by.liauko.siarhei.fcc.recyclerview.RecyclerViewSwipeController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import java.util.ArrayList
import java.util.Calendar

class MainActivity : AppCompatActivity(), View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {
    private val requestCodeAdd = 1
    private val requestCodeEdit = 2

    private lateinit var items: ArrayList<FuelConsumptionData>
    private lateinit var rvAdapter: RecyclerViewDataAdapter
    private lateinit var database: SQLiteDatabase
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        items = mutableListOf<FuelConsumptionData>() as ArrayList<FuelConsumptionData>
        initToolbar()
        initDatabase()
        initRecyclerView()
        initNavigationView()

        val fab = findViewById<FloatingActionButton>(R.id.add_fab)
        fab.setOnClickListener(this)

        select()
    }

    private fun initToolbar() {
        toolbar = findViewById(R.id.toolbar)
        toolbar.setTitle(R.string.activity_main_log_title)
        setSupportActionBar(toolbar)
    }

    private fun initDatabase() {
        val dbHelper = FuelConsumptionCalculatorDBHelper(applicationContext)
        database = dbHelper.writableDatabase
    }

    private fun initRecyclerView() {
        rvAdapter = RecyclerViewDataAdapter(items, resources, database, object: RecyclerViewOnItemClickListener {
            override fun onItemClick(item: FuelConsumptionData) {
                callEditActivityForResult(DataDialogActivity::class.java, item)
            }
        })

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = rvAdapter
        }

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

    override fun onClick(v: View?) = startActivityForResult(Intent(this, DataDialogActivity::class.java), requestCodeAdd)

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var result = false

        when (item.itemId) {

        }

        drawerLayout.closeDrawers()
        return result
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            val litres = data.getStringExtra("litres").toDouble()
            val distance = data.getStringExtra("distance").toDouble()
            val fuelConsumption = litres * 100 / distance
            val time = data.getLongExtra("time", Calendar.getInstance().timeInMillis)
            when (requestCode) {
                requestCodeAdd -> {
                    val id = insert(litres, distance, fuelConsumption, time)
                    if (id != -1L) {
                        items.add(FuelConsumptionData(id, fuelConsumption, litres, distance, time))
                        rvAdapter.refreshRecyclerView()
                    }
                }
                requestCodeEdit -> {
                    val id = data.getLongExtra("id", -1L)
                    val item = items.find { it.id == id }
                    if (item != null) {
                        item.litres = litres
                        item.distance = distance
                        item.fuelConsumption = fuelConsumption
                        item.time = time
                        update(item)
                        rvAdapter.refreshRecyclerView()
                    }
                }
            }
        }
    }

    private fun callEditActivityForResult(activityClass: Class<*>, item: FuelConsumptionData) {
        val intent = Intent(this, activityClass)
        intent.putExtra("title", R.string.data_dialog_title_edit)
        intent.putExtra("positive_button", R.string.data_dialog_positive_button_edit)
        intent.putExtra("id", item.id)
        intent.putExtra("time", item.time)
        intent.putExtra("litres", item.litres)
        intent.putExtra("distance", item.distance)
        startActivityForResult(intent, requestCodeEdit)
    }

    private fun select() {
        items.clear()
        database.query(tableName, null, "", emptyArray(), null, null, null).use {
            if (it.moveToFirst()) {
                val idColumnIndex = it.getColumnIndex(_ID)
                val litresColumnIndex = it.getColumnIndex(columnNameLitres)
                val distanceColumnIndex = it.getColumnIndex(columnNameDistance)
                val consumptionColumnIndex = it.getColumnIndex(columnNameConsumption)
                val timeColumnIndex = it.getColumnIndex(columnNameTime)

                do {
                    items.add(
                        FuelConsumptionData(
                            it.getLong(idColumnIndex),
                            it.getDouble(consumptionColumnIndex),
                            it.getDouble(litresColumnIndex),
                            it.getDouble(distanceColumnIndex),
                            it.getLong(timeColumnIndex)
                        )
                    )
                } while (it.moveToNext())
            }
        }
        rvAdapter.refreshRecyclerView()
    }

    private fun insert(litres: Double, distance: Double, fuelConsumption: Double, time: Long)
            = database.insert(tableName, null, fillValues(litres, distance, fuelConsumption, time))

    private fun update(item: FuelConsumptionData) {
        database.update(tableName,
            fillValues(item.litres, item.distance, item.fuelConsumption, item.time),
            "$_ID LIKE ?",
            arrayOf(item.id.toString()))
    }

    private fun fillValues(litres: Double, distance: Double, fuelConsumption: Double, time: Long): ContentValues {
        val values = ContentValues()
        values.put(columnNameLitres, litres)
        values.put(columnNameDistance, distance)
        values.put(columnNameConsumption, fuelConsumption)
        values.put(columnNameTime, time)
        return values
    }
}