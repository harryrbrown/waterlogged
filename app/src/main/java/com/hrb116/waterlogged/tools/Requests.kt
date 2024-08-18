@file:Suppress("BlockingMethodInNonBlockingContext")

package com.hrb116.waterlogged.tools

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

/**
 * Simple implementation of a POST request. Normally you'd use a library to do these requests.
 */
suspend fun doPostRequest(
    url: String,
    params: Map<String, String>,
    requestHeaders: Map<String, String>? = null,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): JSONObject {
    return withContext(dispatcher) {
        val conn = (URL(url).openConnection() as HttpURLConnection)
        val postData = StringBuilder()
        for ((key, value) in params) {
            if (postData.isNotEmpty()) postData.append('&')
            postData.append(URLEncoder.encode(key, "UTF-8"))
            postData.append('=')
            postData.append(URLEncoder.encode(value, "UTF-8"))
        }
        val postDataBytes = postData.toString().toByteArray(charset("UTF-8"))

        requestHeaders?.onEach { (key, value) ->
            conn.setRequestProperty(key, value)
        }

        conn.apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            setRequestProperty("Content-Length", postDataBytes.size.toString())
            doOutput = true
            outputStream.write(postDataBytes)
        }

        val inputReader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
        val response = inputReader.readText()

        Log.d("PostRequestUtil", "Response: $response")

        JSONObject(response)
    }
}

/**
 * Simple implementation of a GET request. Normally you'd use a library to do these requests.
 */
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