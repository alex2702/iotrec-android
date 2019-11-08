package de.ikas.iotrec.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import de.ikas.iotrec.R
import de.ikas.iotrec.bluetooth.ThingListFragment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.ikas.iotrec.account.ui.LoginActivity
import de.ikas.iotrec.bluetooth.ThingBottomSheetFragment
import de.ikas.iotrec.database.model.Thing
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.content.BroadcastReceiver
import android.content.Context
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.content.IntentFilter
import com.google.android.material.snackbar.Snackbar
import de.ikas.iotrec.database.model.Category
import android.widget.*
import androidx.lifecycle.ViewModelProviders
import de.ikas.iotrec.database.dao.CategoryDao
import de.ikas.iotrec.database.db.IotRecDatabase
import de.ikas.iotrec.database.repository.CategoryRepository
import de.ikas.iotrec.account.data.LoginRepository
import de.ikas.iotrec.preferences.ui.PreferenceListFragment
import de.ikas.iotrec.preferences.ui.PreferenceSelectDialogFragment
import de.ikas.iotrec.preferences.ui.PreferenceViewModel
import android.content.pm.PackageManager
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import de.ikas.iotrec.database.model.Preference
import de.ikas.iotrec.database.repository.PreferenceRepository
import androidx.appcompat.app.AlertDialog
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import de.ikas.iotrec.account.data.model.User
import de.ikas.iotrec.database.model.Question
import de.ikas.iotrec.database.repository.ExperimentRepository
import de.ikas.iotrec.database.repository.ReplyRepository
import de.ikas.iotrec.experiment.ExperimentFragment
import java.util.*


class MainActivity :
    AppCompatActivity(),
    ThingListFragment.OnListFragmentInteractionListener,
    PreferenceListFragment.OnListFragmentInteractionListener,
    ProfileFragment.OnFragmentInteractionListener,
    PreferenceSelectDialogFragment.OnFragmentInteractionListener,
    ExperimentFragment.OnQuestionListFragmentInteractionListener {

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
    lateinit var categoryRepository: CategoryRepository

    lateinit var preferenceRepository: PreferenceRepository

    lateinit var loginRepository: LoginRepository
    lateinit var userPreferences: MutableList<Preference>
    var userPreferencesToBeAdded = mutableListOf<String>()
    var userPreferencesToBeRemoved = mutableListOf<String>()
    lateinit var experimentRepository: ExperimentRepository
    lateinit var replyRepository: ReplyRepository

    lateinit var navView: BottomNavigationView

    private val TAG = "MainActivity"

    private lateinit var broadcastReceiver: BroadcastReceiver


    companion object {
        // constants for activity and permission request callbacks
        const val START_LOGIN_ACTIVITY_REQUEST_CODE = 15623
        const val IOTREC_PERMISSION_REQUEST_COARSE_LOCATION = 18451
    }

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_things -> {
                setTitle(R.string.title_things)
                loadFragment(item.itemId)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_preferences -> {
                setTitle(R.string.title_preferences)
                loadFragment(item.itemId)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_experiment-> {
                setTitle(R.string.title_experiment)
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
                loadFragment(item.itemId)

                if (token == "") {
                    // if user is not logged in, show LoginActivity on top
                    loginRepository.logout()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivityForResult(intent, START_LOGIN_ACTIVITY_REQUEST_CODE)
                    //startActivity(intent)
                }

                return@OnNavigationItemSelectedListener true
            }
        }

        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "MainActivity – onCreate")

        app = application as IotRecApplication
        loginRepository = app.loginRepository
        if(loginRepository.user != null) {
            userPreferences = loginRepository.user!!.preferences
        }


        setContentView(R.layout.activity_main)
        navView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        //supportFragmentManager.beginTransaction().add(profileFragment, "4").hide(profileFragment).commit()
        //supportFragmentManager.beginTransaction().add(settingFragment, "3").hide(settingFragment).commit()
        //supportFragmentManager.beginTransaction().add(preferencesFragment, "2").hide(preferencesFragment).commit()
        //supportFragmentManager.beginTransaction().add(thingListFragment, "1").commit()


        // set first tab as selected
        navView.selectedItemId = R.id.navigation_things

        // synchronize categories and experiment questions
        GlobalScope.launch {
            val result = app.iotRecApi.getCategories()

            // if successful, update database
            if (result.isSuccessful) {
                val resultCategories = result.body()
                app.categoryRepository.insertMultiple(*resultCategories!!.toTypedArray())
            }

            val resultQ = app.iotRecApi.getQuestions()

            Log.d(TAG, resultQ.toString())

            // if successful, update database
            if (resultQ.isSuccessful) {
                val resultQuestions = resultQ.body()
                Log.d(TAG, resultQuestions.toString())
                app.questionRepository.insertMultiple(*resultQuestions!!.toTypedArray())
            }
        }

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



        // only start bluetooth scanning if we have location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG, "don't have permission yet, requesting...")

            // permission is not granted

            // check if we should show an explanation (i.e. when the user has denied permission before)
            //if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            //} else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    IOTREC_PERMISSION_REQUEST_COARSE_LOCATION
                )
            //}
        } else {
            Log.d(TAG, "already have permission")
            // Permission has already been granted, start bluetooth scanning
            app.startBluetoothScanning()
        }

        // preference handling
        categoryViewModel = ViewModelProviders.of(this).get(PreferenceViewModel::class.java)
        categoriesDao = IotRecDatabase.getDatabase(this, GlobalScope).categoryDao()
        categoryRepository = CategoryRepository(categoriesDao)

        preferenceRepository = app.preferenceRepository
        experimentRepository = app.experimentRepository
        replyRepository = app.replyRepository
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            IOTREC_PERMISSION_REQUEST_COARSE_LOCATION -> {
                // if request is cancelled, the result arrays are empty
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, start bluetooth scanning
                    app.startBluetoothScanning()
                } else {
                    // permission denied
                    // display a message in ThingListFragment but find a way to start scanning and removing the message once permission is granted
                }
                return
            }

            // add other 'when' lines to check for other permissions this app might request

            else -> {
                // ignore all other requests
            }
        }
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
                ThingListFragment.newInstance()
            }
            R.id.navigation_preferences -> {
                PreferenceListFragment.newInstance(1)
            }
            R.id.navigation_profile -> {
                ProfileFragment()
            }
            R.id.navigation_experiment -> {
                ExperimentFragment()
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

        val bottomSheetFragment = ThingBottomSheetFragment.newInstance(thing, loginRepository.isLoggedIn())
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

        //GlobalScope.launch {
        //    loginRepository.saveUser(true)
        //}

        // load dialog fragment to select sub-categories
        //val fragmentTransaction = supportFragmentManager.beginTransaction()
        //fragmentTransaction.addToBackStack("preference_select_dialog")
        val fragment = PreferenceSelectDialogFragment.newInstance(category)
        fragment.show(supportFragmentManager, "preference_select_dialog")
    }

    override fun onQuestionListFragmentInteraction(question: Question) {
        Log.d(TAG, "a question list item was clicked")
    }

    override fun onFragmentInteraction(uri: Uri) {
        Log.d(TAG, "onFragmentInteraction")
    } // double preferenceDialogListener? */ /*, BeaconConsumer*/ {


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == START_LOGIN_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // if login has been completed (either successfully or unsuccessfully), reload profile fragment
            //supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ProfileFragment()).commit()

            if(data != null) {
                Log.d(TAG, "data is not null")

                val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
                val json = sharedPrefs.getString("user", "{}")
                val moshi = Moshi.Builder().add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe()).build()
                val adapter = moshi.adapter(User::class.java)
                val user = adapter.fromJson(json!!)

                if(data.getStringExtra("ACTION") == "signup" || (user != null && user.preferences.isEmpty())) {
                    Log.d(TAG, "action is signup or user has no preferences")

                    // show AlertDialog asking user to set preferences
                    val builder = AlertDialog.Builder(this)
                    builder
                        .setCancelable(false)
                        .setPositiveButton("OK, let's go!") { dialog, which ->
                            supportFragmentManager.beginTransaction().replace(
                                R.id.fragment_container,
                                PreferenceListFragment(),
                                R.id.navigation_preferences.toString()
                            ).commit()
                            navView.selectedItemId = R.id.navigation_preferences
                        }
                        .setNegativeButton("Not now") { dialog, _ -> dialog.cancel() }

                    if (data.getStringExtra("ACTION") == "login") {
                        Log.d(TAG, "got string extra login")
                        builder.setMessage("It looks like you have not set your preferences yet. In order to receive useful recommendations, please select them now.")
                    } else if (data.getStringExtra("ACTION") == "signup") {
                        Log.d(TAG, "got string extra signup")
                        builder.setMessage("Thank you for signing up. In order to receive useful recommendations, please select your personal preferences now.")
                    }

                    val alert = builder.create()
                    alert.setTitle("Welcome!")
                    alert.show()
                }
            } else {
                Log.d(TAG, "data is null")
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


    fun onPreferenceToggle(toggleButtonView: View) {
        val radioGroupView = toggleButtonView.parent as RadioGroup
        radioGroupView.check(0)
        radioGroupView.check(toggleButtonView.id)

        val selectedCategory = radioGroupView.tag as Category
        Log.d(TAG, "clicked category: $selectedCategory")

        when {
            toggleButtonView.id == R.id.button_negative -> GlobalScope.launch { app.loginRepository.setPreference(selectedCategory.textId, -1) }
            toggleButtonView.id == R.id.button_neutral -> GlobalScope.launch { app.loginRepository.setPreference(selectedCategory.textId, 0) }
            toggleButtonView.id == R.id.button_positive -> GlobalScope.launch { app.loginRepository.setPreference(selectedCategory.textId, 1) }
        }
    }

    override fun onPreferenceSelectDialogFragmentInteraction(preference: Preference) {
        Log.d(TAG, preference.toString())

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

