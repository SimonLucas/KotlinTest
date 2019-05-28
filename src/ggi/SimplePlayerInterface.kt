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
    fun <T : ActionAbstractGameState> getAction(gameState: T, playerId: Int): Action<T>
    fun <T : ActionAbstractGameState> getPlan(gameState: T, playerId: Int): List<Action<T>>
    override fun reset(): SimpleActionPlayerInterface
    fun getForwardModelInterface(): SimpleActionPlayerInterface
    fun backPropagate(finalScore: Double)
}
