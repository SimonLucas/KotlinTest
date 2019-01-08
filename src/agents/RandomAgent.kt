package agents

import ggi.AbstractGameState
import ggi.SimplePlayerInterface
import java.util.*

class RandomAgent : SimplePlayerInterface {
    val random = Random()
    override fun getAction(gameState: AbstractGameState, playerId: Int): Int {
        return random.nextInt(gameState.nActions())
    }

    override fun reset(): SimplePlayerInterface {
        // do nothing
        return this
    }
}
