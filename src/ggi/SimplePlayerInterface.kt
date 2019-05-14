package ggi

import ggi.game.*

interface SimplePlayerInterface {
    fun getAction(gameState: AbstractGameState, playerId: Int) : Int
    fun reset() : SimplePlayerInterface
    fun getAgentType(): String
}

interface SimpleActionPlayerInterface {
    fun getAction(gameState: ActionAbstractGameState, playerId: Int) : Action
    fun getPlan(gameState: ActionAbstractGameState, playerId: Int): List<Action>
    fun reset() : SimpleActionPlayerInterface
    fun getAgentType(): String
    fun getForwardModelInterface(): SimpleActionPlayerInterface
}
