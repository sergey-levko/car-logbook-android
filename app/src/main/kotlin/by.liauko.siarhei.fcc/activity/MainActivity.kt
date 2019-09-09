package by.liauko.siarhei.fcc.activity

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.provider.BaseColumns._ID
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.liauko.siarhei.fcc.R
import by.liauko.siarhei.fcc.adapter.DataAdapter
import by.liauko.siarhei.fcc.controller.RecyclerViewSwipeController
import by.liauko.siarhei.fcc.database.FuelConsumptionCalculatorDBHelper
import by.liauko.siarhei.fcc.database.entry.FCCEntry.columnNameConsumption
import by.liauko.siarhei.fcc.database.entry.FCCEntry.columnNameDistance
import by.liauko.siarhei.fcc.database.entry.FCCEntry.columnNameLitres
import by.liauko.siarhei.fcc.database.entry.FCCEntry.columnNameTime
import by.liauko.siarhei.fcc.database.entry.FCCEntry.tableName
import by.liauko.siarhei.fcc.entity.FuelConsumptionData
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val requestCodeAdd = 1

    private lateinit var items: ArrayList<FuelConsumptionData>
    private lateinit var rvAdapter: DataAdapter
    private lateinit var database: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        initDatabase()

        items = mutableListOf<FuelConsumptionData>() as ArrayList<FuelConsumptionData>
        initRecyclerView()

        val fab = findViewById<FloatingActionButton>(R.id.add_fab)
        fab.setOnClickListener(this)

        select()
    }

    private fun initDatabase() {
        val dbHelper = FuelConsumptionCalculatorDBHelper(applicationContext)
        database = dbHelper.writableDatabase
    }

    private fun initRecyclerView() {
        rvAdapter = DataAdapter(items, resources, database)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = rvAdapter
        }

        val helper = ItemTouchHelper(RecyclerViewSwipeController(rvAdapter))
        helper.attachToRecyclerView(recyclerView)
    }

    override fun onClick(v: View?) = startActivityForResult(Intent(this, AddDialogActivity::class.java), requestCodeAdd)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                requestCodeAdd -> {
                    val litres = data.getStringExtra("litres").toDouble()
                    val distance = data.getStringExtra("distance").toDouble()
                    val fuelConsumption = litres * 100 / distance
                    val calendar = Calendar.getInstance() as GregorianCalendar
                    calendar.set(data.getIntExtra("year", 1970),
                            data.getIntExtra("month", 0),
                            data.getIntExtra("day", 1))
                    val id = insert(litres, distance, fuelConsumption, calendar.timeInMillis)

                    if (id != -1L) {
                        items.add(FuelConsumptionData(id, fuelConsumption, litres, distance, calendar))
                        rvAdapter.refreshRecyclerView()
                    }
                }
            }
        }
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
                    val calendar = GregorianCalendar.getInstance() as GregorianCalendar
                    calendar.timeInMillis = it.getLong(timeColumnIndex)
                    items.add(
                        FuelConsumptionData(
                            it.getLong(idColumnIndex),
                            it.getDouble(consumptionColumnIndex),
                            it.getDouble(litresColumnIndex),
                            it.getDouble(distanceColumnIndex),
                            calendar
                        )
                    )
                } while (it.moveToNext())
            }
        }
        rvAdapter.refreshRecyclerView()
    }

    private fun insert(litres: Double, distance: Double, fuelConsumption: Double, time: Long) : Long {
        val contentValues = ContentValues()
        contentValues.put(columnNameLitres, litres)
        contentValues.put(columnNameDistance, distance)
        contentValues.put(columnNameConsumption, fuelConsumption)
        contentValues.put(columnNameTime, time)
        return database.insert(tableName, null, contentValues)
    }
}