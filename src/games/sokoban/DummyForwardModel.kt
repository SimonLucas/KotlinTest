package games.sokoban

import ggi.AbstractGameState

/**
 * Useful to have as a model that does nothing.
 *
 * This enables finding an upper bound on the speed of the agents
 *
 * and to sanity check their play performance with a model that does NOTHING
 */

class DummyForwardModel : ForwardGridModel {

    // keep this for compatibility with other methods
    var grid = SimpleGrid()

    companion object {
        var total: Long = 0
    }

    var nTicks = 0

    override fun setGridArray(array: CharArray, playerX: Int, playerY: Int): ForwardGridModel {
        return this
    }

    override fun copy(): AbstractGameState {
        val dfm = DummyForwardModel()
        dfm.nTicks = nTicks
        return this
    }

    override fun next(actions: IntArray): AbstractGameState {
        nTicks++
        total++
        return this
    }

    override fun nActions(): Int {
        return 5
    }

    override fun score(): Double {
        return 0.0
    }

    override fun isTerminal(): Boolean {
        return false
    }

    override fun nTicks(): Int {
        return nTicks
    }

    override fun totalTicks(): Long {
        return total
    }

    override fun resetTotalTicks() {
        total = 0
    }

    override fun randomInitialState(): AbstractGameState {
        return this
    }
}