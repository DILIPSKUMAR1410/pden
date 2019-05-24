package com.dk.pden.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.DisplayMetrics
import android.util.Log
import org.blockstack.android.sdk.Scope
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object Utils {

    private lateinit var preferencesHelper: PreferencesHelper

    val config = java.net.URI("https://app.pden.xyz").run {
        org.blockstack.android.sdk.BlockstackConfig(
                this,
                java.net.URI("${this}/redirect/"),
                java.net.URI("${this}/manifest.json"),
                kotlin.arrayOf(Scope.StoreWrite, Scope.PublishData))
    }

    fun dpToPx(context: Context, dp: Int) = Math.round(dp *
            (context.resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))

    fun getBitmapFromURL(strURL: String): Bitmap? = try {
        val connection = URL(strURL).openConnection()
        connection.connect()
        BitmapFactory.decodeStream(connection.inputStream)
    } catch (e: IOException) {
        Log.e(e.message, "Utils error")
        null
    }

    fun formatDate(timeStamp: Long): String? {
        val c = Calendar.getInstance()
        val c2 = Calendar.getInstance()
        c2.timeInMillis = timeStamp

        val diff = c.timeInMillis - timeStamp
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
        if (seconds > 60) {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            if (minutes > 60) {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                if (hours > 24) {
                    if (c.get(Calendar.YEAR) == c2.get(Calendar.YEAR))
                        return SimpleDateFormat("MMM dd", Locale.getDefault()).format(c2.time)
                    else
                        return SimpleDateFormat("MMM dd yyyy", Locale.getDefault()).format(c2.time)
                } else
                    return "${hours}h"
            } else
                return "${minutes}m"
        } else
            return "${seconds}s"
    }

    fun inputStreamFromUri(context: Context, uri: Uri): InputStream = context.contentResolver.openInputStream(uri)


    fun checkPostBalance(context: Context): Int {
        preferencesHelper = PreferencesHelper(context)
        var status = 0
        if (0 < preferencesHelper.freePromoPost) {
            status = 1
        } else if (7 < preferencesHelper.inkBal) {
            status = 2
        }
        return status
    }

    fun checkLoveBalance(context: Context): Int {
        preferencesHelper = PreferencesHelper(context)
        var status = 0
        if (0 < preferencesHelper.freePromoLove) {
            status = 1
        } else if (4 < preferencesHelper.inkBal) {
            status = 2
        }
        return status
    }
}

