package com.dk.pden

import android.annotation.SuppressLint
import android.app.Application
import com.crashlytics.android.Crashlytics
import com.dk.pden.common.PreferencesHelper
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.pusher.pushnotifications.PushNotifications
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
        mixpanel.identify(PreferencesHelper(this).blockstackId)
        mixpanel.people.identify(PreferencesHelper(this).blockstackId)
        val name = "name"
        mixpanel.people.set("$$name", PreferencesHelper(this).blockstackId)
        PushNotifications.start(applicationContext, "246e7fe4-7a7b-4da5-9c76-d1cc8d1c4bac")

    }

    override fun onTerminate() {
        mixpanel.flush()
        super.onTerminate()
    }

}