package au.edu.swin.sdmd.vic_turntables

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView

//data class holds the current selected state and title
data class GaugeState(
    var selected : Boolean,
    var gaugeTitle : String
)

class GaugeSpinnerAdapter(
    context: Context,
    resource: Int,
    objects : ArrayList<GaugeState>
) : ArrayAdapter<GaugeState>(context, resource, objects) {
    private val mContext = context
    private val mResource = resource
    private val listState = objects
    private var isFromView = false

    //companion object contains constructor
    companion object {
        //createFromResource custom constructor
        fun createFromResource(
            context : Context,
            textArrayResId : Int,
            textViewResId : Int
        ) : GaugeSpinnerAdapter {
            val objects : ArrayList<GaugeState> =
                ArrayList(context.resources.getStringArray(textArrayResId).map {
                    GaugeState(false, it)
                })
            return GaugeSpinnerAdapter(context, textViewResId, objects)
        }
    }

    //convert gauges state list to gauges MutableList<Int>
    fun getGaugesList() : MutableList<Int> {
        val gaugesList = mutableListOf<Int>()
        if (listState[1].selected) { gaugesList.add(610) }  //NG 610mm
        if (listState[2].selected) { gaugesList.add(762) }  //NG 762mm
        if (listState[3].selected) { gaugesList.add(1067) } //NG 1067mm
        if (listState[4].selected) { gaugesList.add(1435) } //SG 1435mm
        if (listState[5].selected) { gaugesList.add(1600) } //BG 1600mm
        return gaugesList
    }

    //convert gauges state list to string
    private fun gaugeString() : String {
        val gaugesList = getGaugesList()
        var gaugeString = ""

        gaugesList.forEachIndexed { index, gaugeInt ->
            gaugeString += "${gaugeInt.toString()}mm" //append gauge to string
            if (index < gaugesList.size-1) { //if there is another gauge after add separator
                gaugeString += " / "
            }
        }

        //if no gauges selected put default string
        if (gaugeString=="") { gaugeString = mContext.getString(R.string.gaugeSelect)}
        return gaugeString //return gauge string
    }

    //populate gauges spinner with existing gauge list
    fun setGauges(gaugesList : MutableList<Int>) {
        //unselect all gauges
        listState.forEachIndexed { position, _ ->
            listState[position].selected = false
        }

        //select all gauges that match parameter list
        gaugesList.forEach { gauge ->
            when (gauge) {
                610 -> { listState[1].selected = true }
                762 -> { listState[2].selected = true }
                1067 -> { listState[3].selected = true }
                1435 -> { listState[4].selected = true }
                1600 -> { listState[5].selected = true }
            }
        }
    }

    //override ArrayAdapter function to call getCustomView
    override fun getDropDownView(
        position : Int,
        convertView : View?,
        parent : ViewGroup
    ) : View {
        return getCustomView(position, convertView, parent)
    }

    //override ArrayAdapter function to call getCustomView
    override fun getView(
        position : Int,
        convertView : View?,
        parent : ViewGroup
    ) : View {
        return getCustomView(position, convertView, parent)
    }

    //custom adapter function
    private fun getCustomView(
        position : Int,
        convertView : View?,
        parent : ViewGroup
    ) : View {
        var itemView = convertView
        val viewHolder : SpinnerViewHolder?

        //create view holder if doesn't exist, else retrieve current view holder
        if (itemView == null) {
            //create view by inflating resource
            val layoutInflater = LayoutInflater.from(mContext)
            itemView = layoutInflater.inflate(
                mResource,
                parent,
                false
            ) as View 
            
            //create view holder and set as view tag
            viewHolder = SpinnerViewHolder()
            viewHolder.checkBox = itemView.findViewById<CheckBox>(R.id.spinnerCheckbox)
            viewHolder.textView = itemView.findViewById<TextView>(R.id.spinnerText)
            itemView.tag = viewHolder
        }
        else {
            //retrieve current view holder
            viewHolder = itemView.tag as SpinnerViewHolder
        }

        //set spinner item text
        viewHolder.textView!!.text = listState[position].gaugeTitle
        //set checkbox to checked state
        isFromView = true
        viewHolder.checkBox!!.isChecked = listState[position].selected
        isFromView = false

        if (position==0) {
            //first row checkbox should be invisible
            viewHolder.checkBox!!.visibility = View.INVISIBLE

            //first row should show selected gauges
            viewHolder.textView!!.text = gaugeString()
        }
        else { viewHolder.checkBox!!.visibility = View.VISIBLE }

        //set checked change listener to update listState
        viewHolder.checkBox!!.setOnCheckedChangeListener{ _, isChecked ->
            if (!isFromView) {
                listState[position].selected = isChecked //update listState bool from checkbox
                notifyDataSetChanged() //update list to show selected gauges
            }
        }
        return itemView //return spinner item view
    }

    //view holder class
    private class SpinnerViewHolder {
        var checkBox : CheckBox? = null
        var textView : TextView? = null
    }
}