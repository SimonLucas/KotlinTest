package games.gridgame

import agents.DoNothingAgent
import agents.SimpleEvoAgent
import forwardmodels.decisiontree.DecisionTree
import ggi.AbstractGameState
import ggi.ExtendedAbstractGameState
import ggi.SimplePlayerInterface
import utilities.JEasyFrame
import views.GridView
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import forwardmodels.modelinterface.ForwardModelTrainerSimpleGridGame


enum class InputType {
    None, PlayerInt, PlayerOneHot, Simple, Sokoban
}

val includeNeighbourInputs = InputType.PlayerInt

// set this to null to turn off learning
var learner = StatLearner()

fun main(args: Array<String>) {

    val harvestData = true

    var game = GridGame(30, 30, harvestData).setFast(false)
    var decisionTree : DecisionTree

    (game.updateRule as MyRule).next = ::generalSumUpdate

    game.rewardFactor = 1.0;
    // game.setFast(true)
    println(game.grid)
    val gv = GridView(game.grid)
    val frame = JEasyFrame(gv, "Life Game")
    val actions = intArrayOf(0, 0)
    var agent1: SimplePlayerInterface = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 5, nEvals = 40)
    var agent2: SimplePlayerInterface = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 5, nEvals = 10)
    //agent1 = RandomAgent()
    // agent1 = DoNothingAgent(game.doNothingAction())
    agent2 = DoNothingAgent(game.doNothingAction())

    val modelTrainer = ForwardModelTrainerSimpleGridGame(InputType.PlayerInt)

    val nSteps = 2000
    for (i in 0 until nSteps) {
        actions[0] = agent1.getAction(game.copy(), Constants.player1)
        actions[1] = agent2.getAction(game.copy(), Constants.player2)
        game.next(actions)

        gv.grid = game.grid
        gv.repaint()
        Thread.sleep(50)
        frame.title = "tick = ${game.nTicks}, score = ${game.score()}"
        // System.exit(0)
        // game = game.copy() as GridGame
        // println(game.updateRule.next)
        println("$i\t N distinct patterns learned = ${learner.lut.size}")

        //decisionTree = modelTrainer.trainModel(data) as DecisionTree
    }

    val learner = StatLearner()
    if (harvestData) {
        val set = HashSet<Pattern>()
        val input = HashSet<ArrayList<Int>>()
        for (p in data) {
            // println(p)
            set.add(p)
            input.add(p.ip)
            // learner.add(p.ip, p.op)
        }
        // println("\nUnique IP / OP pairs:")
        // set.forEach { println(it) }
        // println("\nUnique Inputs:")
        // input.forEach { println(it) }
        println("\nN Patterns  =  " + data.size)
        println("Unique size = " + set.size)
        println("Unique ips  = " + input.size)

        // learner.report()

    }
}




fun generalSumUpdate(centre: Int, sum: Int): Int {

    // println("Sum =" + sum)

    val lut = arrayOf(
            intArrayOf(0, 0, 0, 1, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 1, 1, 0, 0, 0, 0, 0)
    )

    return lut[centre][sum]

}

data class Pattern(val ip: ArrayList<Int>, val op: Int)


val data = ArrayList<Pattern>()

val random = Random()

object Constants {
    val player1 = 0
    val player2 = 1
    val playerValues = intArrayOf(player1, player2)
    val on: Int = 1
    val off: Int = 0
    val outside: Int = 0
    // val range = 0..1
}

data class Grid(val w: Int = 20, val h: Int = 20, val wrap: Boolean = true, val seed: Long = -1) {
    var grid: IntArray = randomGrid()

    fun randomGrid() = IntArray(w * h, { random.nextInt(2) })

    fun setAll (v: Int) {grid.fill(v)}

    fun getCell(i: Int): Int = grid[i]

    fun setCell(i: Int, v: Int) {
        grid[i] = v
    }

    fun invertCell(i: Int) {
        grid[i] = 1 - grid[i]
    }

    fun getCell(x: Int, y: Int): Int {
//        if (!wrap)
//            if (x < 0 || y < 0 || x >= w || y >= h) return Constants.outside
        val xx = (x + w) % w
        val yy = (y + h) % h
        return grid[xx + w * yy]
    }

    fun setCell(x: Int, y: Int, value: Int) {
        if (x < 0 || y < 0 || x >= w || y >= h) return
        grid[x + w * y] = value
    }

    fun difference (other: Grid) : Int {
        var tot = 0
        // lazily assume same dimensions...
        for (i in 0 until grid.size)
            tot += if (grid[i] == other.grid[i]) 0 else 1
        return tot
    }

    init {

        // Random seed is -1
        if(seed != -1L) {
            random.setSeed(seed);
        }
        // println(gridGame)

    }

    fun deepCopy(): Grid {
        val gc = this.copy()
        gc.grid = grid.copyOf()
        return gc
    }

}

var totalTicks: Long = 0

open class GridGame : ExtendedAbstractGameState {
    override fun randomInitialState(): AbstractGameState {
        grid.grid = grid.randomGrid()
        return this
    }

    var updateRule : UpdateRule = MyRule()
    var grid: Grid = Grid()
    var nTicks = 0

    val harvestData: Boolean

    // negate this to reward destroying life
    var rewardFactor = 1.0
    var fastUpdate: FastUpdate? = null

    constructor(w: Int = 20, h: Int = 20, harvestData: Boolean = false ,seed: Long = -1) {
        grid = Grid(w, h, seed=seed)
        this.harvestData = harvestData
    }

    fun doNothingAction() = grid.grid.size

    fun setFast(fast: Boolean): GridGame {
        if (fast) {
            fastUpdate = FastUpdate(grid)
        } else {
            fastUpdate = null
        }
        return this
    }

    override fun copy(): AbstractGameState {
        val gridGame = GridGame()
        gridGame.nTicks = nTicks
        gridGame.grid = grid.deepCopy()
        gridGame.fastUpdate = fastUpdate
        gridGame.updateRule = updateRule
        gridGame.rewardFactor = rewardFactor
        return gridGame
    }

    override fun next(actions: IntArray): AbstractGameState {
        // val playerId = 0

//        val p1Action = actions[0]
//        val p2Action = actions[1]
//        // if both players choose the same action then do nothing
//
//        // otherwise invert at each position - or if inverting then no need to ignore
//        gridGame.invertCell(p1Action)
//        gridGame.invertCell(p2Action)


        // capture the player input

        // apply the player actions
        for (action in actions)
            if (action != doNothingAction())
                grid.invertCell(action)


        // capture the local gridGame pattern input

        val gridCopy = grid.copy()

        if (fastUpdate != null) {
            with(grid) {
                // computeIndex()
                for (i in 0 until grid.size) {
                    var sum = 0
                    for (ix in fastUpdate!!.index[i]) {
                        sum += grid.get(ix)
                    }
                    // println("$i : $sum")
                    gridCopy.setCell(i, sumFun(sum))
                }
            }
        } else {

            for (i in 0 until grid.w) {
                for (j in 0 until grid.h) {
                    gridCopy.setCell(i, j, updateRule.cellUpdate(grid, i, j))
                }
            }
        }

        if (harvestData || learner!=null) addData(grid, gridCopy, actions, data)

        grid = gridCopy

        totalTicks++
        nTicks++
        return this
    }

    fun sumFun(sum: Int): Int {
        return if (sum < 3 || sum > 4) 0 else 1
    }

    override fun nActions(): Int {
        return grid.grid.size + 1
    }

    override fun score(): Double {
        return rewardFactor * grid.grid.sum().toDouble()
    }

    override fun isTerminal(): Boolean {
        // for now let this never end!
        return false
    }

    override fun nTicks(): Int {
        return nTicks
    }

    override fun totalTicks(): Long {
        return totalTicks
    }

    override fun resetTotalTicks() {
        totalTicks = 0;
    }



    fun addData(grid: Grid, next: Grid, actions: IntArray, data: ArrayList<Pattern>) {
        data.clear()

        val off = 0
        val on = 1

        // make a gridGame for the inputs
        // set them all to zero apart from where the actions are being played
        val inputs = grid.copy()
        inputs.setAll(off)

        for (action in actions)
            if (action != doNothingAction())
                inputs.setCell(action, on)


        for (i in 0 until grid.w) {
            for (j in 0 until grid.h) {
                val p = Pattern(vectorExtractor(grid, i, j), next.getCell(i, j))
                when (includeNeighbourInputs) {
                    InputType.PlayerInt -> p.ip.add(getActionInt(inputs, i, j))
                    InputType.PlayerOneHot -> p.ip.addAll(vectorExtractor(inputs, i, j))
                    // the clear() option is to run a sanity check that codes only the actions
                    // p.ip.clear()
                }
                if (learner != null) {
                    learner.add(p.ip, p.op)
                }
                if (harvestData) data.add(p)
            }
        }
    }
}


interface UpdateRule {
    fun cellUpdate(grid: Grid, x: Int, y: Int): Int
}

interface NeighbourSumFunction {
    fun next(centre: Int, sum: Int): Int
}

fun gameOfLife(centre: Int, nSum: Int): Int {
    if (centre == 1)
        return if (nSum < 2 || nSum > 3) 0 else 1
    else
        return if (nSum == 3) 1 else 0
}

fun hardLife(centre: Int, nSum: Int): Int {
    if (centre == 1)
        return if (nSum < 3 || nSum > 4) 0 else 1
    else
        return if (nSum == 3) 1 else 0
}


class MyRule : UpdateRule {


    var next = ::gameOfLife

//    override fun next(centre: Int, sum: Int): Int {
//        if (centre == 1)
//            return if (sum < 3 || sum > 4) 0 else 1
//        else
//            return if (sum == 3) 1 else 0
//    }

    override fun cellUpdate(grid: Grid, x: Int, y: Int): Int {
        var sum = 0
        for (xx in x - 1..x + 1) {
            for (yy in y - 1..y + 1) {
                if (!(xx == x && yy == y)) {
                    sum += grid.getCell(xx, yy)
                    // println(gridGame.getCell(xx, yy))
                }
            }
        }
        return next(grid.getCell(x, y), sum)
    }
}

fun vectorExtractor(grid: Grid, x: Int, y: Int): ArrayList<Int> {
    val v = ArrayList<Int>()
    for (xx in x - 1..x + 1) {
        for (yy in y - 1..y + 1) {
            v.add(grid.getCell(xx, yy))
        }
    }
    return v
}

fun getActionInt(grid: Grid, x: Int, y: Int): Int {
    var v = 0
    for (xx in x - 1..x + 1) {
        for (yy in y - 1..y + 1) {
            if (grid.getCell(xx, yy)==1) return v
            else v++
        }
    }
    return v
}

// fast updating only increases speed by about 20% therefore not worth the effort

class FastUpdate {
    val index: ArrayList<IntArray> = ArrayList()
    val nNeighbours = 9

    constructor (grid: Grid) {
        for (i in 0 until grid.grid.size) {
            // println(Arrays.toString(index[i]))
            index.add(computeIndex(grid, i))
        }
    }

    fun computeIndex(grid: Grid, i: Int): IntArray {
        // for now compute them with wrap
        val lut = IntArray(nNeighbours)
        var x = i % grid.w
        var y = i / grid.w
        // now wrap

        var j = 0
        for (xx in x - 1..x + 1) {
            for (yy in y - 1..y + 1) {
                // wrap the gridGame position
                val ix = (xx + grid.w) % grid.w
                val iy = (yy + grid.h) % grid.h
                lut[j] = ix + grid.w * iy
                j++
            }
        }
        // println(Arrays.toString(lut))
        return lut
    }
}


