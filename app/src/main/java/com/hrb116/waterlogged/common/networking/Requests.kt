@file:Suppress("BlockingMethodInNonBlockingContext")

package com.hrb116.waterlogged.common.networking

// Based on the samples in https://github.com/android/wear-os-samples/tree/main/WearOAuth

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

suspend fun doPostRequest(
    url: String,
    params: Map<String, String>,
    requestHeaders: Map<String, String>? = null,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    sendAsQuery: Boolean = false
): JSONObject {
    return withContext(dispatcher) {
        // Build URL or body based on mode
        val encodedParams = params.entries.joinToString("&") {
            "${URLEncoder.encode(it.key, "UTF-8")}=${URLEncoder.encode(it.value, "UTF-8")}"
        }
        val requestUrl = if (sendAsQuery && encodedParams.isNotEmpty()) {
            if (url.contains("?")) "$url&$encodedParams" else "$url?$encodedParams"
        } else {
            url
        }

        val conn = (URL(requestUrl).openConnection() as HttpURLConnection)

        // Apply headers (auth, etc.)
        requestHeaders?.forEach { (key, value) ->
            conn.setRequestProperty(key, value)
        }

        conn.requestMethod = "POST"
        conn.doInput = true

        if (!sendAsQuery) {
            val postDataBytes = encodedParams.toByteArray(Charsets.UTF_8)
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            conn.setRequestProperty("Content-Length", postDataBytes.size.toString())
            conn.outputStream.use { it.write(postDataBytes) }
        }

        val responseCode = conn.responseCode
        val inputStream = if (responseCode in 200..299) {
            conn.inputStream
        } else {
            conn.errorStream ?: conn.inputStream
        }

        val response = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        Log.d("PostRequestUtil", "HTTP $responseCode: $response")

        JSONObject(response)
    }
}

suspend fun doGetRequest(
    url: String,
    requestHeaders: Map<String, String>,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): JSONObject {
    return withContext(dispatcher) {
        val conn = (URL(url).openConnection() as HttpURLConnection)
        requestHeaders.onEach { (key, value) ->
            conn.setRequestProperty(key, value)
        }
        val inputReader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
        val response = inputReader.readText()

        Log.d("RequestUtil", "Response: $response")

        JSONObject(response)
    }
}