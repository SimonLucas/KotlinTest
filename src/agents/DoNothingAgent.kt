package agents

import ggi.AbstractGameState
import ggi.SimpleActionPlayerInterface
import ggi.SimplePlayerInterface
import ggi.game.Action
import ggi.game.ActionAbstractGameState


data class DoNothingAgent (var action: Int = 0) : SimplePlayerInterface {
    override fun getAgentType(): String {
        return "DoNothingAgent"
    }

    override fun getAction(gameState: AbstractGameState, playerId: Int): Int {
        // return zero without knowing what this will do
        return action
    }

    override fun reset(): SimplePlayerInterface {
        return this
    }

}
