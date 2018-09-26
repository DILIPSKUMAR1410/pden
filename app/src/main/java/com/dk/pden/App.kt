package com.dk.pden

import android.app.Application
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric


class App : Application() {

    companion object Constants {
        const val TAG = "ObjectBox"
    }

    override fun onCreate() {
        super.onCreate()
        ObjectBox.build(this)
        Fabric.with(this, Crashlytics())

    }

}