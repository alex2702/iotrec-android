package de.ikas.iotrec.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response
import android.util.Log

class ResponseInterceptor constructor(val context: Context) : Interceptor {

    private val TAG = "ResponseInterceptor"

    // do different things for different response codes (nothing implemented so far)
    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            val request = chain.request()
            val response = chain.proceed(request)

            return response
        } catch(e: Exception) {
            Log.d(TAG, e.toString())
        }

        return chain.proceed(chain.request())
    }
}