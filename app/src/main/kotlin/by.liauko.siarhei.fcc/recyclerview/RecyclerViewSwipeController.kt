package by.liauko.siarhei.fcc.recyclerview

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import by.liauko.siarhei.fcc.R
import by.liauko.siarhei.fcc.entity.DataType
import by.liauko.siarhei.fcc.entity.FuelConsumptionData
import com.google.android.material.snackbar.Snackbar

class RecyclerViewSwipeController(private val adapter: RecyclerViewDataAdapter): ItemTouchHelper.Callback() {
    private val deleteBackground = ColorDrawable(Color.RED)
    private val deleteIcon = adapter.resources.getDrawable(R.drawable.delete_white)

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ) = makeMovementFlags(0, ItemTouchHelper.LEFT)

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ) = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        val deletedItem = adapter.dataSet[position]
        adapter.removeItem(position)
        val snackBar = Snackbar.make(viewHolder.itemView, R.string.data_fragment_snackbar_message,
            Snackbar.LENGTH_LONG
        ).setAction(R.string.data_fragment_snackbar_undo) {
            adapter.restoreItem(deletedItem, position)
        }.addCallback(object: Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)
                if (event != DISMISS_EVENT_ACTION) {
                    var type = DataType.LOG
                    if (deletedItem is FuelConsumptionData) {
                        type = DataType.FUEL
                    }
                    adapter.repositoryCollection.getRepository(type).delete(deletedItem)
                }
            }
        })
        snackBar.show()
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        val itemView = viewHolder.itemView
        val backgroundCornerOffset = 20

        val deleteIconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2
        val deleteIconTop = itemView.top + (itemView.height - deleteIcon.intrinsicHeight) / 2
        val deleteIconBottom = deleteIconTop + deleteIcon.intrinsicHeight
        if (dX < 0) { // Swiping to the left
            val deleteIconLeft = itemView.right - deleteIconMargin - deleteIcon.intrinsicWidth
            val deleteIconRight = itemView.right - deleteIconMargin
            deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)

            deleteBackground.setBounds(itemView.right + dX.toInt() - backgroundCornerOffset,
                itemView.top, itemView.right, itemView.bottom)
        } else {
            deleteBackground.setBounds(0, 0, 0, 0)
        }

        deleteBackground.draw(c)
        deleteIcon.draw(c)
    }
}