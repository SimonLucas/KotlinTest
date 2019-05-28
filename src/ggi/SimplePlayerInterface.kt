package ggi

import ggi.game.*

interface SimplePlayerInterface {
    fun getAction(gameState: AbstractGameState, playerId: Int): Int
    fun reset(): SimplePlayerInterface
    fun getAgentType(): String
}

interface SimpleActionPlayerInterface: SimplePlayerInterface {
    override fun getAction(gameState: AbstractGameState, playerId: Int): Int {
        throw AssertionError("Should not use")
    }
    fun getAction(gameState: ActionAbstractGameState, playerId: Int): Action
    fun getPlan(gameState: ActionAbstractGameState, playerId: Int): List<Action>
    override fun reset(): SimpleActionPlayerInterface
    fun getForwardModelInterface(): SimpleActionPlayerInterface
    fun backPropagate(finalScore: Double)
}
