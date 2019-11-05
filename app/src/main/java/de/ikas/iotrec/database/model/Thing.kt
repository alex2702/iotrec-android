package de.ikas.iotrec.database.model
import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parceler
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
@Entity(tableName = "thing_table")
data class Thing(
    @PrimaryKey var id: String,
    var type: String,
    var title: String,
    var description: String?,
    var iBeaconUuid: String?,
    @Json(name = "ibeacon_major_id")
    var iBeaconMajor: Int?,
    @Json(name = "ibeacon_minor_id")
    var iBeaconMinor: Int?,
    @Json(name = "eddystone_namespace_id")
    var eddystoneNamespaceId: String?,
    @Json(name = "eddystone_instance_id")
    var eddystoneInstanceId: String?,
    var bluetoothName: String?,
    var distance: Double?,
    var typeCode: Int?,
    var macAddress: String?,
    var rssi: Int?,
    var txPower: Int?,
    var inRange: Boolean?,
    var lastSeen: Date?,
    var lastQueried: Date?, // last time the query was successful (i.e. the thing was found on the server)
    var lastTriedToQuery: Date?,
    var lastRecommended: Date?,
    var lastCheckedForRecommendation: Date?,
    var image: String?,
    var categories: String?,
    var occupation: Int,
    var recommendationQueryRunning: Boolean
) : Parcelable