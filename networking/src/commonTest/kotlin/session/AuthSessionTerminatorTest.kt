package networking.session

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import networking.DevengNetworkingConfig
import networking.DevengNetworkingModule
import networking.session.fake.FakeRefreshTokenRevoker

class AuthSessionTerminatorTest {

    private val authCredentialsStorage = AuthCredentialsStorageImpl(dataStore = MapSettings())
    private val refreshTokenRevoker = FakeRefreshTokenRevoker()
    private val revokeFailures = mutableListOf<Throwable>()
    private val networkingModule = DevengNetworkingModule().apply {
        initDevengNetworkingModule(REST_BASE_URL, DevengNetworkingConfig(loggingEnabled = false, token = STORED_ACCESS_TOKEN))
    }
    private val authSessionTerminator = AuthSessionTerminator(
        authCredentialsStorage = authCredentialsStorage,
        devengNetworkingModule = networkingModule,
        refreshTokenRevoker = refreshTokenRevoker,
        onRevokeFailure = { throwable -> revokeFailures += throwable }
    )

    @Test
    fun endSession_whileSignedIn_revokesTheRefreshTokenAndClearsTheSession() = runTest {
        signIn()

        authSessionTerminator.endSession()

        assertEquals(listOf(STORED_REFRESH_TOKEN), refreshTokenRevoker.revokedRefreshTokens)
        assertFalse(authCredentialsStorage.isUserLoggedIn())
        assertFalse(authCredentialsStorage.isLoggedIn.value)
        assertEquals("", authCredentialsStorage.getAuthenticationToken())
        assertNull(authCredentialsStorage.getRefreshToken())
        assertEquals("", networkingModule.getToken())
        assertTrue(revokeFailures.isEmpty())
    }

    @Test
    fun endSession_runsTheAuthenticatedCleanupSignedOutButBeforeTheTokensAreDropped() = runTest {
        signIn()
        var isLoggedInDuringCleanup: Boolean? = null
        var networkingTokenDuringCleanup: String? = null
        var revokedTokensDuringCleanup: List<String>? = null

        authSessionTerminator.endSession(
            authenticatedCleanup = {
                isLoggedInDuringCleanup = authCredentialsStorage.isUserLoggedIn()
                networkingTokenDuringCleanup = networkingModule.getToken()
                revokedTokensDuringCleanup = refreshTokenRevoker.revokedRefreshTokens.toList()
            }
        )

        assertEquals(false, isLoggedInDuringCleanup)
        assertEquals(STORED_ACCESS_TOKEN, networkingTokenDuringCleanup)
        assertEquals(emptyList(), revokedTokensDuringCleanup)
    }

    @Test
    fun endSession_whenRevokeFails_reportsTheFailureAndStillClearsTheSession() = runTest {
        signIn()
        val revokeFailure = IllegalStateException("revoke endpoint failed")
        refreshTokenRevoker.revokeFailure = revokeFailure

        authSessionTerminator.endSession()

        assertSame(revokeFailure, revokeFailures.single())
        assertEquals("", authCredentialsStorage.getAuthenticationToken())
        assertNull(authCredentialsStorage.getRefreshToken())
        assertEquals("", networkingModule.getToken())
    }

    @Test
    fun endSession_withoutRefreshToken_skipsTheRevoke() = runTest {
        authCredentialsStorage.setLoggedIn(true)
        authCredentialsStorage.setRefreshToken(" ")

        authSessionTerminator.endSession()

        assertTrue(refreshTokenRevoker.revokedRefreshTokens.isEmpty())
        assertFalse(authCredentialsStorage.isUserLoggedIn())
    }

    @Test
    fun endSession_advancesTheSessionGeneration() = runTest {
        signIn()
        val generationBeforeSignOut = SessionGeneration.currentValue()

        authSessionTerminator.endSession()

        assertEquals(generationBeforeSignOut + 1, SessionGeneration.currentValue())
    }

    private suspend fun signIn() {
        authCredentialsStorage.setLoggedIn(true)
        authCredentialsStorage.setAuthenticationToken(STORED_ACCESS_TOKEN)
        authCredentialsStorage.setRefreshToken(STORED_REFRESH_TOKEN)
    }

    private companion object {
        const val REST_BASE_URL = "https://example.test/"
        const val STORED_ACCESS_TOKEN = "stored-access-token"
        const val STORED_REFRESH_TOKEN = "stored-refresh-token"
    }
}
