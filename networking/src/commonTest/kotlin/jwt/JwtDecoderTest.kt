package networking.jwt

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class JwtDecoderTest {

    @Test
    fun decodeJwtPayload_withAJwt_returnsThePayloadJson() {
        assertEquals(PAYLOAD_JSON, decodeJwtPayload(JWT))
    }

    @Test
    fun decodeJwtPayload_withoutThreeParts_throws() {
        assertFailsWith<IllegalArgumentException> { decodeJwtPayload("header.payload") }
    }

    @Test
    fun decodeJwtClaims_withUnknownClaims_readsTheKnownOnes() {
        val claims = decodeJwtClaims<UserClaims>(JWT)

        assertEquals(UserClaims(userId = USER_ID, expiresAtEpochSeconds = EXPIRES_AT_EPOCH_SECONDS), claims)
    }

    @Serializable
    private data class UserClaims(
        @SerialName("userId")
        val userId: String?,
        @SerialName("exp")
        val expiresAtEpochSeconds: Long? = null
    )

    private companion object {
        const val USER_ID = "user-1"
        const val EXPIRES_AT_EPOCH_SECONDS = 1_900_000_000L
        const val PAYLOAD_JSON = """{"userId":"user-1","exp":1900000000,"role":"member"}"""
        // header {"alg":"HS256","typ":"JWT"}, payload PAYLOAD_JSON, signature not checked
        const val JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLTEiLCJleHAiOjE5MDAwMDAwMDAsInJvbGUiOiJtZW1iZXIifQ.c2lnbmF0dXJl"
    }
}
