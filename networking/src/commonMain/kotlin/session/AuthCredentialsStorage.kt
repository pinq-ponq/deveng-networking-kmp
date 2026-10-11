package networking.session

import kotlinx.coroutines.flow.StateFlow

public interface AuthCredentialsStorage {
    public val isLoggedIn: StateFlow<Boolean>
    public fun isUserLoggedIn(): Boolean
    public suspend fun setLoggedIn(isLoggedIn: Boolean)

    public fun getAuthenticationToken(): String
    public suspend fun setAuthenticationToken(token: String)

    public fun getRefreshToken(): String?
    public suspend fun setRefreshToken(refreshToken: String?)

    public suspend fun clearAuthCredentials()
}
