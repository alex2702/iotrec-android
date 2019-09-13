package de.ikas.iotrec.app

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import de.ikas.iotrec.R
import de.ikas.iotrec.bluetooth.ui.ThingListFragment
import java.util.logging.Logger
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.ikas.iotrec.account.ui.LoginActivity
import de.ikas.iotrec.bluetooth.ui.ThingBottomSheetFragment
import de.ikas.iotrec.database.model.Thing
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.content.BroadcastReceiver
import android.content.Context
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.content.IntentFilter
import com.google.android.material.snackbar.Snackbar
import de.ikas.iotrec.database.model.Category
import android.view.WindowManager
import android.widget.*
import androidx.lifecycle.ViewModelProviders
import de.ikas.iotrec.database.dao.CategoryDao
import de.ikas.iotrec.database.db.IotRecDatabase
import de.ikas.iotrec.database.repository.CategoryRepository
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager
import de.ikas.iotrec.account.data.LoginRepository
import de.ikas.iotrec.preferences.adapter.PreferenceDialogViewAdapter
import de.ikas.iotrec.preferences.ui.PreferenceListFragment
import de.ikas.iotrec.preferences.ui.PreferenceSelectDialogFragment
import de.ikas.iotrec.preferences.ui.PreferenceViewModel
import android.R.attr.fragment
import android.R.attr.key
//import permissions.dispatcher.RuntimePermissions


//@RuntimePermissions
class MainActivity :
    AppCompatActivity(),
    ThingListFragment.OnListFragmentInteractionListener,
    PreferenceListFragment.OnListFragmentInteractionListener,
    ProfileFragment.OnFragmentInteractionListener,
    PreferenceSelectDialogFragment.OnFragmentInteractionListener {

    private lateinit var textMessage: TextView
    //private lateinit var notificationHelper: NotificationHelper
    //private var backgroundPowerSaver: BackgroundPowerSaver? = null
    //private lateinit var application: IotRecApplication //Monitoring
    //private val beaconManager = BeaconManager.getInstanceForApplication(this) //Ranging

    // fragments for bottom navigation
    //private val thingListFragment = ThingListFragment.newInstance(1)
    //private val preferencesFragment = PreferenceListFragment.newInstance(1)
    //private val settingFragment = SettingFragment.newInstance(1)
    //private val profileFragment = ProfileFragment()
    //private var active = Fragment()

    private lateinit var app: IotRecApplication

    private lateinit var categoryViewModel: PreferenceViewModel
    private lateinit var categoriesDao: CategoryDao
    private lateinit var categoryRepository: CategoryRepository

    lateinit var loginRepository: LoginRepository
    lateinit var currentlySelectedUserPreferences: MutableList<String>
    var userPreferencesToBeAdded = mutableListOf<String>()
    var userPreferencesToBeRemoved = mutableListOf<String>()
    //private var preferenceDialogListener: OnPreferenceDialogFragmentInteractionListener? = null

    private val TAG = "MainActivity"
    private val PERMISSION_REQUEST_COARSE_LOCATION = 1

    private lateinit var broadcastReceiver: BroadcastReceiver

    val logger = Logger.getLogger(MainActivity::class.java.name)

    // TODO what else to put in here from the above declarations? => general research needed
    companion object {
        //const val START_LOGIN_ACTIVITY_REQUEST_CODE = 0
    }

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_things -> {
                setTitle(R.string.title_things)
                textMessage.setText(R.string.title_things)
                loadFragment(item.itemId)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_preferences -> {
                setTitle(R.string.title_preferences)
                textMessage.setText(R.string.title_preferences)
                loadFragment(item.itemId)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_settings -> {
                setTitle(R.string.title_settings)
                textMessage.setText(R.string.title_settings)
                loadFragment(item.itemId)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_profile -> {
                // check if a user is logged in (via token in sharedPrefs)
                val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)

                /* print all shared prefs
                val allEntries = sharedPrefs.getAll()
                for (entry in allEntries.entries) {
                    Log.d(TAG, entry.key + ": " + entry.value.toString())
                }
                */

                val token = sharedPrefs.getString("userToken", "")
                Log.d(TAG, "token is $token")

                // show ProfileFragment
                setTitle(R.string.title_profile)
                textMessage.setText(R.string.title_profile)
                loadFragment(item.itemId)

                if (token == "") {
                    // if user is not logged in, show LoginActivity on top
                    // TODO do I want this?
                    val intent = Intent(this, LoginActivity::class.java)
                    //startActivityForResult(intent, START_LOGIN_ACTIVITY_REQUEST_CODE)
                    startActivity(intent)
                }

                return@OnNavigationItemSelectedListener true
            }
        }

        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "MainActivity – onCreate")

        // TODO move somewhere else for performance reason? further down in here?
        app = application as IotRecApplication
        loginRepository = app.loginRepository
        if(loginRepository.user != null) {
            currentlySelectedUserPreferences = loginRepository.user!!.preferences
        }


        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        textMessage = findViewById(R.id.message)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        //supportFragmentManager.beginTransaction().add(profileFragment, "4").hide(profileFragment).commit()
        //supportFragmentManager.beginTransaction().add(settingFragment, "3").hide(settingFragment).commit()
        //supportFragmentManager.beginTransaction().add(preferencesFragment, "2").hide(preferencesFragment).commit()
        //supportFragmentManager.beginTransaction().add(thingListFragment, "1").commit()


        // set first tab as selected
        navView.selectedItemId = R.id.navigation_things

        // TODO put this in LoginRepository
        // if a JWT is present in shared prefs, verify validity
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val token = sharedPrefs.getString("userToken", "")
        if (token != "") {
            GlobalScope.launch {
                val loggedInUserResult = app.iotRecApi.verifyToken(token!!)
                if (loggedInUserResult.isSuccessful) {
                    val loggedInUser = loggedInUserResult.body()
                    val editor = sharedPrefs.edit()
                    editor.putString("userToken", loggedInUser?.token)
                    editor.apply()
                } else {
                    // if token is invalid, remove it from sharedPrefs
                    val editor = sharedPrefs.edit()
                    editor.putString("userToken", "")
                    editor.apply()
                }
            }
        }

        // set up broadcast receiver to receive messages from bluetooth scanner service
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val snack = Snackbar.make(
                    findViewById(android.R.id.content),
                    intent.getStringExtra("SNACKBAR_MESSAGE"),
                    Snackbar.LENGTH_SHORT
                )
                snack.show()
            }
        }


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


        // preference handling
        categoryViewModel = ViewModelProviders.of(this).get(PreferenceViewModel::class.java)
        categoriesDao = IotRecDatabase.getDatabase(this, GlobalScope).categoryDao()
        categoryRepository = CategoryRepository(categoriesDao)
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
        //backgroundPowerSaver = BackgroundPowerSaver(this)
        //(this.applicationContext as IotRecApplication).setMonitoringActivity(null)    //Monitoring
        //beaconManager.unbind(this) //Ranging
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        //backgroundPowerSaver = null
        //val application = this.applicationContext as IotRecApplication    //Monitoring
        //application.setMonitoringActivity(this)   //Monitoring
        //beaconManager.bind(this) //Ranging

        // connect back the broadcastReceiver to listen for messages from BluetoothScannerService
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastReceiver, IntentFilter("BLUETOOTH_SCANNER_SERVICE"))
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }

    private fun loadFragment(itemId: Int) {
        val tag = itemId.toString()


        var fragment = supportFragmentManager.findFragmentByTag(tag) ?: when (itemId) {
            R.id.navigation_things -> {
                ThingListFragment.newInstance(1)
            }
            R.id.navigation_preferences -> {
                PreferenceListFragment.newInstance(1)
            }
            R.id.navigation_settings -> {
                SettingFragment.newInstance(1)
            }
            R.id.navigation_profile -> {
                ProfileFragment()
            }
            else -> {
                null
            }
        }


        // replace fragment
        if (fragment != null) {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment, tag).commit()

            //supportFragmentManager.beginTransaction().hide(active).show(fragment).commit()
            //active = fragment
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

    override fun onThingListFragmentInteraction(thing: Thing) {
        Log.d(TAG, "a thing list item was clicked")

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
    } // double preferenceDialogListener?

    override fun onPreferenceListFragmentInteraction(category: Category) {
        Log.d(TAG, "a preference list item was clicked")

        GlobalScope.launch {
            loginRepository.saveUser()
        }

        // load dialog fragment to select sub-categories
        val fragment = PreferenceSelectDialogFragment.newInstance(category)
        fragment.show(supportFragmentManager, "preference_select_dialog")
    }

    /*
    fun onPreferenceDialogFragmentInteraction(category: Category) {
        Log.d("MainActivity", "sub-category clicked")

        // mark clicked category as selected
        // TODO

        // add clicked category to set of selected categories
        // TODO
    }
    */


    override fun onFragmentInteraction(uri: Uri) {
        Log.d(TAG, "onFragmentInteraction")
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    } // double preferenceDialogListener? */ /*, BeaconConsumer*/ {

    /*
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == START_LOGIN_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // if login has been completed (either successfully or unsuccessfully), show profile tab)
            Log.d(TAG, "showing ProfileFragment")
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
    */

    override fun onPreferenceSelectDialogFragmentInteraction(category: Category) {
        Log.d(TAG, category.toString())

        //GlobalScope.launch {
        //    loginRepository.saveUser()
        //}


        /*
        // search item in list from beginning
        val index = currentlySelectedUserPreferences.indexOf(category.textId)

        if(index > -1) {
            if(selected) {
                // if item was selected before and is selected now, remove it from all lists
                userPreferencesToBeRemoved.remove(category.textId)
                //userPreferencesToBeAdded.remove(category.textId)
            } else {
                // if item was selected before and is not selected now, add it to toBeRemoved list
                userPreferencesToBeRemoved.add(category.textId)
            }
        } else {
            if(selected) {
                // if item was not selected before but is selected now, add it to toBeAdded list
                userPreferencesToBeAdded.add(category.textId)
            } else {
                // if item was not selected before and is not selected now, remove it from all lists
                userPreferencesToBeRemoved.remove(category.textId)
                //userPreferencesToBeAdded.remove(category.textId)
            }
        }
        */
    }
}

