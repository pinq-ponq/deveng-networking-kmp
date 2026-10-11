package networking.session

public data class AuthTokens(
    val accessToken: String,
    val refreshToken: String?
)
