package networking.session.fake

import networking.session.DevengRefreshTokenRevoker

internal class FakeRefreshTokenRevoker : DevengRefreshTokenRevoker {
    var revokeFailure: Exception? = null
    val revokedRefreshTokens = mutableListOf<String>()

    override suspend fun revokeRefreshToken(refreshToken: String) {
        revokedRefreshTokens += refreshToken
        revokeFailure?.let { failure -> throw failure }
    }
}
