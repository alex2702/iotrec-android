package de.ikas.iotrec.bluetooth.helper

import javax.xml.datatype.DatatypeConstants.SECONDS
import android.system.Os.shutdown
import java.nio.file.Files.size
import android.text.method.TextKeyListener.clear
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.AltBeacon
import org.altbeacon.beacon.simulator.BeaconSimulator
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


class TimedBeaconSimulator : BeaconSimulator {
    private var beacons: MutableList<Beacon>? = null

    /*
	 * You may simulate detection of beacons by creating a class like this in your project.
	 * This is especially useful for when you are testing in an Emulator or on a device without BluetoothLE capability.
	 *
	 * Uncomment the lines in BeaconReferenceApplication starting with:
	 *     // If you wish to test beacon detection in the Android Emulator, you can use code like this:
	 * Then set USE_SIMULATED_BEACONS = true to initialize the sample code in this class.
	 * If using a Bluetooth incapable test device (i.e. Emulator), you will want to comment
	 * out the verifyBluetooth() in MonitoringActivity.java as well.
	 *
	 * Any simulated beacons will automatically be ignored when building for production.
	 */
    var USE_SIMULATED_BEACONS = true


    private var scheduleTaskExecutor: ScheduledExecutorService? = null

    /**
     * Creates empty beacons ArrayList.
     */
    init {
        beacons = ArrayList()
    }

    /**
     * Required getter method that is called regularly by the Android Beacon Library.
     * Any beacons returned by this method will appear within your test environment immediately.
     */
    override fun getBeacons(): List<Beacon>? {
        return beacons
    }

    /**
     * Creates simulated beacons all at once.
     */
    fun createBasicSimulatedBeacons() {
        if (USE_SIMULATED_BEACONS) {
            val beacon1 = AltBeacon.Builder().setId1("DF7E1C79-43E9-44FF-886F-1D1F7DA6997A")
                .setId2("1").setId3("1").setRssi(-55).setTxPower(-55).build()
            val beacon2 = AltBeacon.Builder().setId1("DF7E1C79-43E9-44FF-886F-1D1F7DA6997A")
                .setId2("1").setId3("2").setRssi(-55).setTxPower(-55).build()
            val beacon3 = AltBeacon.Builder().setId1("DF7E1C79-43E9-44FF-886F-1D1F7DA6997A")
                .setId2("1").setId3("3").setRssi(-55).setTxPower(-55).build()
            val beacon4 = AltBeacon.Builder().setId1("DF7E1C79-43E9-44FF-886F-1D1F7DA6997A")
                .setId2("1").setId3("4").setRssi(-55).setTxPower(-55).build()
            beacons!!.add(beacon1)
            beacons!!.add(beacon2)
            beacons!!.add(beacon3)
            beacons!!.add(beacon4)


        }
    }


    /**
     * Simulates a new beacon every 10 seconds until it runs out of new ones to add.
     */
    fun createTimedSimulatedBeacons() {
        if (USE_SIMULATED_BEACONS) {
            beacons = ArrayList()
            val beacon1 = AltBeacon.Builder().setId1("DF7E1C79-43E9-44FF-886F-1D1F7DA6997A")
                .setId2("1").setId3("1").setRssi(-55).setTxPower(-55).build()
            val beacon2 = AltBeacon.Builder().setId1("DF7E1C79-43E9-44FF-886F-1D1F7DA6997A")
                .setId2("1").setId3("2").setRssi(-55).setTxPower(-55).build()
            val beacon3 = AltBeacon.Builder().setId1("DF7E1C79-43E9-44FF-886F-1D1F7DA6997A")
                .setId2("1").setId3("3").setRssi(-55).setTxPower(-55).build()
            val beacon4 = AltBeacon.Builder().setId1("DF7E1C79-43E9-44FF-886F-1D1F7DA6997A")
                .setId2("1").setId3("4").setRssi(-55).setTxPower(-55).build()
            beacons!!.add(beacon1)
            beacons!!.add(beacon2)
            beacons!!.add(beacon3)
            beacons!!.add(beacon4)

            val finalBeacons = ArrayList(beacons)

            //Clearing beacons list to prevent all beacons from appearing immediately.
            //These will be added back into the beacons list from finalBeacons later.
            beacons!!.clear()

            scheduleTaskExecutor = Executors.newScheduledThreadPool(5)

            // This schedules an beacon to appear every 10 seconds:
            scheduleTaskExecutor!!.scheduleAtFixedRate(Runnable {
                try {
                    //putting a single beacon back into the beacons list.
                    if (finalBeacons.size > beacons!!.size)
                        beacons!!.add(finalBeacons[beacons!!.size])
                    else
                        scheduleTaskExecutor!!.shutdown()

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, 0, 10, TimeUnit.SECONDS)
        }
    }

    companion object : BeaconSimulator {
        override fun getBeacons(): MutableList<Beacon> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        protected val TAG = "TimedBeaconSimulator"
    }

}