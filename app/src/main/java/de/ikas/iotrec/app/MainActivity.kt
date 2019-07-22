package de.ikas.iotrec.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import de.ikas.iotrec.R
import de.ikas.iotrec.bluetooth.ui.ThingListFragment
import java.util.logging.Logger
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.ikas.iotrec.account.ui.login.LoginActivity
import de.ikas.iotrec.bluetooth.ui.ThingBottomSheetFragment
import de.ikas.iotrec.bluetooth.ui.ThingListFragment.Companion.NEW_THING_ACTIVITY_REQUEST_CODE
import de.ikas.iotrec.database.db.IotRecDatabase
import de.ikas.iotrec.database.model.Thing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job


class MainActivity : AppCompatActivity(), ThingListFragment.OnListFragmentInteractionListener { // double listener? */ /*, BeaconConsumer*/ {

    private lateinit var textMessage: TextView
    //private lateinit var notificationHelper: NotificationHelper
    //private var backgroundPowerSaver: BackgroundPowerSaver? = null
    //private lateinit var application: IotRecApplication //Monitoring
    //private val beaconManager = BeaconManager.getInstanceForApplication(this) //Ranging

    private val TAG = "MainActivity"
    private val PERMISSION_REQUEST_COARSE_LOCATION = 1

    val logger = Logger.getLogger(MainActivity::class.java.name)


    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_things -> {
                textMessage.setText(R.string.title_things)
                loadFragment(item.itemId)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_preferences -> {
                textMessage.setText(R.string.title_preferences)
                loadFragment(item.itemId)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_settings -> {
                textMessage.setText(R.string.title_settings)
                loadFragment(item.itemId)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_profile -> {
                //textMessage.setText(R.string.title_settings)
                //loadFragment(item.itemId)

                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)

                return@OnNavigationItemSelectedListener true
            }
        }

        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainActivity – onCreate")
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        textMessage = findViewById(R.id.message)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        // set first tab as selected
        navView.selectedItemId = R.id.navigation_things

        //backgroundPowerSaver = null

        //notificationHelper = NotificationHelper(applicationContext)
        //notificationHelper.createNotificationChannels()

        //val intent = Intent(this, BluetoothScannerService::class.java)
        //intent.action = BluetoothScannerService.ACTION_START_FOREGROUND_SERVICE
        //startService(intent)

        // Monitoring
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("This app needs location access")
                builder.setMessage("Please grant location access so this app can detect beacons in the background.")
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setOnDismissListener(DialogInterface.OnDismissListener {
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                        PERMISSION_REQUEST_COARSE_LOCATION
                    )
                })
                builder.show()
            }
        }
        */


        //probably not needed because onResume is called after onCreate anyway
        //val application = this.applicationContext as IotRecApplication
        //application.enableMonitoring()

        /*
        val application = this.applicationContext as IotRecApplication
        if (BeaconManager.getInstanceForApplication(this).monitoredRegions.size > 0) {
            application.disableMonitoring()
        } else {
            application.enableMonitoring()
        }
        */
    }

    override fun onPause() {
        super.onPause()
        //backgroundPowerSaver = BackgroundPowerSaver(this)
        //(this.applicationContext as IotRecApplication).setMonitoringActivity(null)    //Monitoring
        //beaconManager.unbind(this) //Ranging
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "MainActivity – onResume")
        //backgroundPowerSaver = null
        //val application = this.applicationContext as IotRecApplication    //Monitoring
        //application.setMonitoringActivity(this)   //Monitoring
        //beaconManager.bind(this) //Ranging
    }

    private fun loadFragment(itemId: Int) {
        val tag = itemId.toString()
        var fragment = supportFragmentManager.findFragmentByTag(tag) ?: when (itemId) {
            R.id.navigation_things -> {
                ThingListFragment.newInstance(1)
            }
            R.id.navigation_preferences -> {
                PreferenceFragment.newInstance(1)
            }
            R.id.navigation_settings -> {
                SettingFragment.newInstance(1)
            }
            else -> {
                null
            }
        }

        // replace fragment
        if (fragment != null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .commit()
        }
    }

    // Monitoring
    /*
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_COARSE_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted")
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Functionality limited")
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.")
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setOnDismissListener { }
                    builder.show()
                }
                return
            }
        }
    }
    */


    //Ranging
    /*
    override fun onBeaconServiceConnect() {
        val rangeNotifier = object : RangeNotifier {
            override fun didRangeBeaconsInRegion(beacons: Collection<Beacon>, region: Region) {
                if (beacons.size > 0) {
                    Log.d(TAG, "didRangeBeaconsInRegion called with beacon count:  " + beacons.size)
                    val firstBeacon = beacons.iterator().next()
                    Log.d(TAG,"The first beacon " + firstBeacon.toString() + " is about " + firstBeacon.distance + " meters away.")
                }
            }

        }
        try {
            beaconManager.startRangingBeaconsInRegion(Region("myRangingUniqueId", null, null, null))
            beaconManager.addRangeNotifier(rangeNotifier)
        } catch (e: RemoteException) {
            Log.e(TAG, "Not bound to beacon scanning service")
        }

    }
    */

    override fun onListFragmentInteraction(thing: Thing) {
        Log.d(TAG, "a list item was clicked")

        val bottomSheetFragment = ThingBottomSheetFragment.newInstance(thing)
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)

        /*
        val bottomSheetFragment = ThingBottomSheetFragment.newInstance(thing)
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)

        val detailsFragment = RageComicDetailsFragment.newInstance(comic)
        supportFragmentManager.beginTransaction()
            .replace(R.id.root_layout, detailsFragment, "rageComicDetails")
            .addToBackStack(null)
            .commit()

        view.context.getSupportFragmentManager()
        Fragment myFragment = new MyFragment();
        activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, myFragment).addToBackStack(null).commit()
        */
    } // double listener?
}
