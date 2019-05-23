package games.sokoban

import ggi.AbstractGameState
import ggi.ExtendedAbstractGameState

class SokobanMacroGlue(var gameState: Sokoban) : ExtendedAbstractGameState {


    // consider the types of MacroAction
    // move to particular squares
    // aha but what would the value of those actions be?

    override fun copy(): AbstractGameState {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun next(actions: IntArray): AbstractGameState {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun nActions(): Int {

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun score(): Double {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isTerminal(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun nTicks(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun totalTicks(): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun resetTotalTicks() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun randomInitialState(): AbstractGameState {
        TODO("not implemented")
    }


    companion object {
        var total: Long = 0
    }



}