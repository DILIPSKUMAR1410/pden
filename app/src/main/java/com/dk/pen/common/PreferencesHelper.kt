package com.dk.pen.common

import android.content.Context
import android.preference.PreferenceManager

class PreferencesHelper(context: Context) {
    companion object {
        val DEVELOP_MODE = false
        private val DEVICE_TOKEN = "data.source.prefs.DEVICE_TOKEN"
    }

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    // save device token

    var deviceToken = preferences.getString(DEVICE_TOKEN, "")
        set(value) = preferences.edit().putString(DEVICE_TOKEN, value).apply()
}