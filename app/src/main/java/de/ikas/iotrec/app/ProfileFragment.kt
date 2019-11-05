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
import com.google.android.material.snackbar.Snackbar

import de.ikas.iotrec.R
import de.ikas.iotrec.account.ui.LoginActivity
import android.content.SharedPreferences
import android.graphics.Typeface
import android.provider.Settings
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.view.marginBottom
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import de.ikas.iotrec.database.model.Category
import de.ikas.iotrec.database.model.Preference
import de.ikas.iotrec.preferences.ui.PreferenceViewModel
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.util.*

class ProfileFragment : Fragment() {

    private var listener: OnFragmentInteractionListener? = null

    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var mainActivity: MainActivity

    private val TAG = "ProfileFragment"

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        val username = mainActivity.loginRepository.user?.username
        val email = mainActivity.loginRepository.user?.email
        //val preferences = mainActivity.loginRepository.user?.preferences

        // get all UI elements of fragment
        val profileImage = view!!.findViewById<ImageView>(R.id.image)
        val usernameText = view!!.findViewById<TextView>(R.id.username)
        val emailText = view!!.findViewById<TextView>(R.id.email)
        val preferencesHeader = view!!.findViewById<TextView>(R.id.preferences_header)
        val preferencesContainer = view!!.findViewById<LinearLayout>(R.id.preferences_container)
        val preferencesContainerCard = view!!.findViewById<CardView>(R.id.preferences_container_card)
        val dislikesHeader = view!!.findViewById<TextView>(R.id.dislikes_header)
        val dislikesContainer = view!!.findViewById<LinearLayout>(R.id.dislikes_container)
        val dislikesContainerCard = view!!.findViewById<CardView>(R.id.dislikes_container_card)
        val logoutButton = view!!.findViewById<Button>(R.id.logout)
        val notLoggedInText = view!!.findViewById<TextView>(R.id.not_logged_in)
        val goToLoginButton = view!!.findViewById<Button>(R.id.go_to_login)
        val loadingSpinner = view!!.findViewById<ProgressBar>(R.id.loading)

        scope.launch {
            val currentPreferencesList = withContext(IO) {
                mainActivity.preferenceRepository.getCurrentPreferencesList()
            }

            Log.d(TAG, "currentPreferencesList: " + currentPreferencesList.toString())

            // split into positive (preferences) and negative (dislikes)
            val (preferences, dislikes) = currentPreferencesList.partition { it.value > 0 }
            Log.d(TAG, "preferences: " + preferences.toString())
            Log.d(TAG, "dislikes: " + dislikes.toString())

            // create a list of TextViews for preferences
            val preferenceTextViews = mutableListOf<TextView>()

            // create a list of TextViews for dislikes
            val dislikeTextViews = mutableListOf<TextView>()

            // if there are no preferences, create a placeholder string
            // if there are no dislikes, create a placeholder string
            if(preferences.isEmpty()) {
                val tv = TextView(mainActivity)
                tv.text = "You have not specified any preferences yet."
                preferenceTextViews.add(tv)
            } else {
                // get full categories for preferences
                val preferencesCategoryIds = mutableListOf<String>()
                for(preference in preferences) {
                    preferencesCategoryIds.add(preference.category)
                }

                val preferenceCategories = withContext(IO) {
                    mainActivity.categoryRepository.getCategories(preferencesCategoryIds)
                }

                Log.d(TAG, "preferenceCategories: " + preferenceCategories.toString())

                // collect headers for preferences
                val preferencesHeaders = mutableListOf<String>()
                for(preferenceCategory in preferenceCategories) {
                    if(preferenceCategory.parentTextId !in preferencesHeaders) {
                        preferencesHeaders.add(preferenceCategory.parentTextId)
                    }
                }
                preferencesHeaders.sort()

                // for each preference header, create a header TextView
                Log.d(TAG, "preferencesHeaders: " + preferencesHeaders.toString())
                for((index, pH) in preferencesHeaders.withIndex()) {
                    val tv = TextView(mainActivity)
                    tv.text = pH
                    tv.setTypeface(null, Typeface.BOLD)
                    tv.textSize = 18f
                    preferenceTextViews.add(tv)

                    // create a body TextView with the preferences
                    val tvChildren = TextView(mainActivity)
                    var tvString = ""
                    for((indexCat, preferenceCategory) in preferenceCategories.filter{ x -> x.parentTextId == pH }.withIndex()) {
                        tvString += preferenceCategory.name

                        // if it's not the last category, add a line break
                        if(indexCat != preferenceCategories.filter{ x -> x.parentTextId == pH }.size - 1) {
                            tvString += "\n"
                        }
                    }

                    tvChildren.text = tvString
                    tvChildren.textSize = 18f

                    // if it's not the last group/header, add some margin
                    if(index != preferencesHeaders.size - 1) {
                        tvChildren.setPadding(0, 0, 0, 12)
                        //val layoutParams = tvChildren.layoutParams as RelativeLayout.LayoutParams
                        //layoutParams.setMargins(12, 0, 0, 0)
                        //tvChildren.layoutParams = layoutParams
                    }

                    preferenceTextViews.add(tvChildren)
                }
            }

            if(dislikes.isEmpty()) {
                val tv = TextView(mainActivity)
                tv.text = "You have not specified any dislikes yet."
                dislikeTextViews.add(tv)
            } else {
                // get full categories for dislikes
                val dislikesCategoryIds = mutableListOf<String>()
                for(dislike in dislikes) {
                    dislikesCategoryIds.add(dislike.category)
                }

                val dislikeCategories = withContext(IO) {
                    mainActivity.categoryRepository.getCategories(dislikesCategoryIds)
                }

                Log.d(TAG, "dislikeCategories: " + dislikeCategories.toString())

                // collect headers for dislikes
                val dislikesHeaders = mutableListOf<String>()
                for(dislikeCategory in dislikeCategories) {
                    if(dislikeCategory.parentTextId !in dislikesHeaders) {
                        dislikesHeaders.add(dislikeCategory.parentTextId)
                    }
                }
                dislikesHeaders.sort()

                // for each dislike header, create a header TextView
                Log.d(TAG, "dislikesHeaders: " + dislikesHeaders.toString())
                for((index, dH) in dislikesHeaders.withIndex()) {
                    val tv = TextView(mainActivity)
                    tv.text = dH
                    tv.setTypeface(null, Typeface.BOLD)
                    tv.textSize = 18f
                    dislikeTextViews.add(tv)

                    // create a body TextView with the dislikes
                    val tvChildren = TextView(mainActivity)
                    var tvString = ""
                    for((indexCat, dislikeCategory) in dislikeCategories.filter{ x -> x.parentTextId == dH }.withIndex()) {
                        tvString += dislikeCategory.name

                        // if it's not the last category, add a line break
                        if(indexCat != dislikeCategories.filter{ x -> x.parentTextId == dH }.size - 1) {
                            tvString += "\n"
                        }
                    }

                    tvChildren.text = tvString
                    tvChildren.textSize = 18f


                    // if it's not the last group/header, add some margin
                    Log.d(TAG, "index: " + index.toString())
                    Log.d(TAG, "dislikesHeaders.size: " + dislikesHeaders.size.toString())
                    if(index != dislikesHeaders.size - 1) {
                        tvChildren.setPadding(0, 0, 0, 12)
                        //val layoutParams = tvChildren.layoutParams as RelativeLayout.LayoutParams
                        //layoutParams.setMargins(12, 0, 0, 0)
                        //tvChildren.layoutParams = layoutParams
                    }

                    dislikeTextViews.add(tvChildren)
                }
            }

            mainActivity.runOnUiThread {
                // show all preferences TextViews
                Log.d(TAG, "preferenceTextViews: " + preferenceTextViews.toString())
                preferencesContainer.removeAllViews()
                for (tv in preferenceTextViews) {
                    preferencesContainer.addView(tv)
                }

                // show all dislikes TextViews
                Log.d(TAG, "dislikeTextViews: " + dislikeTextViews.toString())
                dislikesContainer.removeAllViews()
                for (tv in dislikeTextViews) {
                    dislikesContainer.addView(tv)
                }

                if(mainActivity.loginRepository.user != null && username != null && username != "") {
                    preferencesHeader.visibility = View.VISIBLE
                    preferencesContainerCard.visibility = View.VISIBLE
                    dislikesHeader.visibility = View.VISIBLE
                    dislikesContainerCard.visibility = View.VISIBLE
                    loadingSpinner.visibility = View.GONE
                }
            }
        }

        /*
        mainActivity.preferenceRepository.getCurrentPreferences().observe(this, Observer { preferencesList ->
            Log.d(TAG, "preferencesList: " + preferencesList.toString())

            // split into positive (preferences) and negative (dislikes)
            val (preferences, dislikes) = preferencesList.partition { it.value > 0 }
            Log.d(TAG, "preferences" + preferences.toString())
            Log.d(TAG, "dislikes" + dislikes.toString())

            // create a list of TextViews for preferences
            val preferenceTextViews = mutableListOf<TextView>()

            // create a list of TextViews for dislikes
            val dislikeTextViews = mutableListOf<TextView>()

            // if there are no preferences, create a placeholder string
            // if there are no dislikes, create a placeholder string
            if(preferences.isEmpty() || dislikes.isEmpty()) {
                if(preferences.isEmpty()) {
                    val tv = TextView(mainActivity)
                    tv.text = "You have not specified any preferences yet."
                    preferenceTextViews.add(tv)
                }
                if(dislikes.isEmpty()) {
                    val tv = TextView(mainActivity)
                    tv.text = "You have not specified any dislikes yet."
                    dislikeTextViews.add(tv)
                }

                // show all preferences TextViews
                preferencesContainer.removeAllViews()
                for(tv in preferenceTextViews) {
                    preferencesContainer.addView(tv)
                }

                // show all dislikes TextViews
                dislikesContainer.removeAllViews()
                for(tv in dislikeTextViews) {
                    dislikesContainer.addView(tv)
                }

                if(mainActivity.loginRepository.user != null && username != null && username != "") {
                    preferencesHeader.visibility = View.VISIBLE
                    preferencesContainerCard.visibility = View.VISIBLE
                    dislikesHeader.visibility = View.VISIBLE
                    dislikesContainerCard.visibility = View.VISIBLE
                    loadingSpinner.visibility = View.GONE
                }
            } else {
                // get full categories for preferences
                val preferencesCategoryIds = mutableListOf<String>()
                for(preference in preferences) {
                    preferencesCategoryIds.add(preference.category)
                }
                // get full categories for dislikes
                val dislikesCategoryIds = mutableListOf<String>()
                for(dislike in dislikes) {
                    dislikesCategoryIds.add(dislike.category)
                }

                GlobalScope.launch {
                    val preferenceCategories = mainActivity.categoryRepository.getCategories(preferencesCategoryIds)
                    val dislikeCategories = mainActivity.categoryRepository.getCategories(dislikesCategoryIds)

                    Log.d(TAG, "preferenceCategories: " + preferenceCategories.toString())
                    Log.d(TAG, "dislikeCategories: " + dislikeCategories.toString())

                    // collect headers for preferences
                    val preferencesHeaders = mutableListOf<String>()
                    for(preferenceCategory in preferenceCategories) {
                        if(preferenceCategory.parentTextId !in preferencesHeaders) {
                            preferencesHeaders.add(preferenceCategory.parentTextId)
                        }
                    }
                    preferencesHeaders.sort()

                    // collect headers for dislikes
                    val dislikesHeaders = mutableListOf<String>()
                    for(dislikeCategory in dislikeCategories) {
                        if(dislikeCategory.parentTextId !in dislikesHeaders) {
                            dislikesHeaders.add(dislikeCategory.parentTextId)
                        }
                    }
                    dislikesHeaders.sort()

                    // for each preference header, create a header TextView
                    Log.d(TAG, "preferencesHeaders: " + preferencesHeaders.toString())
                    for((index, pH) in preferencesHeaders.withIndex()) {
                        val tv = TextView(mainActivity)
                        tv.text = pH
                        tv.setTypeface(null, Typeface.BOLD)
                        tv.textSize = 18f
                        preferenceTextViews.add(tv)

                        // create a body TextView with the preferences
                        val tvChildren = TextView(mainActivity)
                        var tvString = ""
                        for((indexCat, preferenceCategory) in preferenceCategories.filter{ x -> x.parentTextId == pH }.withIndex()) {
                            tvString += preferenceCategory.name

                            // if it's not the last category, add a line break
                            if(indexCat != preferenceCategories.filter{ x -> x.parentTextId == pH }.size - 1) {
                                tvString += "\n"
                            }
                        }

                        tvChildren.text = tvString
                        tvChildren.textSize = 18f

                        // if it's not the last group/header, add some margin
                        if(index != preferencesHeaders.size - 1) {
                            tvChildren.setPadding(0, 0, 0, 12)
                            //val layoutParams = tvChildren.layoutParams as RelativeLayout.LayoutParams
                            //layoutParams.setMargins(12, 0, 0, 0)
                            //tvChildren.layoutParams = layoutParams
                        }

                        preferenceTextViews.add(tvChildren)
                    }

                    // for each dislike header, create a header TextView
                    Log.d(TAG, "dislikesHeaders: " + dislikesHeaders.toString())
                    for((index, dH) in dislikesHeaders.withIndex()) {
                        val tv = TextView(mainActivity)
                        tv.text = dH
                        tv.setTypeface(null, Typeface.BOLD)
                        tv.textSize = 18f
                        dislikeTextViews.add(tv)

                        // create a body TextView with the dislikes
                        val tvChildren = TextView(mainActivity)
                        var tvString = ""
                        for((indexCat, dislikeCategory) in dislikeCategories.filter{ x -> x.parentTextId == dH }.withIndex()) {
                            tvString += dislikeCategory.name

                            // if it's not the last category, add a line break
                            if(indexCat != dislikeCategories.filter{ x -> x.parentTextId == dH }.size - 1) {
                                tvString += "\n"
                            }
                        }

                        tvChildren.text = tvString
                        tvChildren.textSize = 18f


                        // if it's not the last group/header, add some margin
                        Log.d(TAG, "index: " + index.toString())
                        Log.d(TAG, "dislikesHeaders.size: " + dislikesHeaders.size.toString())
                        if(index != dislikesHeaders.size - 1) {
                            tvChildren.setPadding(0, 0, 0, 12)
                            //val layoutParams = tvChildren.layoutParams as RelativeLayout.LayoutParams
                            //layoutParams.setMargins(12, 0, 0, 0)
                            //tvChildren.layoutParams = layoutParams
                        }

                        dislikeTextViews.add(tvChildren)
                    }

                    mainActivity.runOnUiThread {
                        // show all preferences TextViews
                        Log.d(TAG, "preferenceTextViews: " + preferenceTextViews.toString())
                        for (tv in preferenceTextViews) {
                            preferencesContainer.addView(tv)
                        }

                        // show all dislikes TextViews
                        Log.d(TAG, "dislikeTextViews: " + dislikeTextViews.toString())
                        for (tv in dislikeTextViews) {
                            dislikesContainer.addView(tv)
                        }

                        if(mainActivity.loginRepository.user != null && username != null && username != "") {
                            preferencesHeader.visibility = View.VISIBLE
                            preferencesContainerCard.visibility = View.VISIBLE
                            dislikesHeader.visibility = View.VISIBLE
                            dislikesContainerCard.visibility = View.VISIBLE
                            loadingSpinner.visibility = View.GONE
                        }
                    }
                }
            }
        })
        */

        // set up UI elements
        if(mainActivity.loginRepository.user != null && username != null && username != "") {
            Log.d(TAG, mainActivity.loginRepository.user.toString())
            Log.d(TAG, username.toString())
            usernameText.text = username
            emailText.text = email

            profileImage.visibility = View.VISIBLE
            usernameText.visibility = View.VISIBLE
            emailText.visibility = View.VISIBLE
            logoutButton.visibility = View.VISIBLE
            notLoggedInText.visibility = View.GONE
            goToLoginButton.visibility = View.GONE
        } else {
            profileImage.visibility = View.GONE
            usernameText.visibility = View.GONE
            emailText.visibility = View.GONE
            preferencesHeader.visibility = View.GONE
            preferencesContainerCard.visibility = View.GONE
            dislikesHeader.visibility = View.GONE
            dislikesContainerCard.visibility = View.GONE
            loadingSpinner.visibility = View.GONE
            logoutButton.visibility = View.GONE
            notLoggedInText.visibility = View.VISIBLE
            goToLoginButton.visibility = View.VISIBLE
        }


        logoutButton.setOnClickListener {
            mainActivity.loginRepository.logout()

            // show snackbar to confirm logout
            val snack = Snackbar.make(it,"You have been logged out.", Snackbar.LENGTH_LONG)
            snack.setAction("OK", View.OnClickListener {
                snack.dismiss()
            })
            snack.show()

            // change UI elements
            profileImage.visibility = View.GONE
            usernameText.visibility = View.GONE
            emailText.visibility = View.GONE
            preferencesHeader.visibility = View.GONE
            preferencesContainerCard.visibility = View.GONE
            dislikesHeader.visibility = View.GONE
            dislikesContainerCard.visibility = View.GONE
            loadingSpinner.visibility = View.GONE
            logoutButton.visibility = View.GONE
            notLoggedInText.visibility = View.VISIBLE
            goToLoginButton.visibility = View.VISIBLE
        }

        goToLoginButton.setOnClickListener {
            //show login fragment
            val intent = Intent(activity, LoginActivity::class.java)
            (activity as MainActivity).startActivityForResult(intent,
                MainActivity.START_LOGIN_ACTIVITY_REQUEST_CODE
            )
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
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ProfileFragment().apply {}
    }

    // when shared preferences change, update profile data on the screen
    private val onSharedPrefsChanged: SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            Log.d(TAG, "shared Preferences have changed")
            populateProfileInfos(view)
        }
}
