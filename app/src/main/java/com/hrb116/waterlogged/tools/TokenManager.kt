package com.hrb116.waterlogged.tools

import android.content.Context
import android.util.Log
import com.hrb116.waterlogged.BuildConfig
import kotlinx.coroutines.CancellationException
import java.time.LocalDateTime

private const val CLIENT_ID = BuildConfig.CLIENT_ID
private const val TAG = "TokenManager"

fun getValue(context: Context, key: String): String? {
    val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString(key, null)
}

fun putValue(context: Context, key: String, value: String) {
    val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().putString(key, value).apply()
}

fun isTokenExpired(context: Context): Boolean {
    val expiry = getValue(context, "expires_at") ?: return false
    val expiryTime: LocalDateTime = LocalDateTime.parse(expiry)
    return expiryTime < LocalDateTime.now()
}

suspend fun refreshTokens(context: Context): Boolean {
    try {
        Log.d(TAG, "Refreshing token...")
        val refreshToken = getValue(context, "refresh_token") ?: return false

        val responseJson = doPostRequest(
            url = "https://api.fitbit.com/oauth2/token",
            params = mapOf(
                "client_id" to CLIENT_ID,
                "grant_type" to "refresh_token",
                "refresh_token" to refreshToken
            )
        )

        putValue(context, "access_token", responseJson.getString("access_token"))
        putValue(context, "refresh_token", responseJson.getString("refresh_token"))
        putValue(
            context,
            "expires_at", LocalDateTime.now()
                .plusSeconds(responseJson.getLong("expires_in"))
                .toString()
        )

        return true
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}