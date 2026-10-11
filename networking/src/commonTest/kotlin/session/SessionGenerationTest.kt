package networking.session

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class SessionGenerationTest {

    @Test
    fun persistIfSessionUnchanged_withTheCurrentGeneration_persistsTheTokens() = runTest {
        var persistCount = 0

        val isPersisted = SessionGeneration.persistIfSessionUnchanged(SessionGeneration.currentValue()) {
            persistCount += 1
        }

        assertTrue(isPersisted)
        assertEquals(1, persistCount)
    }

    @Test
    fun persistIfSessionUnchanged_afterTheSessionAdvanced_skipsTheTokens() = runTest {
        var persistCount = 0
        val generationAtStart = SessionGeneration.currentValue()
        SessionGeneration.advance()

        val isPersisted = SessionGeneration.persistIfSessionUnchanged(generationAtStart) {
            persistCount += 1
        }

        assertFalse(isPersisted)
        assertEquals(0, persistCount)
    }
}
