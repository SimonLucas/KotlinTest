package games.sokoban

import ggi.AbstractGameState
import ggi.ExtendedAbstractGameState

class SokobanMacroGlue(var gameState: Sokoban) : ExtendedAbstractGameState {


    // consider the types of MacroAction
    // move to particular squares
    // aha but what would the value of those actions be?







    override fun copy(): AbstractGameState {
        val gs = SokobanMacroGlue(gameState.copy() as Sokoban)
        // may want to update other state variables here
        return gs
    }

    override fun next(actions: IntArray): AbstractGameState {

        // here we translate the current state into the next state

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun nActions(): Int {

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.



    }

    override fun score(): Double {
        // interesting one here: what is the score?
        // currently just return the game state score, but
        // we might want to add some heuristic score to
        return gameState.score()
    }



    companion object {
        var total: Long = 0
    }

    override fun isTerminal(): Boolean {
        return false
    }

    var nTicks = 0
    override fun nTicks(): Int {
        return nTicks
    }

    override fun totalTicks(): Long {
        return DummyForwardModel.total
    }

    override fun resetTotalTicks() {
        DummyForwardModel.total = 0
    }

    override fun randomInitialState(): AbstractGameState {
        return this
    }


}