package networking.session

import networking.DevengNetworkingModule

public class AuthSessionRefresher(
    private val authCredentialsStorage: AuthCredentialsStorage,
    private val devengNetworkingModule: DevengNetworkingModule,
    private val authTokenRenewer: DevengAuthTokenRenewer,
    private val onRefreshFailure: (Exception) -> Unit
) : DevengSessionRefresher {

    override suspend fun refresh(): Boolean {
        if (!authCredentialsStorage.isUserLoggedIn()) {
            return false
        }

        val storedRefreshToken = authCredentialsStorage.getRefreshToken()
        if (storedRefreshToken.isNullOrBlank()) {
            return false
        }

        val sessionGenerationAtStart = SessionGeneration.currentValue()

        val renewedTokens = try {
            authTokenRenewer.renewAuthTokens(storedRefreshToken)
        } catch (exception: Exception) {
            onRefreshFailure(exception)
            return false
        }

        return storeRenewedTokens(renewedTokens, sessionGenerationAtStart)
    }

    private suspend fun storeRenewedTokens(renewedTokens: AuthTokens, sessionGenerationAtStart: Int): Boolean =
        SessionGeneration.persistIfSessionUnchanged(sessionGenerationAtStart) {
            authCredentialsStorage.setAuthenticationToken(renewedTokens.accessToken)
            // Every rotation returns a new refresh token; a response without one must leave the
            // stored token alone, because clearing it would end the session on the next renewal.
            renewedTokens.refreshToken?.let { rotatedRefreshToken ->
                authCredentialsStorage.setRefreshToken(rotatedRefreshToken)
            }
            devengNetworkingModule.setToken(renewedTokens.accessToken)
        }
}
