package de.ikas.iotrec.account.data

import android.content.Context
import android.util.Log
import de.ikas.iotrec.account.data.model.LoggedInUser
import de.ikas.iotrec.network.IotRecApiInit
import kotlinx.coroutines.*
import retrofit2.Response
import android.preference.PreferenceManager
import de.ikas.iotrec.account.data.model.RegisteredUser
import de.ikas.iotrec.account.data.model.User
import de.ikas.iotrec.app.IotRecApplication
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import de.ikas.iotrec.database.model.Preference
import de.ikas.iotrec.database.repository.*
import java.util.*


/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(private val iotRecApi: IotRecApiInit, private val context: Context) {

    private val TAG = "LoginRepository"
    private var app: IotRecApplication
    private var categoryRepository: CategoryRepository
    private var preferenceRepository: PreferenceRepository
    private var thingRepository: ThingRepository
    private var feedbackRepository: FeedbackRepository
    private var recommendationRepository: RecommendationRepository
    private var ratingRepository: RatingRepository
    private var experimentRepository: ExperimentRepository
    private var replyRepository: ReplyRepository
    private var questionRepository: QuestionRepository

    // in-memory cache of the user object
    var user: User? = null
        private set

    var token: String? = null
        private set

    init {
        // get global app instance
        app = context.applicationContext as IotRecApplication

        // get app's repository instances
        categoryRepository = app.categoryRepository
        preferenceRepository = app.preferenceRepository
        thingRepository = app.thingRepository
        feedbackRepository = app.feedbackRepository
        recommendationRepository = app.recommendationRepository
        ratingRepository = app.ratingRepository
        experimentRepository = app.experimentRepository
        replyRepository = app.replyRepository
        questionRepository = app.questionRepository

        // set up shared prefs, Moshi for deserializing json string
        val moshi = Moshi.Builder().add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe()).build()
        val adapter = moshi.adapter(User::class.java)
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val json = sharedPrefs.getString("user", "{}")
        val user = adapter.fromJson(json!!)
        this.user = user
        this.token = sharedPrefs.getString("userToken", "")
    }

    fun logout() {
        // empty in-memory objects
        user = null
        token = null

        // empty shared prefs
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPrefs.edit()
        editor.remove("user")
        editor.remove("userToken")
        editor.remove("experimentCurrentStep")
        editor.remove("experimentCurrentRun")
        editor.apply()

        // empty relevant database tables
        GlobalScope.launch {
            preferenceRepository.deleteAll()
            thingRepository.deleteAll()
            recommendationRepository.deleteAll()
            feedbackRepository.deleteAll()
            ratingRepository.deleteAll()
            experimentRepository.deleteAll()
            replyRepository.deleteAll()
            categoryRepository.deleteAll()
            questionRepository.deleteAll()
        }
    }

    suspend fun login(username: String, password: String): Response<LoggedInUser> {
        val result = iotRecApi.login(username, password)

        if(result.isSuccessful) {
            setLoggedInUser(result.body() as LoggedInUser)
        }

        return result
    }

    suspend fun register(username: String, email: String, password: String): Response<RegisteredUser> {
        val result = iotRecApi.register(username, email, password)

        if(result.isSuccessful) {
            setRegisteredUser(result.body() as RegisteredUser)
        }

        return result
    }

    private suspend fun setLoggedInUser(loggedInUser: LoggedInUser) {
        // save to repository (only accessible during lifecycle)
        this.user = loggedInUser.user
        this.token = loggedInUser.token

        // save to SharedPreferences
        saveUser(false)

        // save user preferences to database
        preferenceRepository.insertMultiple(*this.user!!.preferences.toTypedArray())

        // save experiments to database
        experimentRepository.insertMultiple(*this.user!!.experiments.toTypedArray())
    }

    private suspend fun setRegisteredUser(registeredUser: RegisteredUser) {
        // save to repository (only accessible during lifecycle)
        this.user = User(registeredUser.id, registeredUser.username, registeredUser.email, registeredUser.preferences, registeredUser.experiments)
        this.token = registeredUser.token

        // save to SharedPreferences
        saveUser(false)

        // save experiments to database
        experimentRepository.insertMultiple(*this.user!!.experiments.toTypedArray())
    }

    // saves current user object to shared preferences and DB and, optionally, syncs it to the backend
    suspend fun saveUser(userChangedLocally: Boolean) {
        // shared preferences
        val moshi = Moshi.Builder().add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe()).build()
        val adapter = moshi.adapter(User::class.java)
        val json = adapter.toJson(this.user)
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPrefs.edit()
        editor.putString("user", json)
        editor.putString("userToken", this.token)
        editor.apply()

        // if user object was changed locally, sync back to server  //TODO never used, remove (including API endpoint)?
        if(userChangedLocally) {
            iotRecApi.updateUserFromApp(this.user!!.id, this.user!!)
        }
    }

    suspend fun setPreference(categoryId: String, value: Int) {
        // only lowest-level categories can be selected
        if(categoryRepository.getCategory(categoryId).level > 1) {
            // create bar preference object to submit to API
            val preference = Preference("", categoryId, value, this.user!!.id)

            // preference is removed
            if(value == 0) {
                // only go on if the user did actually have that preference
                if(this.user!!.preferences.any { x -> x.category == preference.category }) {
                    val pref = this.user!!.preferences.find { x -> x.category == preference.category }
                    // remove from in-memory user object
                    val removedResult = this.user!!.preferences.remove(pref)
                    // remove from database
                    preferenceRepository.delete(pref!!.id)
                    // remove from backend
                    iotRecApi.deletePreference(this.user!!.id, pref.id)
                }
            } else {    // preference or dislike is added (or an existing one is toggled to the other value)
                // check if preference has existed before
                if(this.user!!.preferences.any { x -> x.category == preference.category }) {
                    // if a pref has values -1 or 1 and has existed before, update it
                    val pref = this.user!!.preferences.find { x -> x.category == preference.category }
                    // update in-memory object
                    pref!!.value = preference.value
                    // update database
                    preferenceRepository.update(pref)
                    // update backend
                    iotRecApi.updatePreference(this.user!!.id, pref.id, pref)
                } else {
                    // if a pref has values -1 or 1 and has not existed before, create it
                    try {
                        // API request
                        val result = iotRecApi.addPreference(this.user!!.id, preference)
                        if (result.isSuccessful) {
                            val resultPreference = result.body()
                            if (resultPreference != null) {
                                // add to database
                                preferenceRepository.insert(resultPreference)
                                // add to in-memory object
                                this.user!!.preferences.add(resultPreference)
                            }
                        }
                    } catch (e: Throwable) {
                        Log.d(TAG, e.toString())
                    }
                }
            }

            // update user profile in SharedPreferences and database, but don't sync to API (done separately above)
            saveUser(false)
        }
    }

    fun isLoggedIn(): Boolean {
        return this.user != null && this.user!!.username != null
    }
}
