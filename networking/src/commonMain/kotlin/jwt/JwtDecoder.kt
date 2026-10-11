package networking.jwt

import io.ktor.util.decodeBase64Bytes
import kotlinx.serialization.json.Json

public fun decodeJwtPayload(token: String): String {
    val parts = token.split(".")
    require(parts.size == 3) { "Invalid JWT format" }

    val base64Payload = parts[1]
    return base64Payload.decodeBase64Bytes().decodeToString()
}

public inline fun <reified T> decodeJwtClaims(token: String): T {
    val json = Json { ignoreUnknownKeys = true }
    return json.decodeFromString(decodeJwtPayload(token))
}
