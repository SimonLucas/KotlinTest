package games.simplegridgame

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

import forwardmodels.modelinterface.ForwardModelTrainerSimpleGridGame
import games.gridgame.*
import games.gridgame.InputType

// todo: need to fix the problem with this - definitely some error
// the one-step prediction test fails, so no surprise that it fails later ...

var harvestData = true

enum class InputType {
    None, PlayerInt, PlayerOneHot
}

// set this to null to turn off learning
var learner = StatLearner()

fun main(args: Array<String>) {

    var game = SimpleGridGame(10, 10)
    (game.updateRule as MyRule).next = ::generalSumUpdate

    game.rewardFactor = 1.0;
    // game.setFast(true)
    println(game.grid)
    val gv = GridView(game.grid)
    val frame = JEasyFrame(gv, "Life Game")
    val actions = intArrayOf(0, 0)
    var agent1: SimplePlayerInterface = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 5, nEvals = 40)
    var agent2: SimplePlayerInterface = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 5, nEvals = 10)
    // agent1 = RandomAgent()
    // agent1 = DoNothingAgent(game.doNothingAction())
    agent2 = DoNothingAgent(game.doNothingAction())

    var decisionTree : DecisionTree? = null
    val modelTrainer = ForwardModelTrainerSimpleGridGame(InputType.Simple)

    val nSteps = 2000
    for (i in 0 until nSteps) {
        actions[0] = agent1.getAction(game.copy(), Constants.player1)
        actions[1] = agent2.getAction(game.copy(), Constants.player2)
        game.next(actions)

        //gv.grid = game.grid
        //gv.repaint()
        //Thread.sleep(50)
        frame.title = "tick = ${game.nTicks}, score = ${game.score()}"
        // System.exit(0)
        //println("$i\t N distinct patterns learned = ${learner.lut.size}")

        decisionTree = modelTrainer.trainModel(data) as DecisionTree
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

data class SimplePattern(val ip: ArrayList<Int>, val op: Int)


val data = ArrayList<SimplePattern>()

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

var totalTicks: Long = 0

open class SimpleGridGame : ExtendedAbstractGameState {
    override fun randomInitialState(): AbstractGameState {
        grid.grid = grid.randomGrid()
        return this
    }

    var updateRule: UpdateRule = MyRule()
    var grid: Grid = Grid()
    var nTicks = 0

    // negate this to reward destroying life
    var rewardFactor = 1.0

    constructor(w: Int = 20, h: Int = 20) {
        grid = Grid(w, h)
    }

    fun doNothingAction() = grid.grid.size

    override fun copy(): AbstractGameState {
        val gridGame = SimpleGridGame()
        gridGame.nTicks = nTicks
        gridGame.grid = grid.deepCopy()
        gridGame.updateRule = updateRule
        gridGame.rewardFactor = rewardFactor
        return gridGame
    }

    override fun next(actions: IntArray): AbstractGameState {
        for (action in actions)
            if (action != doNothingAction())
                grid.invertCell(action)

        // capture the local grid pattern input
        val gridCopy = grid.copy()

        for (i in 0 until grid.w) {
            for (j in 0 until grid.h) {
                gridCopy.setCell(i, j, updateRule.cellUpdate(grid, i, j))
            }
        }

        if (harvestData && learner != null) addData(grid, gridCopy, data)

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
        totalTicks = 0
    }



    // in this version the actions have already been applied to the grid,
    // so there is no separate coding of the actions array
    fun addData(grid: Grid, next: Grid, data: ArrayList<SimplePattern>) {
        data.clear()
        for (i in 0 until grid.w) {
            for (j in 0 until grid.h) {
                val p = SimplePattern(vectorExtractor(grid, i, j), next.getCell(i, j))
                if (learner != null) {
                    learner.add(p.ip, p.op)
                }
                if (harvestData) data.add(p)
            }
        }
    }

    fun lifeRule(ip: ArrayList<Int>) : Int {
        assert(ip.size == 9)
        // find total excluding the centre
        var tot = ip.sum() - ip.get(4)
        if (ip.get(4) == 0) {
            return if (tot == 3) 1 else 0
        } else {
            return if (tot == 2 || tot == 3) 1 else 0
        }
    }
    fun caveRule(ip: ArrayList<Int>) : Int {
        assert(ip.size == 9)
        // find total excluding the centre
        return if (ip.sum() > 4) 1 else 0
    }
}


