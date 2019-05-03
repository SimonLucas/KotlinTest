package games.sokoban

import ggi.AbstractGameState
import ggi.ExtendedAbstractGameState

interface GridInterface {
    fun getCell(x: Int, y: Int): Char
    fun setCell(x: Int, y: Int, value: Char)
    fun getWidth() : Int
    fun getHeight() : Int
}


data class SimpleGrid(val w: Int = 8, val h: Int = 7) : GridInterface {

    var grid: CharArray = CharArray(w * h)

    fun getCell(i: Int): Char = grid[i]

    fun setCell(i: Int, v: Char) {
        grid[i] = v
    }

    override fun getCell(x: Int, y: Int): Char {
        val xx = (x + w) % w
        val yy = (y + h) % h
        return grid[xx + w * yy]
    }

    override fun setCell(x: Int, y: Int, value: Char) {
        if (x < 0 || y < 0 || x >= w || y >= h) return
        grid[x + w * y] = value
    }

    override fun getWidth() : Int {
        return this.w
    }

    override fun getHeight() : Int {
        return this.h
    }

    fun deepCopy(): SimpleGrid {
        val gc = this.copy()
        gc.grid = grid.copyOf()
        return gc
    }

    fun setGrid(grid: CharArray) : SimpleGrid {
        this.grid = grid.copyOf()
        return this
    }
}

// should really generalise this to offer different extraction patterns
fun extractVector(grid: GridInterface, x: Int, y: Int): ArrayList<Char> {
    val v = ArrayList<Char>()
    // add the centre cell
    v.add(grid.getCell(x,y))
    // now row except centre
    for (xx in x - span .. x + span) {
        if (xx != x) v.add(grid.getCell(xx, y))
    }
    // now column except centre
    for (yy in y - span .. y + span) {
        if (yy != y) v.add(grid.getCell(x, yy))
    }
    return v
}

class LocalForwardModel (    val tileData : HashMap<Example, TileDistribution>,
                             val rewardData : HashMap<Example, RewardDistribution>
): ExtendedAbstractGameState {

    // learn this from the data
    var nActions = 0

    init {
        // todo:  count the distinct number of actions

    }

    companion object Ticker {
        var total :Long = 0
    }

    var grid = SimpleGrid()
    var score = 0.0

    fun setGrid(array: CharArray) : LocalForwardModel {
        grid.setGrid(array)
        return this
    }

    override fun copy(): AbstractGameState {
        val lfm = LocalForwardModel(tileData, rewardData)
        lfm.grid = grid.deepCopy()
        lfm.score = score
        return lfm
    }

    override fun next(actions: IntArray): AbstractGameState {

        // can have different policies for picking an answer
        // can be either deterministic or stochastic

        // need to iterate over all the grid positions updating the data

        val nextGrid = grid.deepCopy()
        val action = actions[0]

        val rewarder = RewardEstimator()

        for (x in 0 until grid.getWidth()) {
            for (y in 0 until grid.getHeight()) {

                val ip = extractVector(grid, x, y)
                // data[Example(ip, action, op)]++
                val example = Example(ip, action)

                // now update the tile data
                var tileDis = tileData[example]
                val bestGuess = guessTile(tileDis)
                nextGrid.setCell(x, y, bestGuess)


                // now update the reward data
                var rewardDis = rewardData[example]
                rewarder.addDis(rewardDis)
            }
        }

        score += rewarder.mostLikely()

        nTicks++
        Ticker.total++
        return this
    }

    override fun nActions(): Int {
        // for now just return the correct answer for Sokoban
        return 5
    }

    override fun score(): Double {
        return score
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



