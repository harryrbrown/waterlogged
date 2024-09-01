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
import com.hrb116.waterlogged.tools.doGetRequest
import com.hrb116.waterlogged.tools.doPostRequest
import com.hrb116.waterlogged.tools.putValue
import com.hrb116.waterlogged.tools.saveWaterUnit
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
    fun startAuthFlow() {
        viewModelScope.launch {
            val codeVerifier = CodeVerifier()

            // We have to build this Uri, replace the encoded '+' characters, and then parse the
            // Uri again, otherwise the wrong webpage will open
            val uri = Uri.Builder()
                .encodedPath("https://www.fitbit.com/oauth2/authorize")
                .appendQueryParameter("scope", "nutrition+profile")
                .build()
                .toString()
                .replace("%2B", "+")
            val finalUri = Uri.parse(uri)
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
                url = "https://api.fitbit.com/oauth2/token",
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
                responseJson.getString("access_token"),
                responseJson.getString("refresh_token"),
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
     * Using the access token, make an authorized request to the Auth server to retrieve the user's
     * profile.
     */
    suspend fun retrieveUserProfile(token: String): Result<String> {
        return try {
            val responseJson = doGetRequest(
                url = "https://api.fitbit.com/1/user/-/profile.json",
                requestHeaders = mapOf(
                    "Authorization" to "Bearer $token"
                )
            )
            saveWaterUnitFromProfile(responseJson.getJSONObject("user").getString("waterUnitName"))
            Result.success(responseJson.getJSONObject("user").getString("displayName"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun writeTokensToKeystore(tokens: Tokens) {
        putValue(context, "access_token", tokens.accessToken)
        putValue(context, "refresh_token", tokens.refreshToken)
        putValue(context, "expires_at", tokens.expiresAt.toString())
    }

    private fun saveWaterUnitFromProfile(unit: String) {
        saveWaterUnit(context, unit)
    }
}