package ggi.game

import ggi.AbstractGameState

interface ActionAbstractGameState : AbstractGameState {

    // we do not use two methods of the super interface
    // TODO: Tidy up inheritance
    override fun nActions(): Int {
        throw AssertionError("Should use codonsPerAction() to determine length of genome for Action")
    }
    override fun next(actions: IntArray) : AbstractGameState {
        throw AssertionError("Should use next() with List<Action>")
    }

    fun playerCount(): Int

    fun codonsPerAction(): Int

    fun possibleActions(player: Int): List<Action>

    fun next(actions: List<Action>) : ActionAbstractGameState

    fun translateGene(player: Int, gene: IntArray) : Action
}

interface Action {
    fun apply(state: ActionAbstractGameState) : ActionAbstractGameState
}
