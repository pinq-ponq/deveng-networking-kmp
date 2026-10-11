package networking.session

public fun interface DevengAuthTokenRenewer {
    public suspend fun renewAuthTokens(refreshToken: String): AuthTokens
}
