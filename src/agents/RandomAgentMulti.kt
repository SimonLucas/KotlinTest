package agents

import ggi.AbstractGameState
import ggi.AbstractGameStateMulti
import ggi.SimplePlayerInterface
import ggi.SimplePlayerInterfaceMulti
import java.util.*

// import java.util.*

class RandomAgentMulti (val seed: Long = -1): SimplePlayerInterfaceMulti {

    override fun getAgentType(): String {
        return "RandomAgent"
    }

    val random = Random()

    init{
        if (seed != -1L)
            random.setSeed(seed)
    }

    override fun getAction(gameState: AbstractGameStateMulti, playerId: Int): Int {
        return random.nextInt(gameState.nActions(playerId))
    }

    override fun reset(): SimplePlayerInterfaceMulti {
        // do nothing
        return this
    }
}
