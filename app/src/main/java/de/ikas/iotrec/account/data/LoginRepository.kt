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
import de.ikas.iotrec.database.repository.CategoryRepository


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

        // TODO set all preferences in db to selected=false
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
        saveUser()
    }

    private suspend fun setRegisteredUser(registeredUser: RegisteredUser) {
        // save to repository (only accessible during lifecycle)
        this.user = User(registeredUser.username, registeredUser.email, registeredUser.preferences)
        this.token = registeredUser.token

        // save to SharedPreferences
        saveUser()
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
    suspend fun saveUser() {
        // shared preferences
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(User::class.java)
        val json = adapter.toJson(this.user)
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPrefs.edit()
        editor.putString("user", json)
        editor.putString("userToken", this.token)
        editor.apply()

        // mark all preferences of the user as selected in the database
        //for(preference in this.user!!.preferences) {
        //    categoryRepository.setCategorySelected(preference, true)
        //}
        Log.d(TAG, this.user!!.preferences.size.toString() + " - " + this.user!!.preferences.toString())
        // TODO don't do this
        //categoryRepository.setAllCategoriesSelectedFalse()
        val rowsChanged = categoryRepository.setCategoriesSelectedTrue(this.user!!.preferences)
        //Log.d(TAG, "rowsChanged: $rowsChanged")

        // TODO right now, this is called after every signup/login => unnecessary
        //iotRecApi.updateUserFromApp(this.user!!)
    }

    // TODO
    // verify the token found in SharedPreferences
    // if it's still valid, the user is still logged in
    // if it's invalid, initiate logout
    //suspend fun verifyToken(token: String) {

    //}

    suspend fun removePreferenceFromAccount(category: Category) {
        if(category.level > 0) {
            this.user!!.preferences.remove(category.textId)
            categoryRepository.setCategorySelected(category.textId, false)
            saveUser()
        }
    }

    suspend fun addPreferenceToAccount(category: Category) {
        if(category.level > 0) {
            this.user!!.preferences.add(category.textId)
            categoryRepository.setCategorySelected(category.textId, true)
            saveUser()
        }
    }

    fun isLoggedIn(): Boolean {
        return this.user != null && this.user!!.username != null
    }
}
