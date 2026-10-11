package networking.session

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

public class AuthCredentialsStorageImpl(
    private val dataStore: Settings
) : AuthCredentialsStorage {

    private val _isLoggedIn = MutableStateFlow(
        isUserLoggedIn()
    )

    override val isLoggedIn: StateFlow<Boolean> get() = _isLoggedIn

    override fun isUserLoggedIn(): Boolean {
        return dataStore[PreferencesKeys.IS_LOGGED_IN.key] ?: false
    }

    override suspend fun setLoggedIn(isLoggedIn: Boolean) {
        dataStore[PreferencesKeys.IS_LOGGED_IN.key] = isLoggedIn
        _isLoggedIn.update { isLoggedIn }
    }

    override fun getAuthenticationToken(): String {
        return dataStore[PreferencesKeys.TOKEN.key] ?: ""
    }

    override suspend fun setAuthenticationToken(token: String) {
        dataStore[PreferencesKeys.TOKEN.key] = token
    }

    override fun getRefreshToken(): String? {
        return dataStore[PreferencesKeys.REFRESH_TOKEN.key]
    }

    override suspend fun setRefreshToken(refreshToken: String?) {
        if (refreshToken == null) {
            dataStore.remove(PreferencesKeys.REFRESH_TOKEN.key)
        } else {
            dataStore[PreferencesKeys.REFRESH_TOKEN.key] = refreshToken
        }
    }

    override suspend fun clearAuthCredentials() {
        dataStore.clear()
    }

    // The key names are what apps already have on disk; renaming one signs every user out.
    internal enum class PreferencesKeys {
        TOKEN,
        REFRESH_TOKEN,
        IS_LOGGED_IN;

        val key get() = this.name
    }
}
