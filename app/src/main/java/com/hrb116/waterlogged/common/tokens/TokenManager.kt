package com.hrb116.waterlogged.common.tokens

import android.content.Context
import android.util.Log
import com.hrb116.waterlogged.BuildConfig
import com.hrb116.waterlogged.common.networking.doPostRequest
import kotlinx.coroutines.CancellationException
import java.time.LocalDateTime

private const val CLIENT_ID = BuildConfig.CLIENT_ID
private const val TAG = "TokenManager"

fun getValue(context: Context, key: Tokens): String? {
    val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString(key.token_name, null)
}

fun putValue(context: Context, key: Tokens, value: String) {
    val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().putString(key.token_name, value).apply()
}

fun isTokenExpired(context: Context): Boolean {
    val expiry = getValue(context, Tokens.EXPIRES_AT) ?: return false
    val expiryTime: LocalDateTime = LocalDateTime.parse(expiry)
    return expiryTime < LocalDateTime.now()
}

suspend fun refreshTokens(context: Context): Boolean {
    try {
        Log.d(TAG, "Refreshing token...")
        val refreshToken = getValue(context, Tokens.REFRESH_TOKEN) ?: return false

        val responseJson = doPostRequest(
            url = "https://api.fitbit.com/oauth2/token",
            params = mapOf(
                "client_id" to CLIENT_ID,
                "grant_type" to "refresh_token",
                "refresh_token" to refreshToken
            )
        )

        putValue(context, Tokens.ACCESS_TOKEN, responseJson.getString("access_token"))
        putValue(context, Tokens.REFRESH_TOKEN, responseJson.getString("refresh_token"))
        putValue(
            context,
            Tokens.EXPIRES_AT, LocalDateTime.now()
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