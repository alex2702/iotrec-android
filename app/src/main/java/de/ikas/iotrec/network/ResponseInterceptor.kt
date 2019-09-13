package de.ikas.iotrec.network

import android.content.Context
import android.preference.PreferenceManager
import okhttp3.Interceptor
import okhttp3.Response
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startActivity



class ResponseInterceptor constructor(val context: Context) : Interceptor {

    private val TAG = "ResponseInterceptor"

    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            val request = chain.request()
            val response = chain.proceed(request)

            /*
            if (response.code == 500) {
                return response
            }
            */

            return response
        } catch(e: Exception) {
            Log.d(TAG, e.toString())
        }

        return chain.proceed(chain.request())
    }
}