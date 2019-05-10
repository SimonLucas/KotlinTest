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

    var setWall = false

    // keep this for compatibility with other methods
    private var grid = SimpleGrid(0,0)

    companion object {
        var total: Long = 0
    }


    override fun getGrid() : SimpleGrid { return grid }
    override fun setGrid(simpleGrid: SimpleGrid) {
        grid = simpleGrid
    }



    var nTicks = 0

//    override fun setGridArray(array: CharArray, playerX: Int, playerY: Int): ForwardGridModel {
//        return this
//    }

    override fun copy(): AbstractGameState {
        val dfm = DummyForwardModel()
        dfm.nTicks = nTicks
        return this
    }

    override fun next(actions: IntArray): AbstractGameState {
        if (setWall) {
            grid.grid.forEachIndexed{i,c -> grid.grid[i] = 'w'}
        }
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
