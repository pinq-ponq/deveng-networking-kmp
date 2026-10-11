package networking.session

import kotlin.test.Test
import kotlin.test.assertEquals

class SessionExpiryNotifierTest {

    private val sessionExpiryNotifier = SessionExpiryNotifier()

    @Test
    fun notifySessionExpired_callsOnlyTheLatestHandler() {
        var firstHandlerCallCount = 0
        var latestHandlerCallCount = 0
        sessionExpiryNotifier.setSessionExpiredHandler { firstHandlerCallCount += 1 }
        sessionExpiryNotifier.setSessionExpiredHandler { latestHandlerCallCount += 1 }

        sessionExpiryNotifier.notifySessionExpired()

        assertEquals(0, firstHandlerCallCount)
        assertEquals(1, latestHandlerCallCount)
    }

    @Test
    fun notifySessionExpired_withoutHandler_doesNothing() {
        sessionExpiryNotifier.notifySessionExpired()
    }
}
