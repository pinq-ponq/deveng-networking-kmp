package networking.session

public fun interface DevengRefreshTokenRevoker {
    public suspend fun revokeRefreshToken(refreshToken: String)
}
