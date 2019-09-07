package by.liauko.siarhei.fcc.activity

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.liauko.siarhei.fcc.R
import by.liauko.siarhei.fcc.adapter.DataAdapter
import by.liauko.siarhei.fcc.database.FCCEntry.columnNameConsumption
import by.liauko.siarhei.fcc.database.FCCEntry.columnNameDistance
import by.liauko.siarhei.fcc.database.FCCEntry.columnNameLitres
import by.liauko.siarhei.fcc.database.FCCEntry.columnNameTime
import by.liauko.siarhei.fcc.database.FCCEntry.tableName
import by.liauko.siarhei.fcc.database.FuelConsumptionCalculatorDBHelper
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
        rvAdapter = DataAdapter(items, resources)

        findViewById<RecyclerView>(R.id.recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = rvAdapter
        }

        val fab = findViewById<FloatingActionButton>(R.id.add_fab)
        fab.setOnClickListener(this)

        select()
    }

    private fun initDatabase() {
        val dbHelper = FuelConsumptionCalculatorDBHelper(applicationContext)
        database = dbHelper.writableDatabase
    }

    override fun onClick(v: View?) = startActivityForResult(Intent(this, AddDialogActivity::class.java), requestCodeAdd)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                requestCodeAdd -> {
                    val fuel = data.getStringExtra("litres").toDouble()
                    val distance = data.getStringExtra("distance").toDouble()
                    val fuelConsumption = fuel * 100 / distance
                    val calendar = Calendar.getInstance() as GregorianCalendar
                    calendar.set(data.getIntExtra("year", 1970),
                            data.getIntExtra("month", 0),
                            data.getIntExtra("day", 1))
                    val id = insert(fuel, distance, fuelConsumption, calendar.timeInMillis)

                    if (id != -1L) {
                        items.add(FuelConsumptionData(fuelConsumption, fuel, distance, calendar))
                        items.sortByDescending { it.date }
                        rvAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private fun select() {
        items.clear()
        val cursor = database.query(tableName, null, "", emptyArray(), null, null, null)

        if (cursor.moveToFirst()) {
            val litresColumnIndex = cursor.getColumnIndex(columnNameLitres)
            val distanceColumnIndex = cursor.getColumnIndex(columnNameDistance)
            val consumptionColumnIndex = cursor.getColumnIndex(columnNameConsumption)
            val timeColumnIndex = cursor.getColumnIndex(columnNameTime)

            do {
                val calendar = GregorianCalendar.getInstance() as GregorianCalendar
                calendar.timeInMillis = cursor.getLong(timeColumnIndex)
                items.add(FuelConsumptionData(cursor.getDouble(consumptionColumnIndex),
                    cursor.getDouble(litresColumnIndex),
                    cursor.getDouble(distanceColumnIndex),
                    calendar))
            } while (cursor.moveToNext())
        }

        cursor.close()
        items.sortByDescending { it.date }
        rvAdapter.notifyDataSetChanged()
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