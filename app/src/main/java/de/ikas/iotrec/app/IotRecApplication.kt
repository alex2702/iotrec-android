package de.ikas.iotrec.app

import android.util.Log
import android.content.Intent
import android.app.*
import de.ikas.iotrec.bluetooth.service.BluetoothScannerService
import de.ikas.iotrec.network.IotRecApiInit


class IotRecApplication : Application() {

    private val TAG = "IotRecApplication"

    //lateinit var iotRecDatabase: IotRecDatabase
    //private val applicationJob = Job()
    //private val applicationScope = CoroutineScope(Dispatchers.Main + applicationJob)


    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "IotRecApplication – onCreate")

        //iotRecDatabase = IotRecDatabase.getDatabase(this, applicationScope)
        //val intent = Intent(this, MainActivity::class.java)
        //val bundle = Bundle()
        //bundle.putParcelable("DATABASE", iotRecDatabase)
        //intent.putExtra("DATABASE", iotRecDatabase)
        //this.startActivity(intent)

        // get permissions needed for bluetooth scanning
        // TODO

        // start bluetooth scanner service
        startService(Intent(this, BluetoothScannerService()::class.java))
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
