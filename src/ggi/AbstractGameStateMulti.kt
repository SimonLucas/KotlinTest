package ggi

import games.eventqueuegame.PlayerId

/**
 *  This interface is designed for multi-player games, and supports the return of different scores for different players.
 *
 *  This can reflect the fact that different players may have different objectives (not just the opposite / negative of the other one)
 */

interface AbstractGameStateMulti {

    fun copy(): AbstractGameStateMulti

    // the ith entry of the actions array is the action for the ith player
    // next is used to advance the state of the game given the current
    // set of actions
    // this can either be for the 'real' game
    // or for a copy of the game to use in statistical forward planning, for example
    // fun next(actions: IntArray, playerId: Int): AbstractGameState
    fun next(actions: IntArray): AbstractGameStateMulti

    // the number of actions available to a player in the current state
    fun nActions(playerId: Int): Int

    fun score(playerId: Int): Double

    // can a game be over for one player but not the other one?
    fun isTerminal(playerId: Int): Boolean

    fun nTicks(): Int

}

interface ExtendedAbstractGameStateMulti : AbstractGameStateMulti {
    fun totalTicks() : Long

    fun resetTotalTicks(): Unit

    fun randomInitialState(): AbstractGameStateMulti
}

