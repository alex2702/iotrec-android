package de.ikas.iotrec.account.data

import android.util.Log
import de.ikas.iotrec.account.data.model.LoggedInUser
import de.ikas.iotrec.network.IotRecApiInit
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.await
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences




/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

//class LoginRepository(val dataSource: LoginDataSource) {
class LoginRepository(private val iotRecApi: IotRecApiInit) {

    private val TAG = "LoginRepository"

    // in-memory cache of the loggedInUser object
    var user: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null
    }

    fun logout() {
        user = null
        //dataSource.logout()
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

    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        //TODO put token in local storage, then I can access it from token interceptor
    }
}
