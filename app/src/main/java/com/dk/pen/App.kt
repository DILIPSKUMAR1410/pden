package com.dk.pen

import android.app.Application

class App : Application() {

    companion object Constants {
        const val TAG = "ObjectBox"
    }

    override fun onCreate() {
        super.onCreate()
        ObjectBox.build(this)
    }

}