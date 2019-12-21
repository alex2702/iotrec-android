package de.ikas.iotrec.app

import android.util.Log
import android.content.Intent
import android.app.*
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import de.ikas.iotrec.R
//import de.ikas.iotrec.bluetooth.service.BluetoothScannerService
import de.ikas.iotrec.network.IotRecApiInit
import org.altbeacon.beacon.*
import org.altbeacon.beacon.powersave.BackgroundPowerSaver
import de.ikas.iotrec.account.data.LoginRepository
import de.ikas.iotrec.recommendation.RecommendationCheckerService
import de.ikas.iotrec.database.db.IotRecDatabase
import de.ikas.iotrec.database.model.Thing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager
import com.mikepenz.iconics.Iconics
import de.ikas.iotrec.database.dao.*
import de.ikas.iotrec.database.repository.*
import de.ikas.iotrec.network.OpenWeatherApiInit
import okio.blackholeSink


class IotRecApplication : Application(), BeaconConsumer, LocationListener {
    private val TAG = "IotRecApplication"

    // bluetooth and location-related classes
    private lateinit var beaconManager: BeaconManager
    var bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    lateinit var locationManager: LocationManager
    lateinit var location: Location

    // initialize an instance of the API
    val iotRecApi = IotRecApiInit(this)

    // declare instances of all repositories
    lateinit var loginRepository: LoginRepository

    private lateinit var categoryDao: CategoryDao
    lateinit var categoryRepository: CategoryRepository

    private lateinit var preferenceDao: PreferenceDao
    lateinit var preferenceRepository: PreferenceRepository

    private lateinit var thingsDao: ThingDao
    lateinit var thingRepository: ThingRepository

    private lateinit var recommendationDao: RecommendationDao
    lateinit var recommendationRepository: RecommendationRepository

    private lateinit var feedbackDao: FeedbackDao
    lateinit var feedbackRepository: FeedbackRepository

    private lateinit var ratingDao: RatingDao
    lateinit var ratingRepository: RatingRepository

    private lateinit var experimentDao: ExperimentDao
    lateinit var experimentRepository: ExperimentRepository

    private lateinit var questionDao: QuestionDao
    lateinit var questionRepository: QuestionRepository

    private lateinit var replyDao: ReplyDao
    lateinit var replyRepository: ReplyRepository

    val openWeatherApi = OpenWeatherApiInit(this)

    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()

        // initialize all objects declared above

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)

        categoryDao = IotRecDatabase.getDatabase(this, GlobalScope).categoryDao()
        categoryRepository = CategoryRepository(categoryDao)

        thingsDao = IotRecDatabase.getDatabase(this, GlobalScope).thingDao()
        thingRepository = ThingRepository(thingsDao)

        preferenceDao = IotRecDatabase.getDatabase(this, GlobalScope).preferenceDao()
        preferenceRepository = PreferenceRepository(preferenceDao, categoryDao)

        recommendationDao = IotRecDatabase.getDatabase(this, GlobalScope).recommendationDao()
        recommendationRepository = RecommendationRepository(recommendationDao)

        feedbackDao = IotRecDatabase.getDatabase(this, GlobalScope).feedbackDao()
        feedbackRepository = FeedbackRepository(feedbackDao)

        ratingDao = IotRecDatabase.getDatabase(this, GlobalScope).ratingDao()
        ratingRepository = RatingRepository(ratingDao)

        experimentDao = IotRecDatabase.getDatabase(this, GlobalScope).experimentDao()
        experimentRepository = ExperimentRepository(iotRecApi, experimentDao)

        questionDao = IotRecDatabase.getDatabase(this, GlobalScope).questionDao()
        questionRepository = QuestionRepository(questionDao)

        replyDao = IotRecDatabase.getDatabase(this, GlobalScope).replyDao()
        replyRepository = ReplyRepository(iotRecApi, replyDao)

        loginRepository = LoginRepository(iotRecApi, this)

        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // set up location manager according to device's capabilities
        val provider: String
        if(locationManager.allProviders.contains("network")) {
            provider = LocationManager.NETWORK_PROVIDER
        } else if(locationManager.allProviders.contains("passive")) {
            provider = LocationManager.PASSIVE_PROVIDER
        } else {
            provider = LocationManager.GPS_PROVIDER
        }
        location = Location(provider)

        // try to get the location here, so it's available once needed for weather context data
        try {
            locationManager.requestLocationUpdates(
                provider,
                60000,
                100f,
                this
            )
        } catch (e: SecurityException) {
            Log.d(TAG, e.toString())
        }

        prepareBluetoothScanning()

        // set up for icon library
        Iconics.init(applicationContext)
    }

    fun prepareBluetoothScanning() {
        // set up beacon manager
        beaconManager = BeaconManager.getInstanceForApplication(this)

        // iBeacon layout
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))
        // Eddystone UID layout
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT))
        // other possible layout types
        //beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT))
        //beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT))
        //beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.URI_BEACON_LAYOUT))
        //beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT))

        // debugging prints verbose scanning information
        BeaconManager.setDebug(false)

        // notification for foreground service
        val builder = NotificationCompat.Builder(this, TAG)
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setContentTitle("Scanning for Beacons")
        builder.setGroup("IOTREC_PERSISTENT_GROUP")

        // notification's intent points to MainActivity
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        builder.setContentIntent(pendingIntent)

        // notification channel is needed from Android O on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "IotRec Persistent Notification Channel ID",
                "IotRec Beacon Scan Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "IotRec Persistent Notification Channel Description"
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            builder.setChannelId(channel.id)
        }

        val notification = builder.build()

        // set up scan
        beaconManager.enableForegroundServiceScanning(notification, 456)
        beaconManager.setEnableScheduledScanJobs(false)
        beaconManager.foregroundBetweenScanPeriod = 0
        beaconManager.foregroundScanPeriod = 1500
        beaconManager.backgroundBetweenScanPeriod = 0
        beaconManager.backgroundScanPeriod = 1500

        // beacon library mandates mere presence of this object to enable some power saving features
        val backgroundPowerSaver = BackgroundPowerSaver(this)
    }

    fun startBluetoothScanning() {
        beaconManager.bind(this)
    }


    override fun onBeaconServiceConnect() {
        // remove any existing range notifiers
        beaconManager.removeAllRangeNotifiers()

        // create a new range notifier
        beaconManager.addRangeNotifier(object : RangeNotifier {
            override fun didRangeBeaconsInRegion(beacons: Collection<Beacon>, region: Region) {
                // empty thing to hold things in range, will be populated below
                val rangedBeacons: MutableList<Thing> = mutableListOf()

                // loop through all beacons
                for (beacon in beacons) {
                    // filter out some of the test beacons
                    if(
                        beacon.id1.toString() != "00000000-0000-0000-0000-000000000000" &&
                        beacon.id1.toString() != "490385fd-427d-42ae-9198-3b27b6dfd1c8" &&
                        beacon.id1.toString() != "18bd9ed1-1c6e-4419-8204-e924d68d065e" &&
                        beacon.id1.toString() != "cb443d13-f04e-49f4-a973-944d13cf3a67"
                    ) {
                        // create object for newly discovered thing
                        var beaconType = ""
                        var beaconId = ""
                        var iBeaconUuid = ""
                        var iBeaconMajor = 0
                        var iBeaconMinor = 0
                        var eddystoneNamespaceId = ""
                        var eddystoneInstanceId = ""

                        //check if there is an active experiment and if yes, modify the thing's ID (attach scenario ID)
                        var scenarioForId = ""
                        val experimentCurrentRun = sharedPrefs.getInt("experimentCurrentRun", 0)
                        val experimentCurrentScenario = sharedPrefs.getString("experimentCurrentScenario", "")
                        if(experimentCurrentRun > 0 && experimentCurrentScenario != "") {
                            scenarioForId = "-$experimentCurrentScenario"
                        }

                        // find out type of beacon, typecode 215 is iBeacon
                        if (beacon.beaconTypeCode.toString(16) == "215") {
                            beaconType = "BCN_I"
                            beaconId = beacon.id1.toString() + "-" + beacon.id2.toString() + "-" + beacon.id3.toString() + scenarioForId
                            iBeaconUuid = beacon.id1.toString()
                            iBeaconMajor = beacon.id2.toInt()
                            iBeaconMinor = beacon.id3.toInt()
                        } else {
                            beaconType = "BCN_EDDY"
                            beaconId = beacon.id1.toString() + "-" + beacon.id2.toString() + scenarioForId
                            eddystoneNamespaceId = beacon.id1.toString()
                            eddystoneInstanceId = beacon.id2.toString()
                        }

                        // create a bare thing object from the bluetooth data
                        val thing = Thing(
                            beaconId,
                            beaconType,
                            beacon.bluetoothName ?: "Bluetooth Device",
                            "",
                            iBeaconUuid,
                            iBeaconMajor,
                            iBeaconMinor,
                            eddystoneNamespaceId,
                            eddystoneInstanceId,
                            beacon.bluetoothName ?: "unknown beacon",
                            beacon.distance,
                            beacon.beaconTypeCode,
                            beacon.bluetoothAddress,
                            beacon.rssi,
                            beacon.txPower,
                            true,
                            Date(), // lastSeen
                            Date(0), // lastQueried
                            Date(0), // lastTriedToQuery
                            Date(0), // lastRecommended
                            Date(0), // lastCheckedForRecommendation
                            "",
                            "",
                            0,
                            false
                        )

                        // add to list initialized above
                        rangedBeacons.add(thing)

                        // insert object into database
                        GlobalScope.launch(Dispatchers.IO) {
                            // try to get existing db entry
                            val thingInDatabase = thingRepository.getThing(thing.id)
                            val thingExistsInDatabase = thingInDatabase != null

                            // if the thing exists, update some properties
                            if (thingExistsInDatabase) {
                                /* DEBUGGING LOGS
                                val thingWasSeenGTE30SecondsAgo = (thing.lastSeen!!.time - thingInDatabase.lastSeen!!.time)/1000 >= 30
                                val thingWasSeenLT90SecondsAgo = (thing.lastSeen!!.time - thingInDatabase.lastSeen!!.time)/1000 < 90
                                val thingWasSeenGTE90SecondsAgo = (thing.lastSeen!!.time - thingInDatabase.lastSeen!!.time)/1000 >= 90

                                if(thingWasSeenGTE30SecondsAgo && thingWasSeenLT90SecondsAgo) {
                                    Log.d(TAG, "Found already active thing in database, last seen " + ((thing.lastSeen!!.time - thingInDatabase.lastSeen!!.time)/1000).toString() + " seconds ago: " + thing.id.toString())
                                } else if(thingWasSeenGTE90SecondsAgo) {
                                    Log.d(TAG, "Found inactive thing in database, last seen " + ((thing.lastSeen!!.time - thingInDatabase.lastSeen!!.time)/1000).toString() + " seconds ago: " + thing.id.toString())
                                }
                                */

                                //if beacon is known already, just update inRange status and connectivity details
                                thingRepository.updateBluetoothData(thing)
                            } else {
                                //if beacon is new, insert entire object
                                thingRepository.insert(thing)
                            }

                            // get network data for beacon
                            // only query if
                            //  - user is logged in AND
                            //  - never fetched successfully and last query is at least 10 seconds ago or
                            //  - previously fetched successfully but last fetch is older than 1 minute
                            val userIsLogegdIn = loginRepository.isLoggedIn()
                            val thingWasNeverFetchedSuccessfully = thingExistsInDatabase && thingInDatabase.lastQueried!!.time == 0L
                            val thingWasFetchedSuccessfullyBefore = thingExistsInDatabase && thingInDatabase.lastQueried!!.time != 0L
                            val queryWasLastTriedGTE10SecondsAgo = thingExistsInDatabase && thingInDatabase.lastTriedToQuery!!.time < Date().time - 1 * 10 * 1000
                            val thingWasLastFetchedSuccessfullyGTE60SecondsAgo = thingExistsInDatabase && thingInDatabase.lastQueried!!.time < Date().time - 1 * 60 * 1000

                            if (userIsLogegdIn && (
                                    (thingExistsInDatabase && thingWasNeverFetchedSuccessfully && queryWasLastTriedGTE10SecondsAgo) ||
                                    (thingExistsInDatabase && thingWasFetchedSuccessfullyBefore && thingWasLastFetchedSuccessfullyGTE60SecondsAgo)
                               )
                            ) {
                                try {
                                    // get thing information from API
                                    val result = iotRecApi.getThing(thing.id)

                                    // if successful, update database object
                                    if (result.isSuccessful) {
                                        val resultThing = result.body()

                                        if (resultThing != null) {
                                            thingRepository.updateBackendData(
                                                resultThing.id,
                                                resultThing.title,
                                                resultThing.description!!,
                                                Date(),
                                                Date(),
                                                thingInDatabase.lastRecommended!!,
                                                thingInDatabase.lastCheckedForRecommendation!!,
                                                if (resultThing.image != null) {
                                                    resultThing.image
                                                } else {
                                                    ""
                                                }!!,
                                                if (resultThing.categories != null) {
                                                    resultThing.categories
                                                } else {
                                                    ""
                                                }!!,
                                                resultThing.occupation
                                            )
                                        } else {
                                            // only update "lastTriedToQuery"
                                            thingRepository.updateBackendData(
                                                thingInDatabase.id,
                                                thingInDatabase.title,
                                                thingInDatabase.description!!,
                                                thingInDatabase.lastQueried!!,
                                                Date(),
                                                thingInDatabase.lastRecommended!!,
                                                thingInDatabase.lastCheckedForRecommendation!!,
                                                if (thingInDatabase.image != null) {
                                                    thingInDatabase.image
                                                } else {
                                                    ""
                                                }!!,
                                                "",
                                                thingInDatabase.occupation
                                            )
                                        }

                                        Log.d(TAG, thingRepository.getThing(thing.id).toString())
                                    } else {
                                        // if not successful,  only update "lastTriedToQuery"
                                        thingRepository.updateBackendData(
                                            thingInDatabase.id,
                                            thingInDatabase.title,
                                            thingInDatabase.description!!,
                                            thingInDatabase.lastQueried!!,
                                            Date(),
                                            thingInDatabase.lastRecommended!!,
                                            thingInDatabase.lastCheckedForRecommendation!!,
                                            if (thingInDatabase.image != null) {
                                                thingInDatabase.image
                                            } else {
                                                ""
                                            }!!,
                                            "",
                                            thingInDatabase.occupation
                                        )
                                    }
                                } catch (e: Throwable) {
                                    Log.d(TAG, e.toString())
                                }
                            }
                        }

                        // start service to check if item is to be recommended
                        val recommendationCheckerServiceIntent = Intent(applicationContext, RecommendationCheckerService()::class.java)
                        recommendationCheckerServiceIntent.putExtra("thingId", thing.id)
                        startService(recommendationCheckerServiceIntent)
                    }
                }

                // clean beacon list of things that weren't in range for a while
                GlobalScope.launch(Dispatchers.IO) {
                    // get all thing with status "inRange=true" in database
                    val databaseThingsInRange = thingRepository.getThingsInRangeList()

                    // find the ones that are not part of current "rangedBeacons" set
                    // AND
                    // have not been seen in the last 60 seconds
                    // note: this only affects the entries in the database, not which beacons are shown (for that, see ThingListFragment and associated classes)
                    val thingsNotInRangeAnymore = databaseThingsInRange.filterNot { rangedBeacons.any { x -> x.id == it.id } || (it.lastSeen!!.time > Date().time - 1 * 60 * 1000) }

                    // set inRange to false for those identified
                    for (thing in thingsNotInRangeAnymore) {
                        thing.inRange = false
                        GlobalScope.launch(Dispatchers.IO) {
                            thingRepository.setThingInRange(thing.id, false)
                        }
                    }

                    Log.d(TAG, "Beacons in Range (DB): " + thingRepository.getThingsInRangeList().size)
                }
            }
        })

        // define a region (needed by altbeacon framework) and then start actual scan
        val region = Region("backgroundRegion", null, null, null)
        beaconManager.startRangingBeaconsInRegion(region)
    }

    // LocationListener overwrites
    override fun onLocationChanged(l: Location?) {
        if (l != null) {
            Log.d(TAG, "location: " + location.toString())
            location = l
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

    }

    override fun onProviderEnabled(provider: String?) {

    }

    override fun onProviderDisabled(provider: String?) {

    }
}