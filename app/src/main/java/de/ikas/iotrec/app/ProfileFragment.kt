package de.ikas.iotrec.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar

import de.ikas.iotrec.R
import de.ikas.iotrec.account.ui.LoginActivity
import android.content.SharedPreferences
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ProfileFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class ProfileFragment : Fragment() {

    //private var param1: String? = null
    //private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

    private lateinit var sharedPrefs: SharedPreferences

    private lateinit var mainActivity: MainActivity

    private val TAG = "ProfileFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        */

        mainActivity = activity as MainActivity

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity)
        sharedPrefs.registerOnSharedPreferenceChangeListener(onSharedPrefsChanged)

        Log.d(TAG, "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Log.d(TAG, "onCreateView")

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        populateProfileInfos(view)

        return view
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        Log.d(TAG, "onAttach")

        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(onSharedPrefsChanged)
    }

    private fun populateProfileInfos(view: View?) {
        // get info of logged in user
        //val username = sharedPrefs.getString("user.username", "")
        //val email = sharedPrefs.getString("user.email", "")
        val username = mainActivity.loginRepository.user?.username
        val email = mainActivity.loginRepository.user?.email
        val preferences = mainActivity.loginRepository.user?.preferences
        //preferences?.sort()   // TODO fix this

        // get all UI elements of fragment
        val usernameText = view!!.findViewById<TextView>(R.id.username)
        val emailHeader = view!!.findViewById<TextView>(R.id.email_header)
        val emailText = view!!.findViewById<TextView>(R.id.email)
        val preferencesHeader = view!!.findViewById<TextView>(R.id.preferences_header)
        val preferencesText = view!!.findViewById<TextView>(R.id.preferences)
        val logoutButton = view!!.findViewById<Button>(R.id.logout)
        val notLoggedInText = view!!.findViewById<TextView>(R.id.not_logged_in)
        val goToLoginButton = view!!.findViewById<Button>(R.id.go_to_login)

        // set up UI elements
        if(mainActivity.loginRepository.user != null && username != null && username != "") {
            Log.d(TAG, mainActivity.loginRepository.user.toString())
            Log.d(TAG, username.toString())
            usernameText.text = username
            emailText.text = email
            //preferencesText.text = preferences?.joinToString(separator = "\n")

            var preferencesString = ""
            for(pref in preferences!!) {
                preferencesString += "${pref.category} (${pref.value})\n"
            }
            preferencesText.text = preferencesString

            usernameText.visibility = View.VISIBLE
            emailHeader.visibility = View.VISIBLE
            emailText.visibility = View.VISIBLE
            preferencesHeader.visibility = View.VISIBLE
            preferencesText.visibility = View.VISIBLE
            logoutButton.visibility = View.VISIBLE
            notLoggedInText.visibility = View.GONE
            goToLoginButton.visibility = View.GONE
        } else {
            usernameText.visibility = View.GONE
            emailHeader.visibility = View.GONE
            emailText.visibility = View.GONE
            preferencesHeader.visibility = View.GONE
            preferencesText.visibility = View.GONE
            logoutButton.visibility = View.GONE
            notLoggedInText.visibility = View.VISIBLE
            goToLoginButton.visibility = View.VISIBLE
        }


        logoutButton.setOnClickListener {
            //TODO use logout method from LoginRepo?

            mainActivity.loginRepository.logout()


            //clear profile data from sharedPrefs
            //val editor = sharedPrefs.edit()
            //editor.remove("user.token")
            //editor.remove("user.username")
            //editor.remove("user.email")
            //editor.remove("user")
            //editor.apply()

            // show snackbar to confirm logout
            val snack = Snackbar.make(it,"You have been logged out.",Snackbar.LENGTH_LONG)
            snack.show()

            // change UI elements
            usernameText.visibility = View.GONE
            emailText.visibility = View.GONE
            preferencesText.visibility = View.GONE
            logoutButton.visibility = View.GONE
            notLoggedInText.visibility = View.VISIBLE
            goToLoginButton.visibility = View.VISIBLE

            // TODO also clear backend information from database?
        }

        goToLoginButton.setOnClickListener {
            //show login fragment
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    // when shared preferences change, update profile data on the screen
    private val onSharedPrefsChanged: SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            Log.d(TAG, "shared Preferences have changed")
            populateProfileInfos(view)
        }
}
