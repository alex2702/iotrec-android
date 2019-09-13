package de.ikas.iotrec.app

import android.util.Log
import android.content.Intent
import android.app.*
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ProcessLifecycleOwner
import de.ikas.iotrec.R
//import de.ikas.iotrec.bluetooth.service.BluetoothScannerService
import de.ikas.iotrec.network.IotRecApiInit
import org.altbeacon.beacon.*
import org.altbeacon.beacon.powersave.BackgroundPowerSaver
import android.bluetooth.le.ScanFilter
import android.provider.Settings
import androidx.annotation.RequiresApi
import de.ikas.iotrec.account.data.LoginRepository
import de.ikas.iotrec.account.data.model.User
import de.ikas.iotrec.database.dao.CategoryDao
import de.ikas.iotrec.database.dao.ThingDao
import de.ikas.iotrec.database.db.IotRecDatabase
import de.ikas.iotrec.database.model.Thing
import de.ikas.iotrec.database.repository.CategoryRepository
import de.ikas.iotrec.database.repository.ThingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


class IotRecApplication : Application(), BeaconConsumer {

    private val TAG = "IotRecApplication"

    //lateinit var appLifecycleObserver: IotRecAppLifecycleObserver

    //lateinit var iotRecDatabase: IotRecDatabase
    //private val applicationJob = Job()
    //private val applicationScope = CoroutineScope(Dispatchers.Main + applicationJob)

    private lateinit var beaconManager: BeaconManager

    val iotRecApi = IotRecApiInit(this)
    lateinit var loginRepository: LoginRepository
    private lateinit var categoryDao: CategoryDao
    lateinit var categoryRepository: CategoryRepository

    private lateinit var thingsDao: ThingDao
    private lateinit var thingRepository: ThingRepository



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

        categoryDao = IotRecDatabase.getDatabase(this, GlobalScope).categoryDao()
        categoryRepository = CategoryRepository(categoryDao)

        loginRepository = LoginRepository(iotRecApi, this)

        // gets up-to-date user profile if a user is logged in
        if(loginRepository.isLoggedIn()) {
            loginRepository.syncUserProfile()
        }




        //appLifecycleObserver = IotRecAppLifecycleObserver(this)
        //ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)

        //iotRecDatabase = IotRecDatabase.getDatabase(this, applicationScope)
        //val intent = Intent(this, MainActivity::class.java)
        //val bundle = Bundle()
        //bundle.putParcelable("DATABASE", iotRecDatabase)
        //intent.putExtra("DATABASE", iotRecDatabase)
        //this.startActivity(intent)

        // get permissions needed for bluetooth scanning
        // TODO

        // start bluetooth scanner service
        //startService(Intent(this, BluetoothScannerService()::class.java))
        //ContextCompat.startForegroundService(this, Intent(this, BluetoothScannerService()::class.java))


        // set up beacon manager
        beaconManager = BeaconManager.getInstanceForApplication(this)

        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))

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
                "IotRec Notification Channel ID",
                "IotRec Beacon Scan Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "IotRec Notification Channel Description";
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            builder.setChannelId(channel.id)
        }

        val notification = builder.build()

        // set up scan
        beaconManager.enableForegroundServiceScanning(notification, 456)
        beaconManager.setEnableScheduledScanJobs(false)
        //beaconManager.foregroundBetweenScanPeriod = 0
        beaconManager.foregroundBetweenScanPeriod = 10000
        beaconManager.foregroundScanPeriod = 1100
        //beaconManager.backgroundBetweenScanPeriod = 5000
        beaconManager.backgroundBetweenScanPeriod = 20000
        beaconManager.backgroundScanPeriod = 1100

        //beaconManager.disableForegroundServiceScanning()

        val backgroundPowerSaver = BackgroundPowerSaver(this)


        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        //    val scanFilterBuilder = ScanFilter.Builder()
        //    scanFilterBuilder.setManufacturerData(0x004c, byteArrayOf())
        //    val scanFilter = scanFilterBuilder.build()
        //}

        beaconManager.bind(this)

        thingsDao = IotRecDatabase.getDatabase(this, GlobalScope).thingDao()
        thingRepository = ThingRepository(thingsDao)
    }

    override fun onBeaconServiceConnect() {
        Log.d(TAG, "onBeaconServiceConnect")

        beaconManager.removeAllRangeNotifiers()

        beaconManager.addRangeNotifier(object : RangeNotifier {
            override fun didRangeBeaconsInRegion(beacons: Collection<Beacon>, region: Region) {
                //Log.d(TAG,"=================================================================")

                val rangedBeacons: MutableList<Thing> = mutableListOf()

                for (beacon in beacons) {
                    Log.d(TAG,"distance: " + beacon.distance + " id:" + beacon.id1 + "/" + beacon.id2 + "/" + beacon.id3)
                    //Log.d(TAG, beacon.toString())
                    //Log.d(TAG, beacon.manufacturer.toString())

                    // create object for newly discovered thing
                    val thing = Thing(
                        beacon.id1.toString() + "-" + beacon.id2.toString() + "-" + beacon.id3.toString(),
                        beacon.bluetoothName ?: "Bluetooth name not found",
                        "This beacon's details have not been fetched yet or could not be found.",
                        beacon.id1.toString(),
                        beacon.id2.toInt(),
                        beacon.id3.toInt(),
                        beacon.bluetoothName ?: "unknown beacon",
                        beacon.distance,
                        beacon.beaconTypeCode,
                        beacon.bluetoothAddress,
                        beacon.rssi,
                        beacon.txPower,
                        true,
                        Date(), // lastSeen
                        Date(0), // lastQueried
                        Date(0) //lastTriedToQuery
                    )

                    rangedBeacons.add(thing)

                    // insert object into database
                    GlobalScope.launch(Dispatchers.IO) {

                        val thingInDatabase = thingRepository.getThing(thing.id)

                        if (thingInDatabase != null) {
                            //if beacon is known already, just update inRange status and connectivity details
                            thingRepository.updateBluetoothData(thing)
                        } else {
                            //if beacon is new, insert entire object
                            thingRepository.insert(thing)
                        }

                        // get network data for beacon
                        // only query if
                        //  - never fetched successfully and last query is at least 30 seconds ago or
                        //  - last fetch is older than 10 minutes
                        if(
                            (thingInDatabase != null && thingInDatabase.lastQueried.time.equals(0) && thingInDatabase.lastTriedToQuery.time < Date().time - 30000) ||
                            (thingInDatabase != null && thingInDatabase.lastQueried.time < Date().time - 600000)) {
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
                                            resultThing.description,
                                            Date(),
                                            Date()
                                        )
                                    } else {
                                        // only update "lastTriedToQuery"
                                        thingRepository.updateBackendData(
                                            thingInDatabase.id,
                                            thingInDatabase.title,
                                            thingInDatabase.description,
                                            Date(0),
                                            Date()
                                        )
                                    }

                                    Log.d(TAG, thingRepository.getThing(thing.id).toString())
                                } else {
                                    // only update "lastTriedToQuery"
                                    thingRepository.updateBackendData(
                                        thingInDatabase.id,
                                        thingInDatabase.title,
                                        thingInDatabase.description,
                                        Date(0),
                                        Date()
                                    )
                                }
                                //} catch(e: SocketTimeoutException) {
                                //    Log.d(TAG, e.toString())
                                //} catch(e: ConnectException) {
                                //    // show snackbar to notify of failed connection
                                //    sendBroadcast(R.string.network_failed.toString())
                            } catch(e: Throwable) {
                                //TODO get more information from Throwable and send appropriate broadcast
                                Log.d(TAG, e.toString())
                                //sendBroadcast(e.toString())
                            }

                        }
                    }
                }

                GlobalScope.launch(Dispatchers.IO) {

                    // get all beacons with status "inRange=true" in database
                    val databaseBeaconsInRange = thingRepository.getThingsInRangeList()

                    // find the ones that are not part of current "beacons" set
                    // only filter out if beacon hasn't been seen for at least 5 seconds
                    val beaconsNotInRangeAnymore = databaseBeaconsInRange.filterNot { rangedBeacons.any { x -> x.id == it.id } || (it.lastSeen.time > Date().time - 5000)}

                    // set inRange to false for those identified
                    for (beacon in beaconsNotInRangeAnymore) {
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
