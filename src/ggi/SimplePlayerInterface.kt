package ggi

interface SimplePlayerInterface {
    fun getAction(gameState: AbstractGameState, playerId: Int) : Int
    fun reset() : SimplePlayerInterface
    fun getAgentType(): String
}
