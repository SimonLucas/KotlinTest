package games.sokoban

import agents.RandomAgent
import games.gridgame.data
import ggi.AbstractGameState
import ggi.ExtendedAbstractGameState
import utilities.ElapsedTimer
import utilities.JEasyFrame
import utilities.StatSummary

val span = 2

fun main(args: Array<String>) {

    var game = Sokoban()
    game.print()
    val actions = intArrayOf(0, 0)
    //var agent1: SimplePlayerInterface = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 5, nEvals = 40)
    var agent = RandomAgent()

    val gatherer = Gatherer()

    val timer = ElapsedTimer()
    val nSteps = 1000
    for (i in 0 until nSteps) {
        actions[0] = agent.getAction(game.copy(), Constants.player1)
        val grid1 = game.board.deepCopy()
        // setting cells is a quick hack to get around the fact that
        // it's not properly updated
        grid1.setCell(grid1.playerX, grid1.playerY, 'A')
        val score1 = game.score()
        game.next(actions)
        val grid2 = game.board.deepCopy()
        grid2.setCell(grid2.playerX, grid2.playerY, 'A')
        gatherer.addGrid(grid1, grid2, actions[0], game.score() - score1)
    }

    // now print the patterns

    gatherer.report()
    game.print()
    println("Ran for $nSteps steps")
    println("Generated ${gatherer.tileData.size} unique tile observations")
    println("Generated ${gatherer.rewardData.size} unique reward observations")
    println("Total local patterns = " + gatherer.total)
    println(timer)
}

// the input to the local model is the input array and the action taken
data class Example(val ip: ArrayList<Char>, val action: Int)


// there is probably a nicer way to bring out the commonalities between
// the two types of Distribution - only the type of item that we're counting is
// different
// could just have them as object type
// except that further down the line we might want to bucket the reward distributions

class TileDistribution() {
    val dis = HashMap<Char,Int>()
    fun add(op: Char) {
        var count = dis.get(op)
        if (count == null) count =0
        count++
        dis[op] = count
    }
    override fun toString() : String {
        return dis.toString()
    }
}

class RewardDistribution() {
    val dis = HashMap<Double,Int>()
    fun add(op: Double) {
        var count = dis.get(op)
        if (count == null) count =0
        count++
        dis[op] = count
    }
    override fun toString() : String {
        return dis.toString()
    }
}


// copy the grid etc

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

class Gatherer {

    val tileData = HashMap<Example, TileDistribution>()
    val rewardData = HashMap<Example, RewardDistribution>()
    var total = 0

    fun addGrid(grid1: Grid, grid2: Grid, action: Int, rewardDelta: Double) {
        assert(grid1.getWidth() == grid2.getWidth() && grid1.getHeight() == grid2.getHeight())
        for (x in 0 until grid1.getWidth()) {
            for (y in 0 until grid1.getHeight()) {
                val op = grid2.getCell(x, y)
                val ip = extractVector(grid1, x, y)
                // data[Example(ip, action, op)]++
                val example = Example(ip, action)

                // now update the tile data
                var tileDis = tileData[example]
                if (tileDis == null) {
                    tileDis = TileDistribution()
                    tileData.put(example, tileDis)
                }
                tileDis.add(op)

                // now update the reward data
                var rewardDis = rewardData[example]
                if (rewardDis == null) {
                    rewardDis = RewardDistribution()
                    rewardData.put(example, rewardDis)
                }
                rewardDis.add(rewardDelta)

                // and the reward data

                total++

            }
        }
    }

    // should really generalise this to offer different extraction patterns
    fun extractVector(grid: Grid, x: Int, y: Int): ArrayList<Char> {
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

    fun report() {
        println("Tile distributions:")
        tileData.forEach{key, value -> println("$key -> $value")}
        println()
        println("Reward distributions:")
        rewardData.forEach{key, value -> println("$key -> $value")}
    }
}


data class SimpleGrid(val w: Int = 8, val h: Int = 7) {

    var grid: CharArray = CharArray(w * h)

    fun getCell(i: Int): Char = grid[i]

    fun setCell(i: Int, v: Char) {
        grid[i] = v
    }

    fun getCell(x: Int, y: Int): Char {
        val xx = (x + w) % w
        val yy = (y + h) % h
        return grid[xx + w * yy]
    }

    fun setCell(x: Int, y: Int, value: Char) {
        if (x < 0 || y < 0 || x >= w || y >= h) return
        grid[x + w * y] = value
    }

    fun getWidth() : Int {
        return this.w
    }

    fun getHeight() : Int {
        return this.h
    }

    fun deepCopy(): SimpleGrid {
        val gc = this.copy()
        gc.grid = grid.copyOf()
        return gc
    }
}
