package com.dk.pden

import android.annotation.SuppressLint
import android.app.Application
import com.crashlytics.android.Crashlytics
import com.mixpanel.android.mpmetrics.MixpanelAPI
import io.fabric.sdk.android.Fabric


class App : Application() {

    companion object Constants {
        const val TAG = "ObjectBox"
        val MIXPANEL_TOKEN = "5755beb7f2e098e15d6144fe1530f8f7"
        @SuppressLint("StaticFieldLeak")
        lateinit var mixpanel: MixpanelAPI
    }

    override fun onCreate() {
        super.onCreate()
        ObjectBox.build(this)
        Fabric.with(this, Crashlytics())


        // Initialize the library with your
        // Mixpanel project token, MIXPANEL_TOKEN, and a reference
        // to your application context.

        mixpanel = MixpanelAPI.getInstance(this, MIXPANEL_TOKEN)

    }

    override fun onTerminate() {
        mixpanel.flush()
        super.onTerminate()
    }

}