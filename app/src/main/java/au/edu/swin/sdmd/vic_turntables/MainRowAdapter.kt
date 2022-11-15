package au.edu.swin.sdmd.vic_turntables

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class MainRowAdapter(
    private val rowClickListener : (TurntableEntity) -> Unit
) : ListAdapter<TurntableEntity, MainRowAdapter.RowViewHolder>(TurntableComparator()) {

    //creates and returns ViewHolder by creating layout inflater
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : RowViewHolder {
        return RowViewHolder.create(parent, rowClickListener)
    }

    //gets country from position in country list and calls bind()
    override fun onBindViewHolder(rowViewHolder : RowViewHolder, position : Int) {
        val turntable = getItem(position)
        rowViewHolder.bind(turntable)
    }

    //view holder class which populates rows with country data and sets click listener
    class RowViewHolder(
        private val rowView : View,
        private val rowClickListener : (TurntableEntity) -> Unit
    ) : RecyclerView.ViewHolder(rowView) {

        @SuppressLint("SetTextI18n") //suppress warning to use I18 localisation of units for length
        fun bind(turntable : TurntableEntity) {
            //populate row text views with country data
            rowView.findViewById<TextView>(R.id.rowTurntableName)
                .text = turntable.name
            rowView.findViewById<TextView>(R.id.rowTurntableLocation)
                .text = turntable.location
            rowView.findViewById<TextView>(R.id.rowTurntableStatus)
                .text = turntable.status
            rowView.findViewById<TextView>(R.id.rowTurntableLength)
                .text = "${turntable.lengthFeet}'"
            rowView.findViewById<TextView>(R.id.rowTurntablePower)
                .text = turntable.power

            //set click listener for row
            rowView.setOnClickListener {
                Log.i("ROW_CLICK",turntable.name)
                rowClickListener(turntable) //callback click listener function in main activity
            }

            //draw icon colored based on status and insert into image view
            rowView.findViewById<ImageView>(R.id.rowTurntableIcon)
                .setImageDrawable(turntable.drawIcon(rowView.context))
        }

        companion object {
            fun create(parent: ViewGroup, rowClickListener : (TurntableEntity) -> Unit): RowViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val rowView = layoutInflater.inflate(
                    R.layout.row_layout_main,
                    parent,
                    false
                ) as View
                return RowViewHolder(rowView, rowClickListener)
            }
        }
    }

    class TurntableComparator : DiffUtil.ItemCallback<TurntableEntity>() {
        override fun areItemsTheSame(oldItem: TurntableEntity, newItem: TurntableEntity): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: TurntableEntity, newItem: TurntableEntity): Boolean {
            return oldItem == newItem
        }
    }
}