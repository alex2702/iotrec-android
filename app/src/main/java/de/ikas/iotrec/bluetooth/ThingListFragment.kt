package de.ikas.iotrec.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.ikas.iotrec.R
import de.ikas.iotrec.app.MainActivity
import de.ikas.iotrec.database.model.Thing
import androidx.recyclerview.widget.DividerItemDecoration
import de.ikas.iotrec.app.IotRecApplication
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import java.lang.Exception


class ThingListFragment : Fragment() {

    private lateinit var mainActivity: MainActivity
    lateinit var app: IotRecApplication

    private var listener: OnListFragmentInteractionListener? = null
    private lateinit var thingViewModel: ThingViewModel

    private val TAG = "ThingListFragment"
    private val REQUEST_ENABLE_BLUETOOTH = 2702
    private val REQUEST_ENABLE_LOCATION = 1900

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainActivity = activity as MainActivity
        app = mainActivity.application as IotRecApplication

        thingViewModel = ViewModelProviders.of(this).get(ThingViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_thing_list, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        val loadingCircle = view.findViewById(R.id.loading) as ProgressBar
        loadingCircle.visibility = View.VISIBLE
        val bluetoothNotEnabledMessage = view.findViewById<TextView>(R.id.bluetooth_not_enabled)
        val locationNotEnabledMessage = view.findViewById<TextView>(R.id.location_not_enabled)
        val bluetoothEnableButton = view.findViewById<Button>(R.id.enable_bluetooth)
        val locationEnableButton = view.findViewById<Button>(R.id.enable_location)

        val locationEnabled: Boolean
        var locationGpsEnabled = false
        var locationNetworkEnabled = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationEnabled = app.locationManager.isLocationEnabled
        } else {
            try {
                locationGpsEnabled = app.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            } catch(e: Exception) {
                Log.e(TAG, e.toString())
            }

            try {
                locationNetworkEnabled = app.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            } catch(e: Exception) {
                Log.e(TAG, e.toString())
            }

            locationEnabled = locationGpsEnabled || locationNetworkEnabled
        }

        if(app.bluetoothAdapter == null || !app.bluetoothAdapter.isEnabled || !locationEnabled) {
            if(app.bluetoothAdapter == null) {
                // bluetooth is not supported by this device
                Log.d(TAG, "Bluetooth is not supported")
                loadingCircle.visibility = View.GONE
                bluetoothNotEnabledMessage.visibility = View.VISIBLE
                bluetoothNotEnabledMessage.text = "Your device does not support Bluetooth."
                bluetoothEnableButton.visibility = View.GONE
            } else if(!app.bluetoothAdapter.isEnabled) {
                // bluetooth is not enabled
                loadingCircle.visibility = View.GONE
                bluetoothNotEnabledMessage.visibility = View.VISIBLE
                bluetoothEnableButton.visibility = View.VISIBLE

                bluetoothEnableButton.setOnClickListener {
                    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(intent, REQUEST_ENABLE_BLUETOOTH)
                }
            }

            if(!locationEnabled) {
                loadingCircle.visibility = View.GONE
                locationNotEnabledMessage.visibility = View.VISIBLE
                locationEnableButton.visibility = View.VISIBLE

                locationEnableButton.setOnClickListener {
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivityForResult(intent, REQUEST_ENABLE_LOCATION)
                }
            }
        } else {
            bluetoothNotEnabledMessage.visibility = View.GONE
            bluetoothEnableButton.visibility = View.GONE
            locationNotEnabledMessage.visibility = View.GONE
            locationEnableButton.visibility = View.GONE


            // Set the adapter
            if (recyclerView is RecyclerView) {
                with(recyclerView) {
                    layoutManager = LinearLayoutManager(context)

                    adapter = ThingRecyclerViewAdapter(context, listener)

                    thingViewModel.allThingsInRange.observe(viewLifecycleOwner, Observer { things ->
                        // Update the cached copy of the things in the adapter.
                        things?.let {
                            (adapter as ThingRecyclerViewAdapter).setThings(it)
                            loadingCircle.visibility = View.GONE
                        }
                    })

                    val dividerItemDecoration = DividerItemDecoration(
                        recyclerView.context,
                        (layoutManager as LinearLayoutManager).orientation
                    )
                    recyclerView.addItemDecoration(dividerItemDecoration)
                }
            }
        }

        return view
    }

    /* https://stackoverflow.com/a/37279212 */

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener")
        }
    }



    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson
     * [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnListFragmentInteractionListener { // double listener?
        fun onThingListFragmentInteraction(thing: Thing) {
            Log.d("ThingListFragment", "a list item was clicked")
        }
    }


    companion object {
        @JvmStatic
        fun newInstance() =
            ThingListFragment().apply {}
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_ENABLE_BLUETOOTH) {
            // check if bluetooth is enabled now
            if(app.bluetoothAdapter.isEnabled) {
                // if yes, reload the fragment to re-initialize all checks
                mainActivity.supportFragmentManager
                    .beginTransaction()
                    .detach(this)
                    .attach(this)
                    .commit()
            }
        } else if(requestCode == REQUEST_ENABLE_LOCATION) {
            // check if location is enabled now
            val locationEnabled: Boolean
            var locationGpsEnabled = false
            var locationNetworkEnabled = false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                locationEnabled = app.locationManager.isLocationEnabled
            } else {
                try {
                    locationGpsEnabled = app.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                } catch(e: Exception) {
                    Log.e(TAG, e.toString())
                }

                try {
                    locationNetworkEnabled = app.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                } catch(e: Exception) {
                    Log.e(TAG, e.toString())
                }

                locationEnabled = locationGpsEnabled || locationNetworkEnabled
            }

            if(locationEnabled) {
                // if yes, reload the fragment to re-initialize all checks
                mainActivity.supportFragmentManager
                    .beginTransaction()
                    .detach(this)
                    .attach(this)
                    .commit()
            }
        }
    }
}
