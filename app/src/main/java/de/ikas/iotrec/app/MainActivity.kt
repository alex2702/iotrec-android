package de.ikas.iotrec.app

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import de.ikas.iotrec.R
import de.ikas.iotrec.bluetooth.dummy.DummyContent
import de.ikas.iotrec.bluetooth.ui.ThingListFragment
import java.util.logging.Level
import java.util.logging.Logger
import android.support.v4.view.accessibility.AccessibilityEventCompat.setAction
import android.content.Intent
import de.ikas.iotrec.bluetooth.service.BluetoothScannerService


class MainActivity : AppCompatActivity() {

    private lateinit var textMessage: TextView

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
        }

        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        textMessage = findViewById(R.id.message)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        val intent = Intent(this, BluetoothScannerService::class.java)
        intent.setAction(BluetoothScannerService.ACTION_START_FOREGROUND_SERVICE)
        startService(intent)
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
}
