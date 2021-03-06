package com.dk.pden.common

import android.content.Context
import android.preference.PreferenceManager

class PreferencesHelper(context: Context) {
    companion object {
        private val BLOCKSTACK_ID = "data.source.prefs.BLOCKSTACK_ID"
        private val ISINITCOMPLETED = "data.source.prefs.ISINITCOMPLETED"
    }

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    // save device token

    var blockstackId = preferences.getString(BLOCKSTACK_ID, "")
        set(value) = preferences.edit().putString(BLOCKSTACK_ID, value).apply()
    var isInitCompleted = preferences.getBoolean(ISINITCOMPLETED, false)
        set(value) = preferences.edit().putBoolean(ISINITCOMPLETED, value).apply()

}