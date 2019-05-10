package games.sokoban

import ggi.AbstractGameState
import ggi.ExtendedAbstractGameState

class GPModel() : ForwardGridModel {

    private var grid = SimpleGrid(0,0)
    var score = 0.0

    companion object Ticker {
        var total: Long = 0
    }

    override fun getGrid() : SimpleGrid { return grid }
    override fun setGrid(simpleGrid: SimpleGrid) {
        grid = simpleGrid
    }




//    override fun setGridArray(array: CharArray, playerX: Int, playerY: Int): ForwardGridModel {
//        grid.setGrid(array, playerX, playerY)
//        return this
//    }

    override fun copy(): AbstractGameState {
        val gpm = GPModel()
        gpm.grid = grid.deepCopy()
        gpm.score = score
        return gpm
    }

    override fun next(actions: IntArray): AbstractGameState {
        val nextGrid = grid.deepCopy()
        val action = actions[0]
        val gpLocal = LocalGPModel()
        for (x in 0 until grid.getWidth()) {
            for (y in 0 until grid.getHeight()) {
                val bestGuess = gpLocal.nextTile(grid, x, y, action)
                nextGrid.setCell(x, y, bestGuess)
            }
        }
        grid = nextGrid
        nTicks++
        total++
        return this
    }

    override fun nActions(): Int {
        // for now just return the correct answer for Sokoban
        return 5
    }

    val bypassScore = true

    override fun score(): Double {
        if (bypassScore)
            return countScore()
        else return score
    }

    fun countScore(): Double {
        return grid.grid.count { t -> t == '+' }.toDouble()
    }

    override fun isTerminal(): Boolean {
        // return false for now as we don't have a way of learning this yet
        return false
    }

    var nTicks = 0
    override fun nTicks(): Int {
        return nTicks
    }

    override fun totalTicks(): Long {
        return Ticker.total
    }

    override fun resetTotalTicks() {
        Ticker.total = 0
    }

    override fun randomInitialState(): AbstractGameState {
        // deliberately do nothing for now
        println("Not able to set a random initial state")
        return this
    }

}

class LocalGPModel () {
    val xOff = intArrayOf(0,0,-1,0,1)
    val yOff = intArrayOf(0, 1, 0, -1, 0)
    fun nextTile(grid: SimpleGrid, x:Int, y:Int, action: Int) : Char {

        // here is what a "linear" GP solution might look like
        if (action == 0) return grid.getCell(x,y)
        val xx = xOff[action]
        val yy = yOff[action]
        val cur = grid.getCell(x,y)
        if (cur == 'w') return 'w'
        if (cur == '+') return '+'
        val oneAway = grid.getCell(x+xx, y+yy)
        val twoAway: Char = grid.getCell(x+2*xx, y+2*yy)

        if (cur == '.' && oneAway == '.') return '.'
        if (oneAway == 'A') return 'A'
        if (oneAway == '*' && twoAway == 'A' && cur == 'o') return '+'
        if (cur == '.' && oneAway == '*' && twoAway == 'A') return '*'
        return cur
    }
}

