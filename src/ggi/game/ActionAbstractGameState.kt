package ggi.game

import games.eventqueuegame.PlayerId
import ggi.AbstractGameState
import ggi.SimpleActionPlayerInterface

interface ActionAbstractGameState : AbstractGameState {

    // we do not use one method of the super interface
    override fun next(actions: IntArray) : AbstractGameState {
        throw AssertionError("Should use next() with List<Action>")
    }

    fun next(forwardTicks: Int) : ActionAbstractGameState

    /** this method is shared with AbstractGameState, but defines the number of different
    values that each location (or base) on the genome can hold, so it is not strictly the number of 'actions'
     any more. TODO: Refactor the inheritance set up to remove this opportunity for misunderstanding
    **/
    override fun nActions(): Int

    /**
     * Unlike AbstractGameState, we now allow different players to have different score functions
     * so that things are not necessarily purely zero-sum
     */
    fun score(player: Int): Double

    override fun score(): Double {
        throw AssertionError("Please specify which player's perspective you mean")
    }

    fun playerCount(): Int

    fun possibleActions(player: Int): List<Action<*>>

    fun translateGene(player: Int, gene: IntArray) : Action<*>

    fun registerAgent(player: Int, agent: SimpleActionPlayerInterface)

    fun getAgent(player: Int): SimpleActionPlayerInterface
}

interface Action<T>  {
    fun apply(state: T) : T
    fun visibleTo(player: Int, state: T): Boolean
}
