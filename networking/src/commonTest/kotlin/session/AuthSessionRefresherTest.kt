package networking.session

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import networking.DevengNetworkingConfig
import networking.DevengNetworkingModule
import networking.session.fake.FakeAuthTokenRenewer

class AuthSessionRefresherTest {

    private val authCredentialsStorage = AuthCredentialsStorageImpl(dataStore = MapSettings())
    private val authTokenRenewer = FakeAuthTokenRenewer()
    private val refreshFailures = mutableListOf<Exception>()
    private val networkingModule = DevengNetworkingModule().apply {
        initDevengNetworkingModule(REST_BASE_URL, DevengNetworkingConfig(loggingEnabled = false, token = STORED_ACCESS_TOKEN))
    }
    private val authSessionRefresher = AuthSessionRefresher(
        authCredentialsStorage = authCredentialsStorage,
        devengNetworkingModule = networkingModule,
        authTokenRenewer = authTokenRenewer,
        onRefreshFailure = { exception -> refreshFailures += exception }
    )

    @Test
    fun refresh_whenSignedOut_returnsFalseWithoutRenewing() = runTest {
        authCredentialsStorage.setRefreshToken(STORED_REFRESH_TOKEN)

        assertFalse(authSessionRefresher.refresh())
        assertTrue(authTokenRenewer.presentedRefreshTokens.isEmpty())
    }

    @Test
    fun refresh_withoutStoredRefreshToken_returnsFalseWithoutRenewing() = runTest {
        authCredentialsStorage.setLoggedIn(true)
        authCredentialsStorage.setRefreshToken("  ")

        assertFalse(authSessionRefresher.refresh())
        assertTrue(authTokenRenewer.presentedRefreshTokens.isEmpty())
    }

    @Test
    fun refresh_withRenewedTokens_storesThemAndPublishesTheAccessToken() = runTest {
        signIn()

        assertTrue(authSessionRefresher.refresh())

        assertEquals(listOf(STORED_REFRESH_TOKEN), authTokenRenewer.presentedRefreshTokens)
        assertEquals(RENEWED_ACCESS_TOKEN, authCredentialsStorage.getAuthenticationToken())
        assertEquals(RENEWED_REFRESH_TOKEN, authCredentialsStorage.getRefreshToken())
        assertEquals(RENEWED_ACCESS_TOKEN, networkingModule.getToken())
    }

    @Test
    fun refresh_withResponseWithoutRefreshToken_keepsTheStoredRefreshToken() = runTest {
        signIn()
        authTokenRenewer.renewedAuthTokens = AuthTokens(accessToken = RENEWED_ACCESS_TOKEN, refreshToken = null)

        assertTrue(authSessionRefresher.refresh())

        assertEquals(RENEWED_ACCESS_TOKEN, authCredentialsStorage.getAuthenticationToken())
        assertEquals(STORED_REFRESH_TOKEN, authCredentialsStorage.getRefreshToken())
    }

    @Test
    fun refresh_whenRenewalFails_reportsTheFailureAndKeepsTheStoredTokens() = runTest {
        signIn()
        val renewalFailure = IllegalStateException("refresh endpoint failed")
        authTokenRenewer.renewFailure = renewalFailure

        assertFalse(authSessionRefresher.refresh())

        assertSame(renewalFailure, refreshFailures.single())
        assertEquals(STORED_ACCESS_TOKEN, authCredentialsStorage.getAuthenticationToken())
        assertEquals(STORED_REFRESH_TOKEN, authCredentialsStorage.getRefreshToken())
        assertEquals(STORED_ACCESS_TOKEN, networkingModule.getToken())
    }

    @Test
    fun refresh_whenAnotherSessionStartsDuringRenewal_doesNotWriteTheOldSessionsTokens() = runTest {
        signIn()
        val responseGate = CompletableDeferred<Unit>()
        authTokenRenewer.responseGate = responseGate

        val refreshResult = async { authSessionRefresher.refresh() }
        authTokenRenewer.awaitFirstCall()
        SessionGeneration.advance()
        authCredentialsStorage.setAuthenticationToken(NEW_SESSION_ACCESS_TOKEN)
        authCredentialsStorage.setRefreshToken(NEW_SESSION_REFRESH_TOKEN)
        networkingModule.setToken(NEW_SESSION_ACCESS_TOKEN)
        responseGate.complete(Unit)

        assertFalse(refreshResult.await())
        assertEquals(NEW_SESSION_ACCESS_TOKEN, authCredentialsStorage.getAuthenticationToken())
        assertEquals(NEW_SESSION_REFRESH_TOKEN, authCredentialsStorage.getRefreshToken())
        assertEquals(NEW_SESSION_ACCESS_TOKEN, networkingModule.getToken())
    }

    private suspend fun signIn() {
        authCredentialsStorage.setLoggedIn(true)
        authCredentialsStorage.setAuthenticationToken(STORED_ACCESS_TOKEN)
        authCredentialsStorage.setRefreshToken(STORED_REFRESH_TOKEN)
        authTokenRenewer.renewedAuthTokens = AuthTokens(
            accessToken = RENEWED_ACCESS_TOKEN,
            refreshToken = RENEWED_REFRESH_TOKEN
        )
    }

    private companion object {
        const val REST_BASE_URL = "https://example.test/"
        const val STORED_ACCESS_TOKEN = "stored-access-token"
        const val STORED_REFRESH_TOKEN = "stored-refresh-token"
        const val RENEWED_ACCESS_TOKEN = "renewed-access-token"
        const val RENEWED_REFRESH_TOKEN = "renewed-refresh-token"
        const val NEW_SESSION_ACCESS_TOKEN = "new-session-access-token"
        const val NEW_SESSION_REFRESH_TOKEN = "new-session-refresh-token"
    }
}
