package com.example.waterlogged.presentation.oauth

// Based on the samples available at
// https://github.com/android/wear-os-samples/tree/main/WearOAuth

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.example.waterlogged.BuildConfig
import com.example.waterlogged.R
import com.example.waterlogged.tools.doPostRequest
import com.example.waterlogged.tools.doGetRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "AuthDeviceGrantViewModel"

private const val CLIENT_ID = BuildConfig.CLIENT_ID
private const val CLIENT_SECRET = BuildConfig.CLIENT_SECRET

data class DeviceGrantState(
    val statusCode: Int = R.string.oauth_device_authorization_default,
    val resultMessage: String = ""
)

/**
 *
 * The viewModel that implements the OAuth flow. The method [startAuthFlow] implements the
 * different steps of the flow. It first retrieves the URL that should be opened on the paired
 * device, then polls for the access token, and uses it to retrieve the user's name.
 */
class AuthDeviceGrantViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val _uiState = MutableStateFlow(DeviceGrantState())

    val uiState: StateFlow<DeviceGrantState> = _uiState.asStateFlow()

    private fun showStatus(statusString: Int = 0, resultString: String = "") {
        _uiState.update {
            DeviceGrantState(statusString, resultString)
        }
    }

    fun startAuthFlow() {
        viewModelScope.launch {
            // Step 1: Retrieve the verification URI
            showStatus(statusString = R.string.status_switch_to_phone)
            val verificationInfo = retrieveVerificationInfo().getOrElse {
                showStatus(statusString = R.string.status_failed)
                return@launch
            }

            // Step 2: Show the pairing code & open the verification URI on the paired device
            showStatus(R.string.status_code, verificationInfo.userCode)
            fireRemoteIntent(context, verificationInfo.verificationUri)

            // Step 3: Poll the Auth server for the token
            val token = retrieveToken(verificationInfo.deviceCode, verificationInfo.interval)

            // Step 4: Use the token to make an authorized request
            val userName = retrieveUserProfile(token).getOrElse {
                showStatus(R.string.status_failed)
                return@launch
            }

            showStatus(R.string.status_retrieved, userName)
        }
    }

    // The response data when retrieving the verification
    data class VerificationInfo(
        val verificationUri: String,
        val userCode: String,
        val deviceCode: String,
        val interval: Int
    )

    /**
     * Retrieve the information needed to verify the user. When performing this request, the server
     * generates a user & device code pair. The user code is shown to the user and opened on the
     * paired device. The device code is passed while polling the OAuth server.
     */
    private suspend fun retrieveVerificationInfo(): Result<VerificationInfo> {
        return try {
            Log.d(TAG, "Retrieving verification info...")
            val responseJson = doPostRequest(
                url = "https://oauth2.googleapis.com/device/code",
                params = mapOf(
                    "client_id" to CLIENT_ID,
                    "scope" to "https://www.googleapis.com/auth/userinfo.profile"
                )
            )
            Result.success(
                VerificationInfo(
                    verificationUri = responseJson.getString("verification_url"),
                    userCode = responseJson.getString("user_code"),
                    deviceCode = responseJson.getString("device_code"),
                    interval = responseJson.getInt("interval")
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Opens the verification URL on the paired device.
     *
     * When the user has the corresponding app installed on their paired Android device, the Data
     * Layer can be used instead, see https://developer.android.com/training/wearables/data-layer.
     *
     * When the user has the corresponding app installed on their paired iOS device, it should
     * use [Universal Links](https://developer.apple.com/ios/universal-links/) to intercept the
     * intent.
     */
    private fun fireRemoteIntent(context: Context, verificationUri: String) {
        RemoteActivityHelper(context).startRemoteActivity(
            Intent(Intent.ACTION_VIEW).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
                data = Uri.parse(verificationUri)
            },
            null
        )
    }

    /**
     * Poll the Auth server for the token. This will only return when the user has finished their
     * authorization flow on the paired device.
     *
     * For this sample the various exceptions aren't handled.
     */
    private tailrec suspend fun retrieveToken(deviceCode: String, interval: Int): String {
        Log.d(TAG, "Polling for token...")
        return fetchToken(deviceCode).getOrElse {
            Log.d(TAG, "No token yet. Waiting...")
            delay(interval * 1000L)
            return retrieveToken(deviceCode, interval)
        }
    }

    private suspend fun fetchToken(deviceCode: String): Result<String> {
        return try {
            val responseJson = doPostRequest(
                url = "https://oauth2.googleapis.com/token",
                params = mapOf(
                    "client_id" to CLIENT_ID,
                    "client_secret" to CLIENT_SECRET,
                    "device_code" to deviceCode,
                    "grant_type" to "urn:ietf:params:oauth:grant-type:device_code"
                )
            )

            Result.success(responseJson.getString("access_token"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Using the access token, make an authorized request to the Auth server to retrieve the user's
     * profile.
     */
    private suspend fun retrieveUserProfile(token: String): Result<String> {
        return try {
            val responseJson = doGetRequest(
                url = "https://www.googleapis.com/oauth2/v2/userinfo",
                requestHeaders = mapOf(
                    "Authorization" to "Bearer $token"
                )
            )
            Result.success(responseJson.getString("name"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}