package de.ikas.iotrec.network.json

data class ThingJson(
    var id: String,
    var type: String,
    var title: String,
    var description: String?,
    var ibeacon_uuid: String?,
    var ibeacon_major_id: Int?,
    var ibeacon_minor_id: Int?,
    var eddystone_namespace_id: String?,
    var eddystone_instance_id: String?,
    var image: String?,
    var categories: ArrayList<String>,
    var occupation: Int
)