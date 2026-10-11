package networking.session

import networking.DevengNetworkingModule

public class AuthSessionTerminator(
    private val authCredentialsStorage: AuthCredentialsStorage,
    private val devengNetworkingModule: DevengNetworkingModule,
    private val refreshTokenRevoker: DevengRefreshTokenRevoker,
    private val onRevokeFailure: (Throwable) -> Unit
) {
    /**
     * [authenticatedCleanup] runs after the session is marked signed out but before its tokens are
     * dropped, for sign-out requests that still need them (unregistering a push token, for example).
     */
    public suspend fun endSession(authenticatedCleanup: suspend () -> Unit = {}) {
        val refreshToken = authCredentialsStorage.getRefreshToken()

        SessionGeneration.advance()
        authCredentialsStorage.setLoggedIn(false)

        authenticatedCleanup()
        revokeRefreshToken(refreshToken)

        devengNetworkingModule.setToken("")
        authCredentialsStorage.clearAuthCredentials()
    }

    private suspend fun revokeRefreshToken(refreshToken: String?) {
        if (refreshToken.isNullOrBlank()) return

        runCatching { refreshTokenRevoker.revokeRefreshToken(refreshToken) }
            .onFailure(onRevokeFailure)
    }
}
