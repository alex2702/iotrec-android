package de.ikas.iotrec.app

import android.app.Activity
import android.app.Application
import android.content.res.Configuration
import android.util.Log
import org.altbeacon.beacon.powersave.BackgroundPowerSaver
import org.altbeacon.beacon.startup.RegionBootstrap
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Region
import org.altbeacon.beacon.logging.LogManager
import org.altbeacon.beacon.logging.Loggers
import org.altbeacon.beacon.startup.BootstrapNotifier
import java.lang.Compiler.disable




class IotRecApplication : Application(), BootstrapNotifier {

    private val TAG = "IotRecApplication"
    private var regionBootstrap: RegionBootstrap? = null
    private var backgroundPowerSaver: BackgroundPowerSaver? = null
    private val haveDetectedBeaconsSinceBoot = false
    private var monitoringActivity: Activity? = null

    // Called when the application is starting, before any other application objects have been created.
    // Overriding this method is totally optional!
    override fun onCreate() {
        super.onCreate()

        val beaconManager = BeaconManager.getInstanceForApplication(this)

        beaconManager.foregroundScanPeriod = 1100
        beaconManager.foregroundBetweenScanPeriod = 0
        beaconManager.backgroundScanPeriod = 1100
        beaconManager.backgroundBetweenScanPeriod = 0

        // set iBeacon layout
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))

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

    override fun didEnterRegion(p0: Region?) {
        Log.d(TAG, "Entered region.")
    }

    override fun didExitRegion(p0: Region?) {
        Log.d(TAG, "I no longer see a beacon.")
    }

    fun setMonitoringActivity(activity: Activity?) {
        this.monitoringActivity = activity
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
}
