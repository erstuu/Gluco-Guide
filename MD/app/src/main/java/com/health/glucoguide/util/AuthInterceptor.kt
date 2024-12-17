package com.health.glucoguide.util

import android.content.Context
import android.content.Intent
import com.health.glucoguide.R
import com.health.glucoguide.ui.activity.onboarding.OnBoardingActivity
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val appContext = context.applicationContext

        if (response.code == 403) {
            val intent = Intent(context, OnBoardingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
        }

        if (response.code == 503) {
            throw IOException(appContext.getString(R.string.http_error_default_message))
        }

        return response
    }
}