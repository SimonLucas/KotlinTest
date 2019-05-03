package games.sokoban

import agents.RandomAgent
import games.gridgame.data
import ggi.AbstractGameState
import ggi.ExtendedAbstractGameState
import utilities.ElapsedTimer
import utilities.JEasyFrame
import utilities.Picker
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


//interface Sampler {
//
//}

// copy the grid etc

val defaultTile = 'w'
fun guessTile(dis: TileDistribution?) : Char {
    if (dis == null) return defaultTile
    // pick the most likely one for now
    val picker = Picker<Char>()
    dis.dis.forEach{k,v -> picker.add(v.toDouble(), k)}
    val ret = picker.best
    // put in a quick and dirty default of a wall for now
    return if (ret != null) ret else defaultTile
}


class RewardEstimator {
    val epsilon = 1e-3

    // we'll add to this each time a reward comes in
    // actually track the log probs to avoid underflow
    val logProbs = HashMap<Double,Double>()

    fun addDis(dis: RewardDistribution?) {
        // do nothing with nothing!
        if (dis == null) return

        // convert each one to a log probability and then update ...

        // set to espilon to avoid divide by zero
        var tot = epsilon
        // the count total occurrences
        dis.dis.forEach { k, v -> tot += v }

        // now update the log probability estimates of each reward value
        dis.dis.forEach { k, v ->
            val lp = Math.log(v / tot)
            var x = logProbs[k]
            if (x == null) x = 0.0
            logProbs[k] = x + lp
        }
    }

    fun mostLikely() : Double {
        val picker = Picker<Double>()
        logProbs.forEach { k, v -> picker.add(v, k) }
        val estimate = picker.best
        if (estimate != null)
            return estimate else return defaultReward
    }

    val defaultReward = 0.0


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


    fun report() {
        println("Tile distributions:")
        tileData.forEach{key, value -> println("$key -> $value")}
        println()
        println("Reward distributions:")
        rewardData.forEach{key, value -> println("$key -> $value")}
    }
}


