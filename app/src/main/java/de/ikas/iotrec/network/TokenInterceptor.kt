package de.ikas.iotrec.network

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

//source: https://medium.com/@theanilpaudel/using-the-power-of-retrofit-okhttp-and-dagger-2-for-jwt-token-authentication-ad8db6121eac

class TokenInterceptor constructor(val context: Context) : Interceptor {

    private val TAG = "TokenInterceptor"

    override fun intercept(chain: Interceptor.Chain) : Response {

        // get current JSON Web Token from shared preferences
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val token = sharedPrefs.getString("userToken", "")

        val original = chain.request()

        // don't need a token when logging in or signing up
        if ((original.url.encodedPath.contains("/login/") && original.method.toLowerCase() == "post") || (original.url.encodedPath.endsWith("/users/") && original.method.toLowerCase() == "post")) {
            return chain.proceed(original)
        }

        val originalHttpUrl = original.url
        val requestBuilder = original.newBuilder().addHeader("Authorization", "JWT " + token).url(originalHttpUrl)

        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}