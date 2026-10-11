package networking.session.fake

import kotlinx.coroutines.CompletableDeferred
import networking.session.AuthTokens
import networking.session.DevengAuthTokenRenewer

internal class FakeAuthTokenRenewer : DevengAuthTokenRenewer {
    var renewedAuthTokens = AuthTokens(accessToken = "renewed-access-token", refreshToken = "renewed-refresh-token")
    var renewFailure: Exception? = null
    var responseGate: CompletableDeferred<Unit>? = null
    val presentedRefreshTokens = mutableListOf<String>()

    private val firstCallStarted = CompletableDeferred<Unit>()

    override suspend fun renewAuthTokens(refreshToken: String): AuthTokens {
        presentedRefreshTokens += refreshToken
        firstCallStarted.complete(Unit)
        responseGate?.await()
        renewFailure?.let { failure -> throw failure }
        return renewedAuthTokens
    }

    suspend fun awaitFirstCall() {
        firstCallStarted.await()
    }
}
