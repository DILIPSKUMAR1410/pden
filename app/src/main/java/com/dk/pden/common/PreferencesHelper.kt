package com.dk.pden.common

import android.content.Context
import android.preference.PreferenceManager

class PreferencesHelper(context: Context) {
    companion object {
        private val BLOCKSTACK_ID = "data.source.prefs.BLOCKSTACK_ID"
        private val EMAIL_ID = "data.source.prefs.EMAIL_ID"
    }

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    // save device token

    var blockstackId = preferences.getString(BLOCKSTACK_ID, "")
        set(value) = preferences.edit().putString(BLOCKSTACK_ID, value).apply()
}