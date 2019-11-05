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


class IotRecApplication : Application(), BeaconConsumer, LocationListener {
    private val TAG = "IotRecApplication"
    private lateinit var beaconManager: BeaconManager
    var bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    lateinit var locationManager: LocationManager
    lateinit var location: Location

    val iotRecApi = IotRecApiInit(this)
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

    // user management
    //public var user: User? = null

    //val isLoggedIn: Boolean
    //    get() = user != null

    //fun logout() {
    //    this.user = null
    //}

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "IotRecApplication – onCreate")
        Log.d(TAG, Build.MANUFACTURER)
        Log.d(TAG, Build.VERSION.SDK_INT.toString())
        Log.d(TAG, Build.VERSION_CODES.O_MR1.toString())

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
        questionRepository = QuestionRepository(iotRecApi, questionDao)

        replyDao = IotRecDatabase.getDatabase(this, GlobalScope).replyDao()
        replyRepository = ReplyRepository(iotRecApi, replyDao)

        loginRepository = LoginRepository(iotRecApi, this)

        // gets up-to-date user profile if a user is logged in
        if(loginRepository.isLoggedIn()) {
            //loginRepository.syncUserProfile()
        }

        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val provider: String
        if(locationManager.allProviders.contains("network")) {
            provider = LocationManager.NETWORK_PROVIDER
        } else if(locationManager.allProviders.contains("passive")) {
            provider = LocationManager.PASSIVE_PROVIDER
        } else {
            provider = LocationManager.GPS_PROVIDER
        }
        location = Location(provider)

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

        Iconics.init(applicationContext)
    }

    fun prepareBluetoothScanning() {
        //appLifecycleObserver = IotRecAppLifecycleObserver(this)
        //ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)

        //iotRecDatabase = IotRecDatabase.getDatabase(this, applicationScope)
        //val intent = Intent(this, MainActivity::class.java)
        //val bundle = Bundle()
        //bundle.putParcelable("DATABASE", iotRecDatabase)
        //intent.putExtra("DATABASE", iotRecDatabase)
        //this.startActivity(intent)

        // start bluetooth scanner service
        //startService(Intent(this, BluetoothScannerService()::class.java))
        //ContextCompat.startForegroundService(this, Intent(this, BluetoothScannerService()::class.java))


        // set up beacon manager
        beaconManager = BeaconManager.getInstanceForApplication(this)

        // iBeacon
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))

        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT))
        //beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT))
        //beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT))
        //beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.URI_BEACON_LAYOUT))
        //beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT))

        BeaconManager.setDebug(false)

        // notification for foreground service
        val builder = NotificationCompat.Builder(this, TAG)
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setContentTitle("Scanning for Beacons")

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        builder.setContentIntent(pendingIntent)
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
        //beaconManager.foregroundBetweenScanPeriod = 0
        beaconManager.foregroundBetweenScanPeriod = 0
        //beaconManager.foregroundScanPeriod = 1100
        beaconManager.foregroundScanPeriod = 3000
        //beaconManager.backgroundBetweenScanPeriod = 5000
        beaconManager.backgroundBetweenScanPeriod = 0
        //beaconManager.backgroundScanPeriod = 1100
        beaconManager.backgroundScanPeriod = 3000

        //beaconManager.disableForegroundServiceScanning()

        val backgroundPowerSaver = BackgroundPowerSaver(this)


        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        //    val scanFilterBuilder = ScanFilter.Builder()
        //    scanFilterBuilder.setManufacturerData(0x004c, byteArrayOf())
        //    val scanFilter = scanFilterBuilder.build()
        //}


    }

    fun startBluetoothScanning() {
        beaconManager.bind(this)
    }


    override fun onBeaconServiceConnect() {
        Log.d(TAG, "onBeaconServiceConnect")

        beaconManager.removeAllRangeNotifiers()

        beaconManager.addRangeNotifier(object : RangeNotifier {
            override fun didRangeBeaconsInRegion(beacons: Collection<Beacon>, region: Region) {
                //Log.d(TAG,"=================================================================")

                val rangedBeacons: MutableList<Thing> = mutableListOf()

                for (beacon in beacons) {

                    Log.d(TAG, "Running for beac: ${beacon.toString()}")

                    // filter out some of the test beacons
                    if(
                        beacon.id1.toString() != "00000000-0000-0000-0000-000000000000" &&
                        beacon.id1.toString() != "490385fd-427d-42ae-9198-3b27b6dfd1c8" &&
                        beacon.id1.toString() != "18bd9ed1-1c6e-4419-8204-e924d68d065e" &&
                        beacon.id1.toString() != "cb443d13-f04e-49f4-a973-944d13cf3a67"
                    ) {


                        /*
                        try {
                            if(beacon.id1 != null) Log.d(TAG,"beaconData - id1: " + beacon.id1.toString())
                            if(beacon.id2 != null) Log.d(TAG,"beaconData - id2: " + beacon.id2.toString())
                            //if(beacon.id3 != null) Log.d(TAG,"beaconData - id3: " + beacon.id3.toString())
                            if(beacon.bluetoothName != null) Log.d(TAG,"beaconData - bluetoothName: " + beacon.bluetoothName)
                            if(beacon.distance != null) Log.d(TAG,"beaconData - distance: " + beacon.distance)
                            Log.d(TAG, "beaconData - dataFields: " + beacon.dataFields?.toString())
                            Log.d(TAG, "beaconData - extraDataFields: " + beacon.extraDataFields?.toString())
                            Log.d(TAG, "beaconData - beaconTypeCode: " + beacon.beaconTypeCode.toString(16))
                            Log.d(TAG, "beaconData - manufacturer: " + beacon.manufacturer.toString(16))
                            Log.d(TAG, "beaconData - parserIdentifier: " + beacon.parserIdentifier?.toString())
                            if(beacon.bluetoothAddress != null) Log.d(TAG,"beaconData - bluetoothAddress: " + beacon.bluetoothAddress)
                        } catch(e: IndexOutOfBoundsException) {
                            Log.d(TAG, "IndexOutOfBoundsException")
                        }
                        */

                        // create object for newly discovered thing
                        var beaconType = ""
                        var beaconId = ""
                        var beaconIdNew = ""
                        var iBeaconUuid = ""
                        var iBeaconMajor = 0
                        var iBeaconMinor = 0
                        var eddystoneNamespaceId = ""
                        var eddystoneInstanceId = ""

                        //check if there is an active experiment and if yes, modify the thing's ID
                        var scenarioForId = ""
                        val experimentCurrentRun = sharedPrefs.getInt("experimentCurrentRun", 0)
                        val experimentCurrentScenario = sharedPrefs.getString("experimentCurrentScenario", "")

                        if(experimentCurrentRun > 0 && experimentCurrentScenario != "") {
                            scenarioForId = "-$experimentCurrentScenario"
                        }

                        if (beacon.beaconTypeCode.toString(16) == "215") {
                            beaconType = "BCN_I"
                            beaconId =
                                beacon.id1.toString() + "-" + beacon.id2.toString() + "-" + beacon.id3.toString() + scenarioForId
                            iBeaconUuid = beacon.id1.toString()
                            iBeaconMajor = beacon.id2.toInt()
                            iBeaconMinor = beacon.id3.toInt()
                        } else {
                            beaconType = "BCN_EDDY"
                            beaconId = beacon.id1.toString() + "-" + beacon.id2.toString() + scenarioForId
                            eddystoneNamespaceId = beacon.id1.toString()
                            eddystoneInstanceId = beacon.id2.toString()
                        }



                        Log.d(TAG, "beacId is ${beaconId}")




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

                        rangedBeacons.add(thing)

                        // insert object into database
                        GlobalScope.launch(Dispatchers.IO) {

                            val thingInDatabase = thingRepository.getThing(thing.id)

                            if (thingInDatabase != null) {
                                if((thing.lastSeen!!.time - thingInDatabase.lastSeen!!.time)/1000 >= 30 && (thing.lastSeen!!.time - thingInDatabase.lastSeen!!.time)/1000 < 90) {
                                    Log.d(TAG, "Found already active thing in database, last seen " + ((thing.lastSeen!!.time - thingInDatabase.lastSeen!!.time)/1000).toString() + " seconds ago: " + thing.id.toString())
                                } else if((thing.lastSeen!!.time - thingInDatabase.lastSeen!!.time)/1000 >= 90) {
                                    Log.d(TAG, "Found inactive thing in database, last seen " + ((thing.lastSeen!!.time - thingInDatabase.lastSeen!!.time)/1000).toString() + " seconds ago: " + thing.id.toString())
                                }
                                //if beacon is known already, just update inRange status and connectivity details
                                thingRepository.updateBluetoothData(thing)
                            } else {
                                Log.d(TAG, "Found new thing: " + thing.id.toString())
                                //if beacon is new, insert entire object
                                thingRepository.insert(thing)
                            }

                            // get network data for beacon
                            // only query if
                            //  - user is logged in AND
                            //  - never fetched successfully and last query is at least 10 seconds ago or
                            //  - previously fetched successfully but last fetch is older than 1 minute
                            if (loginRepository.isLoggedIn() && (
                                        (thingInDatabase != null && thingInDatabase.lastQueried!!.time == 0L && thingInDatabase.lastTriedToQuery!!.time < Date().time - 1 * 10 * 1000) ||
                                                (thingInDatabase != null && thingInDatabase.lastQueried!!.time != 0L && thingInDatabase.lastQueried!!.time < Date().time - 1 * 60 * 1000)
                                        )
                            ) {
                                try {

                                    Log.d(TAG, "getting thing ${thing.id}")

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
                                                Date(0),
                                                Date(0),
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
                                                Date(0),
                                                Date(),
                                                Date(0),
                                                Date(0),
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
                                        // only update "lastTriedToQuery"
                                        thingRepository.updateBackendData(
                                            thingInDatabase.id,
                                            thingInDatabase.title,
                                            thingInDatabase.description!!,
                                            Date(0),
                                            Date(),
                                            Date(0),
                                            Date(0),
                                            if (thingInDatabase.image != null) {
                                                thingInDatabase.image
                                            } else {
                                                ""
                                            }!!,
                                            "",
                                            thingInDatabase.occupation
                                        )
                                    }
                                    //} catch(e: SocketTimeoutException) {
                                    //    Log.d(TAG, e.toString())
                                    //} catch(e: ConnectException) {
                                    //    // show snackbar to notify of failed connection
                                    //    sendBroadcast(R.string.network_failed.toString())
                                } catch (e: Throwable) {
                                    Log.d(TAG, e.toString())
                                    //sendBroadcast(e.toString())
                                }
                            }
                        }

                        // check if item is to be recommended
                        //ContextCompat.startForegroundService(applicationContext, Intent(applicationContext, RecommendationCheckerService()::class.java))
                        //startService(Intent(applicationContext, RecommendationCheckerService()::class.java))

                        val recommendationCheckerServiceIntent =
                            Intent(applicationContext, RecommendationCheckerService()::class.java)
                        recommendationCheckerServiceIntent.putExtra("thingId", thing.id)
                        startService(recommendationCheckerServiceIntent)







                        /*
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

                        rangedBeacons.add(thing)

                        // insert object into database
                        GlobalScope.launch(Dispatchers.IO) {

                            val thingInDatabase = thingRepository.getThing(thing.id)

                            if (thingInDatabase != null) {
                                if((thing.lastSeen!!.time - thingInDatabase.lastSeen!!.time)/1000 >= 30 && (thing.lastSeen!!.time - thingInDatabase.lastSeen!!.time)/1000 < 90) {
                                    Log.d(TAG, "Found already active thing in database, last seen " + ((thing.lastSeen!!.time - thingInDatabase.lastSeen!!.time)/1000).toString() + " seconds ago: " + thing.id.toString())
                                } else if((thing.lastSeen!!.time - thingInDatabase.lastSeen!!.time)/1000 >= 90) {
                                    Log.d(TAG, "Found inactive thing in database, last seen " + ((thing.lastSeen!!.time - thingInDatabase.lastSeen!!.time)/1000).toString() + " seconds ago: " + thing.id.toString())
                                }
                                //if beacon is known already, just update inRange status and connectivity details
                                thingRepository.updateBluetoothData(thing)
                            } else {
                                Log.d(TAG, "Found new thing: " + thing.id.toString())
                                //if beacon is new, insert entire object
                                thingRepository.insert(thing)
                            }

                            // get network data for beacon
                            // only query if
                            //  - user is logged in AND
                            //  - never fetched successfully and last query is at least 10 seconds ago or
                            //  - previously fetched successfully but last fetch is older than 1 minute
                            if (loginRepository.isLoggedIn() && (
                                        (thingInDatabase != null && thingInDatabase.lastQueried!!.time == 0L && thingInDatabase.lastTriedToQuery!!.time < Date().time - 1 * 10 * 1000) ||
                                        (thingInDatabase != null && thingInDatabase.lastQueried!!.time != 0L && thingInDatabase.lastQueried!!.time < Date().time - 1 * 60 * 1000)
                                        )
                            ) {
                                try {

                                    Log.d(TAG, "getting thing ${thing.id}")

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
                                                Date(0),
                                                Date(0),
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
                                                Date(0),
                                                Date(),
                                                Date(0),
                                                Date(0),
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
                                        // only update "lastTriedToQuery"
                                        thingRepository.updateBackendData(
                                            thingInDatabase.id,
                                            thingInDatabase.title,
                                            thingInDatabase.description!!,
                                            Date(0),
                                            Date(),
                                            Date(0),
                                            Date(0),
                                            if (thingInDatabase.image != null) {
                                                thingInDatabase.image
                                            } else {
                                                ""
                                            }!!,
                                            "",
                                            thingInDatabase.occupation
                                        )
                                    }
                                    //} catch(e: SocketTimeoutException) {
                                    //    Log.d(TAG, e.toString())
                                    //} catch(e: ConnectException) {
                                    //    // show snackbar to notify of failed connection
                                    //    sendBroadcast(R.string.network_failed.toString())
                                } catch (e: Throwable) {
                                    Log.d(TAG, e.toString())
                                    //sendBroadcast(e.toString())
                                }
                            }
                        }

                        // check if item is to be recommended
                        //ContextCompat.startForegroundService(applicationContext, Intent(applicationContext, RecommendationCheckerService()::class.java))
                        //startService(Intent(applicationContext, RecommendationCheckerService()::class.java))

                        val recommendationCheckerServiceIntent =
                            Intent(applicationContext, RecommendationCheckerService()::class.java)
                        recommendationCheckerServiceIntent.putExtra("thingId", thing.id)
                        startService(recommendationCheckerServiceIntent)
                        */
                    }
                }

                GlobalScope.launch(Dispatchers.IO) {

                    // get all beacons with status "inRange=true" in database
                    val databaseBeaconsInRange = thingRepository.getThingsInRangeList()

                    // find the ones that are not part of current "beacons" set
                    // only filter out if beacon hasn't been seen for at least 45 seconds
                    val beaconsNotInRangeAnymore = databaseBeaconsInRange.filterNot { rangedBeacons.any { x -> x.id == it.id } || (it.lastSeen!!.time > Date().time - 45 * 1000)}

                    // set inRange to false for those identified
                    for (beacon in beaconsNotInRangeAnymore) {
                        //Log.d(TAG, "beacon not in range anymore: " + beacon.id)
                        beacon.inRange = false

                        GlobalScope.launch(Dispatchers.IO) {
                            thingRepository.setThingInRange(beacon.id, false)
                        }
                    }

                    Log.d(TAG, "Beacons in Range (DB): " + thingRepository.getThingsInRangeList().size)
                }
            }
        })

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


/*
class IotRecApplication : Application(), BootstrapNotifier/*, BeaconConsumer, RangeNotifier*/ {

    private val TAG = "IotRecApplication"
    private var regionBootstrap: RegionBootstrap? = null
    private var backgroundPowerSaver: BackgroundPowerSaver? = null
    //private var haveDetectedBeaconsSinceBoot = false
    private var rangingActivity: MainActivity? = null
    var beaconManager: BeaconManager? = null

    // Called when the application is starting, before any other application objects have been created.
    // Overriding this method is totally optional!
    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "App started up")

        beaconManager = BeaconManager.getInstanceForApplication(this)
        beaconManager.setDebug(true)
        beaconManager!!.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))


        //val region = Region("backgroundRegion", null, null, null)
        //regionBootstrap = RegionBootstrap(this, region)



        /*
        backgroundPowerSaver = BackgroundPowerSaver(this)

        beaconManager!!.backgroundBetweenScanPeriod = 30000L
        beaconManager!!.foregroundBetweenScanPeriod = 2000L
        beaconManager!!.bind(this)
        */

        //val beaconManager = BeaconManager.getInstanceForApplication(this)

        //beaconManager.foregroundScanPeriod = 1100
        //beaconManager.foregroundBetweenScanPeriod = 0
        //beaconManager.backgroundScanPeriod = 1100
        //beaconManager.backgroundBetweenScanPeriod = 0

        // set iBeacon layout
        //beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))


        val builder = Notification.Builder(this)
        builder.setSmallIcon(R.mipmap.ic_launcher)
        val contentTitle = builder.setContentTitle("Scanning for Beacons")
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        builder.setContentIntent(pendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "My Notification Channel ID",
                "My Notification Name", NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "My Notification Channel Description"
            val notificationManager = getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            builder.setChannelId(channel.id)
        }

        beaconManager!!.enableForegroundServiceScanning(builder.build(), 456)

        // For the above foreground scanning service to be useful, you need to disable
        // JobScheduler-based scans (used on Android 8+) and set a fast background scan
        // cycle that would otherwise be disallowed by the operating system.
        //
        beaconManager!!.setEnableScheduledScanJobs(false)
        beaconManager!!.backgroundBetweenScanPeriod = 0
        beaconManager!!.backgroundScanPeriod = 1000L
        beaconManager!!.foregroundBetweenScanPeriod = 0
        beaconManager!!.foregroundScanPeriod = 1000L

        //LogManager.setLogger(Loggers.verboseLogger())
        //LogManager.setVerboseLoggingEnabled(true)

        Log.d(TAG, "setting up background monitoring for beacons and power saving");

        // wake up the app when a beacon is seen
        val region = Region("backgroundRegion", null, null, null)
        regionBootstrap = RegionBootstrap(this, region)

        // simply constructing this class and holding a reference to it in your custom Application
        // class will automatically cause the BeaconLibrary to save battery whenever the application
        // is not visible.  This reduces bluetooth power usage by about 60%
        backgroundPowerSaver = BackgroundPowerSaver(this);
    }

        // Called by the system when the device configuration changes while your component is running.
    // Overriding this method is totally optional!
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    // This is called when the overall system is running low on memory,
    // and would like actively running processes to tighten their belts.
    // Overriding this method is totally optional!
    override fun onLowMemory() {
        super.onLowMemory()
    }

    override fun didDetermineStateForRegion(p0: Int, p1: Region?) {
        Log.d(TAG, "Current region state is: " + if (p0 === 1) "INSIDE" else "OUTSIDE ($p0)")
    }

    override fun didEnterRegion(p0: Region) {

        /*
        // In this example, this class sends a notification to the user whenever a Beacon
        // matching a Region (defined above) are first seen.
        Log.d(TAG, "did enter region.")
        if (!haveDetectedBeaconsSinceBoot) {
            Log.d(TAG, "auto launching MainActivity")

            // The very first time since boot that we detect an beacon, we launch the
            // MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            // Important:  make sure to add android:launchMode="singleInstance" in the manifest
            // to keep multiple copies of this activity from getting created if the user has
            // already manually launched the app.
            this.startActivity(intent)
            haveDetectedBeaconsSinceBoot = true
        } else {
            if (monitoringActivity != null) {
                // If the Monitoring Activity is visible, we log info about the beacons we have
                // seen on its display
                Log.d(TAG, "I see a beacon again")
            } else {
                // If we have already seen beacons before, but the monitoring activity is not in
                // the foreground, we send a notification to the user on subsequent detections.
                Log.d(TAG, "Sending notification.")
                sendNotification()
            }
        }
        */

        /*
        Log.d(TAG, "did enter region.")
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        this.startActivity(intent)
        */

        Log.d(TAG, "did enter region.")
        try {
            beaconManager?.startRangingBeaconsInRegion(p0)
        } catch (e: RemoteException) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Can't start ranging")
        }

    }

    override fun didExitRegion(p0: Region) {
        Log.d(TAG, "I no longer see a beacon.")

        try {
            beaconManager?.stopRangingBeaconsInRegion(p0)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }

    }

    fun setRangingActivity(activity: MainActivity) {
        this.rangingActivity = activity
    }

    fun disableMonitoring() {
        if (regionBootstrap != null) {
            regionBootstrap!!.disable()
            regionBootstrap = null
        }
    }

    fun enableMonitoring() {
        val region = Region("backgroundRegion", null, null, null)
        regionBootstrap = RegionBootstrap(this, region)
    }


    /*
    private fun sendNotification(text: String) {
        /*
        val builder = NotificationCompat.Builder(this)
            .setContentTitle("Beacon Reference Application")
            .setContentText("An beacon is nearby.")
            .setSmallIcon(R.mipmap.ic_launcher)

        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addNextIntent(Intent(this, MainActivity::class.java))
        val resultPendingIntent = stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        builder.setContentIntent(resultPendingIntent)
        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, builder.build())
        */

        val builder = NotificationCompat.Builder(this)
            .setContentTitle("Beacon Reference Application")
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)

        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addNextIntent(Intent(this, MainActivity::class.java))
        val resultPendingIntent = stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        builder.setContentIntent(resultPendingIntent)
        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, builder.build())
    }

    override fun onBeaconServiceConnect() {
        beaconManager?.setRangeNotifier(this)
    }

    override fun didRangeBeaconsInRegion(beacons: MutableCollection<Beacon>?, p1: Region?) {
        if (beacons != null) {
            if (beacons.size > 0) {
                for (b in beacons) {
                    if (b.id2.toString() == "0x6d767674636e") {
                        Log.e(TAG, "Beacon with my Instance ID found!")
                        sendNotification("Beacon with my Instance ID found!")
                    }
                }
            }
        }
    }
    */
}
*/
