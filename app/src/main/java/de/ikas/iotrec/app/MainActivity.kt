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

    private lateinit var app: IotRecApplication

    // declare relevant objects for database access
    private lateinit var categoryViewModel: PreferenceViewModel
    private lateinit var categoriesDao: CategoryDao
    lateinit var categoryRepository: CategoryRepository
    lateinit var preferenceRepository: PreferenceRepository
    lateinit var loginRepository: LoginRepository
    lateinit var userPreferences: MutableList<Preference>
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

    // show respective fragment when navigation items are clicked
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
                val token = sharedPrefs.getString("userToken", "")

                // show ProfileFragment
                setTitle(R.string.title_profile)
                loadFragment(item.itemId)

                if (token == "") {
                    // if user is not logged in, show LoginActivity on top
                    loginRepository.logout()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivityForResult(intent, START_LOGIN_ACTIVITY_REQUEST_CODE)
                }

                return@OnNavigationItemSelectedListener true
            }
        }

        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app = application as IotRecApplication
        loginRepository = app.loginRepository
        if(loginRepository.user != null) {
            userPreferences = loginRepository.user!!.preferences
        }

        // initialize navigation
        setContentView(R.layout.activity_main)
        navView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)


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

            // if successful, update database
            if (resultQ.isSuccessful) {
                val resultQuestions = resultQ.body()
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
                    // if token is invalid, remove it from sharedPrefs, resulting in a logout
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

        // we can only start bluetooth scanning if we have location permission
        // if it's not granted yet, request it
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                IOTREC_PERMISSION_REQUEST_COARSE_LOCATION
            )
        } else {
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            IOTREC_PERMISSION_REQUEST_COARSE_LOCATION -> {
                // if request is cancelled, the result arrays are empty
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, start bluetooth scanning
                    app.startBluetoothScanning()
                } else {
                    // permission denied
                    // idea: display a message in ThingListFragment but find a way to start scanning and removing the message once permission is granted
                }
                return
            }

            // in the future: add other 'when' lines to check for other permissions this app might request

            else -> {
                // ignore all other requests
            }
        }
    }


    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()

        // connect back the broadcastReceiver to listen for messages from BluetoothScannerService
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, IntentFilter("BLUETOOTH_SCANNER_SERVICE"))
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }

    // helper function to show a fragment
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
        }
    }

    // when a thing in ListFragment was clicked, show the bottom sheet
    override fun onThingListFragmentInteraction(thing: Thing) {
        val bottomSheetFragment = ThingBottomSheetFragment.newInstance(thing, loginRepository.isLoggedIn())
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }

    // when a top-level category in PreferenceListFragment was clicked, show the selection dialog
    override fun onPreferenceListFragmentInteraction(category: Category) {
        // load dialog fragment to select sub-categories
        val fragment = PreferenceSelectDialogFragment.newInstance(category)
        fragment.show(supportFragmentManager, "preference_select_dialog")
    }

    override fun onQuestionListFragmentInteraction(question: Question) {
        // has to be implemented but clicking in item in this list does not do anything
    }

    override fun onFragmentInteraction(uri: Uri) {
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == START_LOGIN_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // if login has been completed (either successfully or unsuccessfully), reload profile fragment
            if(data != null) {
                val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
                val json = sharedPrefs.getString("user", "{}")
                val moshi = Moshi.Builder().add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe()).build()
                val adapter = moshi.adapter(User::class.java)
                val user = adapter.fromJson(json!!)

                // when a user has just signed up or hasn't set any preferences yet, ask them to do that
                if(data.getStringExtra("ACTION") == "signup" || (user != null && user.preferences.isEmpty())) {
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

                    // set message according to login or signup
                    if (data.getStringExtra("ACTION") == "login") {
                        builder.setMessage("It looks like you have not set your preferences yet. In order to receive useful recommendations, please select them now.")
                    } else if (data.getStringExtra("ACTION") == "signup") {
                        builder.setMessage("Thank you for signing up. In order to receive useful recommendations, please select your personal preferences now.")
                    }

                    val alert = builder.create()
                    alert.setTitle("Welcome!")
                    alert.show()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    // when a preference was changed in the selection modal, relay that information to loginRepository
    fun onPreferenceToggle(toggleButtonView: View) {
        val radioGroupView = toggleButtonView.parent as RadioGroup
        radioGroupView.check(0)
        radioGroupView.check(toggleButtonView.id)

        val selectedCategory = radioGroupView.tag as Category

        when {
            toggleButtonView.id == R.id.button_negative -> GlobalScope.launch { app.loginRepository.setPreference(selectedCategory.textId, -1) }
            toggleButtonView.id == R.id.button_neutral -> GlobalScope.launch { app.loginRepository.setPreference(selectedCategory.textId, 0) }
            toggleButtonView.id == R.id.button_positive -> GlobalScope.launch { app.loginRepository.setPreference(selectedCategory.textId, 1) }
        }
    }

    override fun onPreferenceSelectDialogFragmentInteraction(preference: Preference) {
    }
}

