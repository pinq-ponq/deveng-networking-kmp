package networking.session

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

public object SessionGeneration {
    private val generationMutex = Mutex()
    private var currentGeneration = 0

    public suspend fun currentValue(): Int = generationMutex.withLock { currentGeneration }

    public suspend fun advance() {
        generationMutex.withLock { currentGeneration += 1 }
    }

    // The lock spans the check and the writes together: a sign-out landing between the two would
    // otherwise put a fresh token back into a store that was just cleared.
    public suspend fun persistIfSessionUnchanged(
        expectedGeneration: Int,
        persistTokens: suspend () -> Unit
    ): Boolean = generationMutex.withLock {
        if (currentGeneration != expectedGeneration) {
            return@withLock false
        }

        persistTokens()
        true
    }
}
