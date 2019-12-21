package de.ikas.iotrec.network.adapter

import android.util.Log
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import de.ikas.iotrec.database.model.Thing
import de.ikas.iotrec.network.json.ThingJson
import java.util.*

/**
 * Used by Moshi to convert between JSON and Kotlin objects
 */
internal class ThingJsonAdapter {
    @FromJson
    fun thingFromJson(thingJson: ThingJson): Thing {
        val thing = Thing(
            thingJson.id,
            thingJson.type,
            thingJson.title,
            thingJson.description,
            thingJson.ibeacon_uuid,
            thingJson.ibeacon_major_id,
            thingJson.ibeacon_minor_id,
            thingJson.eddystone_namespace_id,
            thingJson.eddystone_instance_id,
            "",
            0.0,
            0,
            "",
            0,
            0,
            false,
            Date(0),
            Date(0),
            Date(0),
            Date(0),
            Date(0),
            thingJson.image,
            thingJson.categories.joinToString(";"),
            thingJson.occupation,
            false
        )
        return thing
    }

    @ToJson
    fun thingToJson(thing: Thing): ThingJson {
        val categoriesList = arrayListOf<String>()
        if(thing.categories != null) {
            categoriesList.addAll(thing.categories.let { it!!.split(";").map { x -> x.trim() } })
        }

        val json = ThingJson(
            thing.id,
            thing.type,
            thing.title,
            thing.description,
            thing.iBeaconUuid,
            thing.iBeaconMajor,
            thing.iBeaconMinor,
            thing.eddystoneNamespaceId,
            thing.eddystoneInstanceId,
            thing.image,
            categoriesList,
            thing.occupation
        )
        return json
    }
}