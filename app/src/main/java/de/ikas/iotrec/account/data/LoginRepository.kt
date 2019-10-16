package de.ikas.iotrec.account.data

import android.content.Context
import android.util.Log
import de.ikas.iotrec.account.data.model.LoggedInUser
import de.ikas.iotrec.network.IotRecApiInit
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.await
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.preference.PreferenceManager
import de.ikas.iotrec.account.data.model.RegisteredUser
import de.ikas.iotrec.account.data.model.User
import de.ikas.iotrec.app.IotRecApplication
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types.newParameterizedType
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import de.ikas.iotrec.database.model.Category
import de.ikas.iotrec.database.model.Preference
import de.ikas.iotrec.database.repository.*


/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

//class LoginRepository(val dataSource: LoginDataSource) {
class LoginRepository(private val iotRecApi: IotRecApiInit, private val context: Context) {
//object LoginRepository(private val iotRecApi: IotRecApiInit) {

    private val TAG = "LoginRepository"
    private lateinit var app: IotRecApplication
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var preferenceRepository: PreferenceRepository
    private lateinit var thingRepository: ThingRepository
    private lateinit var feedbackRepository: FeedbackRepository
    private lateinit var recommendationRepository: RecommendationRepository
    //private lateinit var ratingRepository: ratingRepository   //TODO

    // in-memory cache of the user object
    var user: User? = null
        private set

    var token: String? = null
        private set

    //val isLoggedIn: Boolean
    //    get() = user != null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore

        // TODO get logged in user from shared prefs
        //user = null

        app = context.applicationContext as IotRecApplication
        categoryRepository = app.categoryRepository
        preferenceRepository = app.preferenceRepository
        thingRepository = app.thingRepository
        feedbackRepository = app.feedbackRepository
        recommendationRepository = app.recommendationRepository
        //ratingRepository = app.ratingRepository      // TODO

        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(User::class.java)
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val json = sharedPrefs.getString("user", "{}")
        val user = adapter.fromJson(json!!)
        this.user = user
        this.token = sharedPrefs.getString("userToken", "")
    }

    fun logout() {
        user = null
        token = null

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPrefs.edit()
        editor.remove("user")
        editor.remove("userToken")
        editor.apply()

        // empty the preferences table

        GlobalScope.launch {
            preferenceRepository.deleteAll()
            categoryRepository.deleteAll()
            thingRepository.deleteAll()
            recommendationRepository.deleteAll()
            feedbackRepository.deleteAll()
            //ratingsRepository.deleteAll()   //TODO
        }


        // TODO anything else to clear?
    }

    suspend fun login(username: String, password: String): Response<LoggedInUser> {
        // handle login
        //val result = dataSource.login(username, password).await()

        val result = iotRecApi.login(username, password)

        Log.d(TAG, result.toString())

        if(result.isSuccessful) {
            setLoggedInUser(result.body() as LoggedInUser)
        }

        //if (result is Result.Success<*>) {
        //    setLoggedInUser(result.data as LoggedInUser)
        //}

        return result
    }

    suspend fun register(username: String, email: String, password: String): Response<RegisteredUser> {
        val result = iotRecApi.register(username, email, password)

        Log.d(TAG, result.toString())

        if(result.isSuccessful) {
            setRegisteredUser(result.body() as RegisteredUser)
        }

        //if (result is Result.Success<*>) {
        //    setLoggedInUser(result.data as LoggedInUser)
        //}

        return result
    }

    private suspend fun setLoggedInUser(loggedInUser: LoggedInUser) {
        // save to repository (only accessible during lifecycle)
        this.user = loggedInUser.user
        this.token = loggedInUser.token

        // save to SharedPreferences
        saveUser(false)
    }

    private suspend fun setRegisteredUser(registeredUser: RegisteredUser) {
        // save to repository (only accessible during lifecycle)
        this.user = User(registeredUser.id, registeredUser.username, registeredUser.email, registeredUser.preferences)
        this.token = registeredUser.token

        // save to SharedPreferences
        saveUser(false)
    }

    fun syncUserProfile() {
        // check if a user is logged in
        if(isLoggedIn()) {
            // query current-user and collect up-to-date data
            // TODO

            // save data to User object and to Shared Preferences
            // TODO

            //updateUserToApp function in iotRecApi
        }
    }

    // saves current user object to shared preferences and DB and syncs it to the backend
    suspend fun saveUser(userChangedLocally: Boolean) {
        // shared preferences
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(User::class.java)
        val json = adapter.toJson(this.user)
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPrefs.edit()
        editor.putString("user", json)
        editor.putString("userToken", this.token)
        editor.apply()

        // save all preferences of the user to the database
        for(preference in this.user!!.preferences) {
            preferenceRepository.insert(preference)
        }

        // if user object was changed locally, sync back to server
        if(userChangedLocally) {
            iotRecApi.updateUserFromApp(this.user!!.id, this.user!!)
        }

        //for(preference in this.user!!.preferences) {
        //    categoryRepository.setCategorySelected(preference, true)
        //}
        //Log.d(TAG, this.user!!.preferences.size.toString() + " - " + this.user!!.preferences.toString())
        // TODO don't do this
        //categoryRepository.setAllCategoriesSelectedFalse()
        //val rowsChanged = categoryRepository.setCategoriesSelectedTrue(this.user!!.preferences)
        //Log.d(TAG, "rowsChanged: $rowsChanged")
    }

    // TODO
    // verify the token found in SharedPreferences
    // if it's still valid, the user is still logged in
    // if it's invalid, initiate logout
    //suspend fun verifyToken(token: String) {

    //}


    suspend fun setPreference(preferenceId: String, value: Int) {
        Log.d(TAG, "setPreference")
        if(categoryRepository.getCategory(preferenceId).level > 1) {   // only lowest-level categories can be selected
            val preference = Preference(preferenceId, value, this.user!!.id)
            Log.d(TAG, preference.toString())

            if(value == 0) {
                Log.d(TAG, "value is 0")
                Log.d(TAG, this.user!!.preferences.toString())

                if(this.user!!.preferences.any { x -> x.category == preference.category }) {  // TODO does contain work with different values?
                    val pref = this.user!!.preferences.find { x -> x.category == preference.category }

                    Log.d(TAG, "Found existing pref")
                    // if a pref has a value of 0 and has existed before, remove it
                    val removedResult = this.user!!.preferences.remove(pref)
                    Log.d(TAG, "Removed preference ($removedResult) - $pref")
                    preferenceRepository.delete(preference.category)
                    iotRecApi.deletePreference(this.user!!.id, preference.category)
                } else {
                    Log.d(TAG, "did not find existing pref")
                }
            } else {
                Log.d(TAG, "value is non-zero")

                if(this.user!!.preferences.any { x -> x.category == preference.category }) {
                    Log.d(TAG, "Found existing pref $preference")

                    // if a pref has values -1 or 1 and has existed before, update it
                    val pref = this.user!!.preferences.find { x -> x.category == preference.category }
                    pref!!.value = preference.value // TODO check if this works

                    Log.d(TAG, this.user!!.id.toString())
                    Log.d(TAG, pref.category)
                    Log.d(TAG, pref.toString())

                    iotRecApi.updatePreference(this.user!!.id, pref.category, pref)
                } else {
                    Log.d(TAG, "did not find existing pref")

                    // if a pref has values -1 or 1 and has not existed before, create it
                    this.user!!.preferences.add(preference)
                    iotRecApi.addPreference(this.user!!.id, preference)
                }
            }

            // update user profile in SharedPreferences and database, but don't sync to API (done separately above)
            saveUser(false)
        }
    }

    /*
    suspend fun removePreferenceFromAccount(preference: Preference) {
        if(categoryRepository.getCategory(preference.id).level > 1) {   // only lowest-level categories can be selected
            this.user!!.preferences.remove(preference)
            preferenceRepository.delete(preference.id)
            saveUser(true) // causes changes to be saved to DB and API
        }
    }

    suspend fun addPreferenceToAccount(preference: Preference) {
        if(categoryRepository.getCategory(preference.id).level > 1) {   // only lowest-level categories can be selected
            this.user!!.preferences.add(preference)
            //categoryRepository.setCategorySelected(category.textId, true)
            saveUser(true) // causes changes to be saved to DB and API
        }
    }
    */


    fun isLoggedIn(): Boolean {
        return this.user != null && this.user!!.username != null
    }
}
