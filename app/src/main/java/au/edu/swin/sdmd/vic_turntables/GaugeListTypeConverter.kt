package au.edu.swin.sdmd.vic_turntables

import androidx.room.TypeConverter

//Type converters for gauges list for compatibility with database
class GaugeListTypeConverter {
    //Turn MutableList<Int> into string with / separators
    @TypeConverter
    fun gaugeListToString(gaugeList : MutableList<Int>) : String {
        return if (gaugeList.size == 0) { "" }
        else {
            gaugeList.joinToString("/") { it.toString() }
        }
    }

    //Turn string with / separators into MutableList<Int>
    @TypeConverter
    fun stringToGaugeList(gaugeString : String?) : MutableList<Int> {
        return when (gaugeString) {
            null -> { mutableListOf<Int>() }
            "" -> { mutableListOf<Int>() }
            else -> {
                gaugeString.split("/").map { it.toInt() }.toMutableList()
            }
        }
    }
}