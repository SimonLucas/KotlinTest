package agents

import ggi.AbstractGameState
import ggi.AbstractGameStateMulti
import ggi.SimplePlayerInterface
import ggi.SimplePlayerInterfaceMulti


data class DoNothingAgentMulti (var action: Int = 0) : SimplePlayerInterfaceMulti {
    override fun getAgentType(): String {
        return "DoNothingAgent"
    }

    override fun getAction(gameState: AbstractGameStateMulti, playerId: Int): Int {
        // return zero without knowing what this will do
        return action
    }

    override fun reset(): SimplePlayerInterfaceMulti {
        return this
    }

//    override fun toString(): String {
//        return "Do Nothing Agent"
//    }


}
