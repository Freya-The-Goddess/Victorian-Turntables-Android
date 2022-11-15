package au.edu.swin.sdmd.vic_turntables

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Parcel
import android.os.Parcelable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.android.material.color.MaterialColors

//Database entity object to store turntable data
@Entity(tableName = "TurntableDatabaseTable")
@TypeConverters(GaugeListTypeConverter::class)
data class TurntableEntity(
    @ColumnInfo(name = "latitude")   var latitude   : Double,           // Latitude with 7 digits of precision
    @ColumnInfo(name = "longitude")  var longitude  : Double,           // Longitude with 7 digits of precision
    @ColumnInfo(name = "name")       var name       : String,           // Turntable common name
    @ColumnInfo(name = "location")   var location   : String,           // Location string
    @ColumnInfo(name = "status")     var status     : String,           // Status string (in use / disused)
    @ColumnInfo(name = "gauges")     var gauges     : MutableList<Int>, // List of rail gauges in integer millimetres
    @ColumnInfo(name = "lengthFeet") var lengthFeet : Int,              // Turntable integer length in feet
    @ColumnInfo(name = "power")      var power      : String,           // Turntable power method (manual / electric / petrol)
    @ColumnInfo(name = "notes")      var notes      : String            // Additional notes about turntable
) : Parcelable {
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "uid") var uid : Int = 0 //Primary key is auto generated unique ID

    //Convert turntable length from feet to metres
    private fun lengthMeters() : Double {
        return lengthFeet.toDouble() * 0.3048
    }

    //Get human readable string of length in feet and metres
    fun lengthString() : String {
        return "$lengthFeet' (${String.format("%.2f", lengthMeters())}m)"
    }

    //Get human readable string of turntable gauges
    fun gaugeString() : String {
        var gaugeString = ""
        gauges.forEachIndexed { index, gaugeInt ->
            if (gaugeInt < 1435) { //narrow gauge
                gaugeString += "NG ${gaugeInt}mm"
            }
            else if (gaugeInt == 1435) { //standard gauge
                gaugeString += "SG ${gaugeInt}mm"
            }
            else if (gaugeInt > 1435) { //broad gauge
                gaugeString += "BG ${gaugeInt}mm"
            }
            if (index < gauges.size-1) { //if there is another gauge after add separator
                gaugeString += " / "
            }
        }
        return gaugeString
    }
    
    fun drawIcon(context : Context) : Drawable? {
        val fgDrawable = ContextCompat.getDrawable(context, R.drawable.ic_turntable)
        val bgDrawable = ContextCompat.getDrawable(context, R.drawable.ic_turntable_background)

        return if (fgDrawable!=null && bgDrawable!=null) {
            val fgWrapped = DrawableCompat.wrap(fgDrawable.mutate())
            val bgWrapped = DrawableCompat.wrap(bgDrawable.mutate())

            //set layer colors
            DrawableCompat.setTint(fgWrapped,
                MaterialColors.getColor(context, android.R.attr.textColorPrimary, Color.BLACK)
            )
            DrawableCompat.setTint(bgWrapped, statusColor(context))

            //combine layers and return drawable
            LayerDrawable(arrayOf(bgWrapped, fgWrapped))
        }
        else { null }
    }

    //select color based on status
    private fun statusColor(context : Context) : Int {
        return when (status) {
            //In use
            context.resources.getString(R.string.statusInUse) -> {
                MaterialColors.getColor(context, R.attr.statusInUse, Color.BLACK)
            }
            //Heritage mainline use
            context.resources.getString(R.string.statusHeritageMainlineUse) -> {
                MaterialColors.getColor(context, R.attr.statusHeritageMainlineUse, Color.BLACK)
            }
            //Heritage line use
            context.resources.getString(R.string.statusHeritageLineUse) -> {
                MaterialColors.getColor(context, R.attr.statusHeritageLineUse, Color.BLACK)
            }
            //Heritage line disused
            context.resources.getString(R.string.statusHeritageLineDisused) -> {
                MaterialColors.getColor(context, R.attr.statusHeritageLineDisused, Color.BLACK)
            }
            //Disused
            context.resources.getString(R.string.statusDisused) -> {
                MaterialColors.getColor(context, R.attr.statusDisused, Color.BLACK)
            }
            //Disconnected
            context.resources.getString(R.string.statusDisconnected) -> {
                MaterialColors.getColor(context, R.attr.statusDisconnected, Color.BLACK)
            }
            //Isolated
            context.resources.getString(R.string.statusIsolated) -> {
                MaterialColors.getColor(context, R.attr.statusIsolated, Color.BLACK)
            }
            //Stored
            context.resources.getString(R.string.statusStored) -> {
                MaterialColors.getColor(context, R.attr.statusStored, Color.BLACK)
            }
            else -> {
                MaterialColors.getColor(context, R.attr.itemBackgroundColor, Color.BLACK)
            }
        }
    }

    //deserialize from parcel constructor
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),            //latitude
        parcel.readDouble(),            //longitude
        parcel.readString().toString(), //name
        parcel.readString().toString(), //location
        parcel.readString().toString(), //status
        GaugeListTypeConverter().stringToGaugeList(parcel.readString()), //gauges
        parcel.readInt(),               //lengthFeet
        parcel.readString().toString(), //power
        parcel.readString().toString()  //notes
    ) {
        uid = parcel.readInt()          //uid
    }

    //serialize to parcel function
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeString(name)
        parcel.writeString(location)
        parcel.writeString(status)
        parcel.writeString(GaugeListTypeConverter().gaugeListToString(gauges))
        parcel.writeInt(lengthFeet)
        parcel.writeString(power)
        parcel.writeString(notes)
        parcel.writeInt(uid)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TurntableEntity> {
        override fun createFromParcel(parcel: Parcel): TurntableEntity {
            return TurntableEntity(parcel)
        }

        override fun newArray(size: Int): Array<TurntableEntity?> {
            return arrayOfNulls(size)
        }
    }
}
