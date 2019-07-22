package de.ikas.iotrec.bluetooth.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.os.RemoteException
import de.ikas.iotrec.database.dao.ThingDao
import de.ikas.iotrec.database.db.IotRecDatabase
import de.ikas.iotrec.database.model.Thing
import de.ikas.iotrec.database.repository.ThingRepository
import de.ikas.iotrec.network.IotRecApiInit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.altbeacon.beacon.*
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.RangeNotifier
import java.time.LocalDateTime
import java.util.*

class BluetoothScannerService() : Service(), BeaconConsumer {

    private val TAG = "BluetoothScannerService"
    private var beaconManager: BeaconManager? = null

    private var iotRecApi = IotRecApiInit(this)
    private lateinit var thingsDao: ThingDao
    private lateinit var thingRepository: ThingRepository
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    init {
        val iotRecApi = IotRecApiInit(this)
    }

    override fun onCreate() {
        super.onCreate()

        beaconManager = BeaconManager.getInstanceForApplication(this)

        // iBeacon layout
        beaconManager!!.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))

        beaconManager!!.foregroundScanPeriod = 5000
        beaconManager!!.foregroundBetweenScanPeriod = 0
        beaconManager!!.backgroundScanPeriod = 5000
        beaconManager!!.backgroundBetweenScanPeriod = 0

        beaconManager!!.bind(this)

        thingsDao = IotRecDatabase.getDatabase(this, serviceScope).thingDao()
        thingRepository = ThingRepository(thingsDao)
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBeaconServiceConnect() {
        val region = Region("backgroundRegion", null, null, null)

        beaconManager?.addRangeNotifier(object : RangeNotifier {
            override fun didRangeBeaconsInRegion(beacons: Collection<Beacon>, region: Region) {

                Log.d(TAG,"=================================================================")

                val rangedBeacons: MutableList<Thing> = mutableListOf()

                for (beacon in beacons) {
                    Log.d(TAG,"distance: " + beacon.distance + " id:" + beacon.id1 + "/" + beacon.id2 + "/" + beacon.id3)
                    //Log.d(TAG, beacon.toString())

                    // create object for newly discovered thing
                    val thing = Thing(
                        beacon.id1.toString() + "-" + beacon.id2.toString() + "-" + beacon.id3.toString(),
                        beacon.bluetoothName ?: "Bluetooth name not found",
                        "",
                        beacon.id1.toString(),
                        beacon.id2.toInt(),
                        beacon.id3.toInt(),
                        beacon.bluetoothName,
                        beacon.distance,
                        beacon.beaconTypeCode,
                        beacon.bluetoothAddress,
                        beacon.rssi,
                        beacon.txPower,
                        true,
                        Date(), // lastSeen
                        Date(0) // lastQueried
                        )

                    rangedBeacons.add(thing)

                    // insert object into database
                    serviceScope.launch(Dispatchers.IO) {

                        val thingInDatabase = thingRepository.getThing(thing.id)

                        if (thingInDatabase != null) {
                            //if beacon is known already, just update inRange status and connectivity details
                            thingRepository.updateBluetoothData(thing)
                        } else {
                            //if beacon is new, insert entire object
                            thingRepository.insert(thing)
                        }

                        // get network data for beacon
                        // only query if never fetched or if last fetch is older than 10 minutes
                        if(thingInDatabase == null || thingInDatabase.lastQueried.time < Date().time - 600000) {
                            val result = iotRecApi.getThing(thing.id)

                            // if successful, update database object
                            if(result.isSuccessful) {
                                val resultThing = result.body()

                                Log.d(TAG, thing.toString())

                                if (resultThing != null) {
                                    thingRepository.updateBackendData(resultThing.id, resultThing.title, resultThing.description, Date())
                                }

                                Log.d(TAG, thingRepository.getThing(thing.id).toString())
                            }
                        }
                    }
                }

                serviceScope.launch(Dispatchers.IO) {

                    Log.d(TAG, "Beacons in Range (DB): " + thingRepository.getThingsInRangeList().size)

                    // get all beacons with status "inRange=true" in database
                    val databaseBeaconsInRange = thingRepository.getThingsInRangeList()

                    // find the ones that are not part of current "beacons" set
                    // TODO only filter out if beacon hasn't been seen for at least 5 seconds
                    val beaconsNotInRangeAnymore = databaseBeaconsInRange.filterNot { rangedBeacons.any { x -> x.id == it.id } /* && (it.lastSeen < (now - 5 seconds))*/ }

                    // set inRange to false for those identified
                    for (beacon in beaconsNotInRangeAnymore) {
                        beacon.inRange = false

                        serviceScope.launch(Dispatchers.IO) {
                            thingRepository.setThingInRange(beacon.id, false)
                        }
                    }

                    Log.d(TAG, "Beacons in Range (DB): " + thingRepository.getThingsInRangeList().size)
                }
            }
        })

        try {
            beaconManager?.startRangingBeaconsInRegion(region)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }
}






/*
class BluetoothScannerService : Service(), /*BeaconConsumer, */BootstrapNotifier {

    private lateinit var beaconManager: BeaconManager
    private var regionBootstrap: RegionBootstrap? = null
    //private var backgroundPowerSaver: BackgroundPowerSaver? = null

    companion object {
        private const val TAG_FOREGROUND_SERVICE = "FOREGROUND_SERVICE"
        const val ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE"
        const val ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE"
        //const val ACTION_PAUSE = "ACTION_PAUSE"
        //const val ACTION_PLAY = "ACTION_PLAY"
    }


    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG_FOREGROUND_SERVICE, "onBind()")
        TODO("Return the communication channel to the service.")
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG_FOREGROUND_SERVICE, "onCreate()")

        /*
        val timedBeaconSimulator = TimedBeaconSimulator()
        timedBeaconSimulator.createTimedSimulatedBeacons()
        BeaconManager.setBeaconSimulator(timedBeaconSimulator)
        */

        //val timedBeaconSimulator = BeaconManager.getBeaconSimulator()
        //timedBeaconSimulator.createTimedSimulatedBeacons()
    }

    override fun onDestroy() {
        super.onDestroy()
        //beaconManager.unbind(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG_FOREGROUND_SERVICE, "onStartCommand()")
        if (intent != null) {
            val action = intent.action

            when (action) {
                ACTION_START_FOREGROUND_SERVICE -> {
                    startForegroundService()
                    Toast.makeText(applicationContext, "Foreground service is started.", Toast.LENGTH_LONG).show()
                }
                ACTION_STOP_FOREGROUND_SERVICE -> {
                    stopForegroundService()
                    Toast.makeText(applicationContext, "Foreground service is stopped.", Toast.LENGTH_LONG).show()
                }
                //ACTION_PLAY -> Toast.makeText(applicationContext, "You click Play button.", Toast.LENGTH_LONG).show()
                //ACTION_PAUSE -> Toast.makeText(applicationContext, "You click Pause button.", Toast.LENGTH_LONG).show()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /* Used to build and start foreground service. */
    private fun startForegroundService() {
        Log.d(TAG_FOREGROUND_SERVICE, "Start foreground service.")


        /*
        // Create notification default intent.
        val intent = Intent()
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        // Create notification builder.
        val builder = NotificationCompat.Builder(this, getString(R.string.notification_channel_id_service))

        // Make notification show big text.
        val bigTextStyle = NotificationCompat.BigTextStyle()
        bigTextStyle.setBigContentTitle("IoT Recommendations")
        bigTextStyle.bigText("IoT Rec is scanning for Bluetooth beacons.")
        // Set big text style.
        builder.setStyle(bigTextStyle)

        builder.setWhen(System.currentTimeMillis())
        builder.setSmallIcon(R.mipmap.ic_launcher)
        val largeIconBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_home_black_24dp)
        builder.setLargeIcon(largeIconBitmap)
        // Make the notification max priority.
        builder.setPriority(Notification.PRIORITY_MAX)
        // Make head-up notification.
        //builder.setFullScreenIntent(pendingIntent, true)

        // Add Play button intent in notification.
        //val playIntent = Intent(this, BluetoothScannerService::class.java)
        //playIntent.action = ACTION_PLAY
        //val pendingPlayIntent = PendingIntent.getService(this, 0, playIntent, 0)
        //val playAction = NotificationCompat.Action(android.R.drawable.ic_media_play, "Play", pendingPlayIntent)
        //builder.addAction(playAction)

        // Add Pause button intent in notification.
        //val pauseIntent = Intent(this, BluetoothScannerService::class.java)
        //pauseIntent.action = ACTION_PAUSE
        //val pendingPrevIntent = PendingIntent.getService(this, 0, pauseIntent, 0)
        //val prevAction = NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause", pendingPrevIntent)
        //builder.addAction(prevAction)

        // Build the notification.
        val notification = builder.build()
        */



        beaconManager = BeaconManager.getInstanceForApplication(this)
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))

        // build the persistent notification
        val builder = NotificationCompat.Builder(this, getString(R.string.notification_channel_id_service))
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setContentTitle("Scanning for Beacons")
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
        beaconManager.enableForegroundServiceScanning(builder.build(), 456)

        // For the above foreground scanning service to be useful, you need to disable
        // JobScheduler-based scans (used on Android 8+) and set a fast background scan
        // cycle that would otherwise be disallowed by the operating system.
        beaconManager.setEnableScheduledScanJobs(false)
        beaconManager.backgroundBetweenScanPeriod = 0
        beaconManager.backgroundScanPeriod = 1100

        Log.d(TAG_FOREGROUND_SERVICE, "setting up background monitoring for beacons and power saving")

        // wake up the app when a beacon is seen
        val region = Region("backgroundRegion", null, null, null)
        regionBootstrap = RegionBootstrap(this, region)

        // simply constructing this class and holding a reference to it in your custom Application
        // class will automatically cause the BeaconLibrary to save battery whenever the application
        // is not visible.  This reduces bluetooth power usage by about 60%

        //backgroundPowerSaver = BackgroundPowerSaver(this);

        //beaconManager.bind(this)

        // Start foreground service.
        //startForeground(1, notification)



    }

    private fun stopForegroundService() {
        Log.d(TAG_FOREGROUND_SERVICE, "Stop foreground service.")

        //beaconManager.unbind(this)

        // Stop foreground service and remove the notification.
        stopForeground(true)

        // Stop the foreground service.
        stopSelf()
    }


    /*
    override fun onBeaconServiceConnect() {
        Log.d(TAG_FOREGROUND_SERVICE, "onBeaconServiceConnect()")

        beaconManager.removeAllMonitorNotifiers()

        beaconManager.addMonitorNotifier(object : MonitorNotifier {
            override fun didEnterRegion(region: Region) {
                Log.i(TAG_FOREGROUND_SERVICE, "I just saw an beacon for the first time!")
            }

            override fun didExitRegion(region: Region) {
                Log.i(TAG_FOREGROUND_SERVICE, "I no longer see an beacon")
            }

            override fun didDetermineStateForRegion(state: Int, region: Region) {
                Log.i(TAG_FOREGROUND_SERVICE, "I have just switched from seeing/not seeing beacons: $state")
            }
        })

        try {
            beaconManager.startMonitoringBeaconsInRegion(Region("myMonitoringUniqueId", null, null, null))
        } catch (e: RemoteException) {

        }
    }
    */

    //overwrites for BootstrapNotifier
    override fun didDetermineStateForRegion(p0: Int, p1: Region?) {
        Log.i(TAG_FOREGROUND_SERVICE, "Current region state is: " + if (p0 === 1) "INSIDE" else "OUTSIDE ($p0)")
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun didEnterRegion(p0: Region?) {
        Log.i(TAG_FOREGROUND_SERVICE, "entered region")
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun didExitRegion(p0: Region?) {
        Log.i(TAG_FOREGROUND_SERVICE, "I no longer see a beacon.")
        /*
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        */
    }
}
*/