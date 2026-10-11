package networking.session

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class AuthCredentialsStorageImplTest {

    private val dataStore = MapSettings()

    @Test
    fun readCredentials_writtenUnderTheExistingKeys_returnsThem() {
        dataStore.putBoolean(IS_LOGGED_IN_KEY, true)
        dataStore.putString(TOKEN_KEY, ACCESS_TOKEN)
        dataStore.putString(REFRESH_TOKEN_KEY, REFRESH_TOKEN)

        val authCredentialsStorage = AuthCredentialsStorageImpl(dataStore = dataStore)

        assertTrue(authCredentialsStorage.isUserLoggedIn())
        assertTrue(authCredentialsStorage.isLoggedIn.value)
        assertEquals(ACCESS_TOKEN, authCredentialsStorage.getAuthenticationToken())
        assertEquals(REFRESH_TOKEN, authCredentialsStorage.getRefreshToken())
    }

    @Test
    fun readCredentials_onAnEmptyStore_returnsSignedOutDefaults() {
        val authCredentialsStorage = AuthCredentialsStorageImpl(dataStore = dataStore)

        assertFalse(authCredentialsStorage.isUserLoggedIn())
        assertEquals("", authCredentialsStorage.getAuthenticationToken())
        assertNull(authCredentialsStorage.getRefreshToken())
    }

    @Test
    fun setLoggedIn_updatesTheStoreAndTheFlow() = runTest {
        val authCredentialsStorage = AuthCredentialsStorageImpl(dataStore = dataStore)

        authCredentialsStorage.setLoggedIn(true)

        assertTrue(dataStore.getBoolean(IS_LOGGED_IN_KEY, defaultValue = false))
        assertTrue(authCredentialsStorage.isLoggedIn.value)
    }

    @Test
    fun setTokens_writesThemUnderTheExistingKeys() = runTest {
        val authCredentialsStorage = AuthCredentialsStorageImpl(dataStore = dataStore)

        authCredentialsStorage.setAuthenticationToken(ACCESS_TOKEN)
        authCredentialsStorage.setRefreshToken(REFRESH_TOKEN)

        assertEquals(ACCESS_TOKEN, dataStore.getStringOrNull(TOKEN_KEY))
        assertEquals(REFRESH_TOKEN, dataStore.getStringOrNull(REFRESH_TOKEN_KEY))
    }

    @Test
    fun setRefreshToken_withNull_removesTheStoredToken() = runTest {
        val authCredentialsStorage = AuthCredentialsStorageImpl(dataStore = dataStore)
        authCredentialsStorage.setRefreshToken(REFRESH_TOKEN)

        authCredentialsStorage.setRefreshToken(null)

        assertFalse(dataStore.hasKey(REFRESH_TOKEN_KEY))
    }

    @Test
    fun clearAuthCredentials_removesEveryCredential() = runTest {
        val authCredentialsStorage = AuthCredentialsStorageImpl(dataStore = dataStore)
        authCredentialsStorage.setLoggedIn(true)
        authCredentialsStorage.setAuthenticationToken(ACCESS_TOKEN)
        authCredentialsStorage.setRefreshToken(REFRESH_TOKEN)

        authCredentialsStorage.clearAuthCredentials()

        assertFalse(authCredentialsStorage.isUserLoggedIn())
        assertEquals("", authCredentialsStorage.getAuthenticationToken())
        assertNull(authCredentialsStorage.getRefreshToken())
    }

    private companion object {
        const val TOKEN_KEY = "TOKEN"
        const val REFRESH_TOKEN_KEY = "REFRESH_TOKEN"
        const val IS_LOGGED_IN_KEY = "IS_LOGGED_IN"
        const val ACCESS_TOKEN = "access-token"
        const val REFRESH_TOKEN = "refresh-token"
    }
}
