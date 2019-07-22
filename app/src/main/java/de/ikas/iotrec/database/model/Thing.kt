package de.ikas.iotrec.database.model
import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parceler
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
@Entity(tableName = "thing_table")
data class Thing(
    @PrimaryKey var id: String,
    var title: String,
    var description: String,
    var uuid: String,
    var major: Int,
    var minor: Int,
    var bluetoothName: String,
    var distance: Double,
    var typeCode: Int,
    var macAddress: String,
    var rssi: Int,
    var txPower: Int,
    var inRange: Boolean,
    var lastSeen: Date,
    var lastQueried: Date
    // TODO add image
) : Parcelable