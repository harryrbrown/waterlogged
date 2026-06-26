package com.hrb116.waterlogged.presentation.oauth.pkce

// Based on the samples in https://github.com/android/wear-os-samples/tree/main/WearOAuth

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.phone.interactions.authentication.CodeChallenge
import androidx.wear.phone.interactions.authentication.CodeVerifier
import androidx.wear.phone.interactions.authentication.OAuthRequest
import androidx.wear.phone.interactions.authentication.OAuthResponse
import androidx.wear.phone.interactions.authentication.RemoteAuthClient
import androidx.wear.tiles.TileService
import com.hrb116.waterlogged.BuildConfig
import com.hrb116.waterlogged.R
import com.hrb116.waterlogged.tile.MainTileService
import com.hrb116.waterlogged.common.networking.doGetRequest
import com.hrb116.waterlogged.common.networking.doPostRequest
import com.hrb116.waterlogged.common.preferences.saveUserName
import com.hrb116.waterlogged.common.tokens.putValue
import com.hrb116.waterlogged.common.preferences.saveWaterUnit
import com.hrb116.waterlogged.common.tokens.Tokens as WaterloggedTokens
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

private const val TAG = "WearOAuthViewModel"

private const val CLIENT_ID = BuildConfig.CLIENT_ID
private const val CLIENT_SECRET = BuildConfig.CLIENT_SECRET
private const val GOOGLE_HEALTH_SETTINGS_URL = "https://health.googleapis.com/v4/users/me/settings"
private const val GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo"

data class ProofKeyCodeExchangeState(
    // Status to show on the Wear OS display
    val statusCode: Int = R.string.start_auth_flow,
    // Dynamic content to show on the Wear OS display
    val resultMessage: String = ""
)

data class Tokens(
    val accessToken: String = "",
    val refreshToken: String = "",
    var expiresAt: LocalDateTime
)

/**
 * The viewModel that implements the OAuth flow. The method [startAuthFlow] implements the
 * different steps of the flow. It first retrieves the OAuth code, uses it to exchange it for an
 * access token, and uses the token to retrieve the user's name.
 */
class AuthPKCEViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val _uiState = MutableStateFlow(ProofKeyCodeExchangeState())
    val uiState: StateFlow<ProofKeyCodeExchangeState> = _uiState.asStateFlow()

    private fun showStatus(statusCode: Int = R.string.start_auth_flow, resultString: String = "") {
        _uiState.value =
            _uiState.value.copy(statusCode = statusCode, resultMessage = resultString)
    }

    /**
     * Start the authentication flow and do an authenticated request. This method implements
     * the steps described at
     * https://d.google.com/identity/protocols/oauth2/native-app#obtainingaccesstokens
     *
     * The [androidx.wear.phone.interactions.authentication] package helps with this implementation.
     * It can generate a code verifier and challenge, and helps to move the consent step to
     * the phone. After the user consents on their phone, the wearable app is notified and can
     * continue the authorization process.
     */
    fun startAuthFlow(onCompletion: () -> Unit) {
        viewModelScope.launch {
            val codeVerifier = CodeVerifier()

            val scopes = listOf(
                "https://www.googleapis.com/auth/googlehealth.nutrition.writeonly",
                "https://www.googleapis.com/auth/googlehealth.nutrition.readonly",
                "https://www.googleapis.com/auth/googlehealth.settings.readonly",
                "https://www.googleapis.com/auth/userinfo.profile"
            ).joinToString(" ")
            val finalUri = Uri.Builder()
                .scheme("https")
                .authority("accounts.google.com")
                .path("/o/oauth2/v2/auth")
                .appendQueryParameter("scope", scopes)
                .appendQueryParameter("access_type", "offline")
                .appendQueryParameter("prompt", "consent")
                .build()
            val oauthRequest = OAuthRequest.Builder(context)
                .setAuthProviderUrl(finalUri)
                .setCodeChallenge(CodeChallenge(codeVerifier))
                .setClientId(CLIENT_ID)
                .build()

            // Step 1: Retrieve the OAuth code
            showStatus(statusCode = R.string.status_switch_to_phone)
            val code = retrieveOAuthCode(oauthRequest, context).getOrElse {
                showStatus(statusCode = R.string.status_failed)
                return@launch
            }

            // Step 2: Retrieve the access token
            showStatus(R.string.status_retrieving_token)
            val tokens = retrieveToken(code, codeVerifier).getOrElse {
                showStatus(R.string.status_failure_token)
                return@launch
            }

            // Step 3: Use token to perform API request
            showStatus(R.string.status_retrieving_user)
            val userName = retrieveUserProfile(tokens.accessToken).getOrElse {
                showStatus(R.string.status_failure_user)
                return@launch
            }

            showStatus(R.string.status_retrieved, userName)
            onCompletion()
        }
    }

    /**
     * Use the [RemoteAuthClient] class to authorize the user. The library will handle the
     * communication with the paired device, where the user can log in.
     */
    private suspend fun retrieveOAuthCode(
        oauthRequest: OAuthRequest,
        context: Context
    ): Result<String> {
        Log.d(TAG, "Authorization requested. Request URL: ${oauthRequest.requestUrl}")

        // Wrap the callback-based request inside a coroutine wrapper
        return suspendCoroutine { c ->
            RemoteAuthClient.create(context).sendAuthorizationRequest(
                request = oauthRequest,
                executor = { command -> command?.run() },
                clientCallback = object : RemoteAuthClient.Callback() {
                    override fun onAuthorizationError(request: OAuthRequest, errorCode: Int) {
                        Log.w(TAG, "Authorization failed with errorCode $errorCode")
                        c.resume(Result.failure(IOException("Authorization failed")))
                    }

                    override fun onAuthorizationResponse(
                        request: OAuthRequest,
                        response: OAuthResponse
                    ) {
                        val responseUrl = response.responseUrl
                        Log.d(TAG, "Authorization success. ResponseUrl: $responseUrl")
                        val code = responseUrl?.getQueryParameter("code")
                        if (code.isNullOrBlank()) {
                            Log.w(
                                TAG,
                                "Google OAuth 2.0 API token exchange failed. " +
                                        "No code query parameter in response URL."
                            )
                            c.resume(Result.failure(IOException("Authorization failed")))
                        } else {
                            c.resume(Result.success(code))
                        }
                    }
                }
            )
        }
    }

    private suspend fun retrieveToken(
        code: String,
        codeVerifier: CodeVerifier
    ): Result<Tokens> {
        return try {
            Log.d(TAG, "Requesting token...")

            val responseJson = doPostRequest(
                url = "https://oauth2.googleapis.com/token",
                params = mapOf(
                    "client_id" to CLIENT_ID,
                    "client_secret" to CLIENT_SECRET,
                    "code" to code,
                    "code_verifier" to codeVerifier.value,
                    "grant_type" to "authorization_code",
                    "redirect_uri" to "https://wear.googleapis.com/3p_auth/com.hrb116.waterlogged"
                )
            )

            val result = Tokens(
                responseJson.getString(WaterloggedTokens.ACCESS_TOKEN.token_name),
                responseJson.optString(WaterloggedTokens.REFRESH_TOKEN.token_name, ""),
                LocalDateTime.now().plusSeconds(responseJson.getLong("expires_in"))
            )

            writeTokensToKeystore(result)
            TileService.getUpdater(context).requestUpdate(MainTileService::class.java)
            Result.success(result)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Using the access token, fetch the user's display name and water unit preference from
     * Google's userinfo and Google Health settings endpoints.
     */
    suspend fun retrieveUserProfile(token: String): Result<String> {
        return try {
            val headers = mapOf("Authorization" to "Bearer $token")

            val userInfoJson = doGetRequest(url = GOOGLE_USERINFO_URL, requestHeaders = headers)
            val displayName = userInfoJson.getString("name")

            val settingsJson = doGetRequest(url = GOOGLE_HEALTH_SETTINGS_URL, requestHeaders = headers)
            val waterUnitEnum = settingsJson.optString("waterUnit", "MILLILITERS")
            val waterUnit = when {
                waterUnitEnum.contains("FLUID_OUNCE") || waterUnitEnum.contains("FLUID_OUNCES") -> "fl oz"
                waterUnitEnum.contains("CUP") || waterUnitEnum.contains("CUPS") -> "cup"
                else -> "ml"
            }

            saveUserInfoProfile(unit = waterUnit, username = displayName)
            Result.success(displayName)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun writeTokensToKeystore(tokens: Tokens) {
        putValue(context, WaterloggedTokens.ACCESS_TOKEN, tokens.accessToken)
        putValue(context, WaterloggedTokens.REFRESH_TOKEN, tokens.refreshToken)
        putValue(context, WaterloggedTokens.EXPIRES_AT, tokens.expiresAt.toString())
    }

    private fun saveUserInfoProfile(unit: String, username: String) {
        saveWaterUnit(context, unit)
        saveUserName(context, username)
    }
}