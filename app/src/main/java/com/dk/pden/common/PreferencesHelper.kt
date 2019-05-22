package com.dk.pden.common

import android.content.Context
import android.preference.PreferenceManager

class PreferencesHelper(context: Context) {
    companion object {
        private val BLOCKSTACK_ID = "data.source.prefs.BLOCKSTACK_ID"
        private val ink_bal = "data.source.prefs.ink_bal"
        private val free_promo_post = "data.source.prefs.free_promo_post"
        private val free_promo_love = "data.source.prefs.free_promo_love"
        private val free_promo_spread = "data.source.prefs.free_promo_spread"
        private val free_promo_comment = "data.source.prefs.free_promo_comment"

    }

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    // save device token

    var blockstackId = preferences.getString(BLOCKSTACK_ID, "")
        set(value) = preferences.edit().putString(BLOCKSTACK_ID, value).apply()
    var inkBal = preferences.getLong(ink_bal, 0)
        set(value) = preferences.edit().putLong(ink_bal, value).apply()
    var freePromoPost = preferences.getLong(free_promo_post, 0)
        set(value) = preferences.edit().putLong(free_promo_post, value).apply()
    var freePromoLove = preferences.getLong(free_promo_love, 0)
        set(value) = preferences.edit().putLong(free_promo_love, value).apply()
    var freePromoSpread = preferences.getLong(free_promo_spread, 0)
        set(value) = preferences.edit().putLong(free_promo_spread, value).apply()
    var freePromoComment = preferences.getLong(free_promo_comment, 0)
        set(value) = preferences.edit().putLong(free_promo_comment, value).apply()
}