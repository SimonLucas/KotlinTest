package games.sokoban

import ggi.AbstractGameState
import ggi.ExtendedAbstractGameState

interface GridInterface {
    fun getCell(x: Int, y: Int): Char
    fun setCell(x: Int, y: Int, value: Char)
    fun getWidth(): Int
    fun getHeight(): Int
}

interface ForwardGridModel : ExtendedAbstractGameState {
    fun setGridArray(array: CharArray, playerX: Int, playerY: Int): ForwardGridModel
    // fun getGrid() : SimpleGrid
}

class PatternSampler(val span: Int = 2) {

    // should really generalise this to offer different extraction patterns
    fun extractVector(grid: GridInterface, x: Int, y: Int): ArrayList<Char> {
        val v = ArrayList<Char>()
        // add the centre cell
        v.add(grid.getCell(x, y))
        // now row except centre
        for (xx in x - span..x + span) {
            if (xx != x) v.add(grid.getCell(xx, y))
        }
        // now column except centre
        for (yy in y - span..y + span) {
            if (yy != y) v.add(grid.getCell(x, yy))
        }
        return v
    }

}

class LocalForwardModel(val tileData: HashMap<Example, TileDistribution>,
                        val rewardData: HashMap<Example, RewardDistribution>,
                        val span: Int = 2,
                        var dummySpeedTest: Boolean = false
) : ForwardGridModel {


    // learn this from the data
    var nActions = 0

    init {
        // todo:  count the distinct number of actions
//        println(dummySpeedTest)
//        println(span)

    }

    companion object Ticker {
        var total: Long = 0
    }

    var grid = SimpleGrid()
    var score = 0.0

    // override fun getGrid() : SimpleGrid { return grid }

    override fun setGridArray(array: CharArray, playerX: Int, playerY: Int): ForwardGridModel {
        grid.setGrid(array, playerX, playerY)
        return this
    }

    override fun copy(): AbstractGameState {
        val lfm = LocalForwardModel(tileData, rewardData, span, dummySpeedTest)
        lfm.grid = grid.deepCopy()
        lfm.score = score
        return lfm
    }

    override fun next(actions: IntArray): AbstractGameState {

        // can have different policies for picking an answer
        // can be either deterministic or stochastic

        // need to iterate over all the grid positions updating the data
//        println(dummySpeedTest)
//        if (dummySpeedTest) {
//            grid = grid.deepCopy()
//            nTicks++
//            total++
//            return this
//        }

        val nextGrid = grid.deepCopy()
        val action = actions[0]
        // println("Action = " + action)

        val rewarder = RewardEstimator()

        val sampler = PatternSampler(span)

        for (x in 0 until grid.getWidth()) {
            for (y in 0 until grid.getHeight()) {

                val ip = sampler.extractVector(grid, x, y)
                // data[Example(ip, action, op)]++
                val example = Example(ip, action)

                if (dummySpeedTest) {
                    // skip all the logic and just guess the next cell from the current value
                    // which will be at position zero in the input
                    nextGrid.setCell(x, y, ip[0])
                } else {

                    // now update the tile data
                    var tileDis = tileData[example]
                    val bestGuess = guessTile(tileDis, grid.getCell(x, y))
                    nextGrid.setCell(x, y, bestGuess)

                }

                if (!bypassScore) {
                    // now update the reward data
                    var rewardDis = rewardData[example]
                    rewarder.addDis(rewardDis)
                }
            }
        }


//        if (action != 0) {
//            println(rewarder.logProbs)
//            val zeroP = rewarder.logProbs[0.0]
//            val oneP = rewarder.logProbs[1.0]
//            if (oneP!=null && zeroP!=null)
//                println("PDiff = %.4f".format( zeroP - oneP))
//        }
//


        if (!bypassScore) score += rewarder.mostLikely()

        grid = nextGrid
        nTicks++
        Ticker.total++
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

    fun countScore() : Double {
        return grid.grid.count{t -> t == '+'}.toDouble()
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



