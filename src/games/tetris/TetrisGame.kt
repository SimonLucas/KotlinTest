package games.tetris

import agents.PolicyEvoAgent
import agents.RandomAgent
import agents.SimpleEvoAgent
import games.gridworld.PairView
import games.sokoban.SokobanConstants
// import games.tetris.TetrisGame.Actions
import games.tetris.TetrisModel.Companion.gameOverPenalty
import ggi.AbstractGameState
import ggi.AbstractValueFunction
import ggi.ExtendedAbstractGameState
import ggi.SimplePlayerInterface
import ggi.game.GeneralKeyController
import utilities.JEasyFrame
import utilities.StatSummary
import views.EasyPlot
import java.awt.event.KeyEvent
import kotlin.random.Random

// todo: Implement sub-goal MCTS version of Tetris?

// todo: Find bug that causes non-determinism - try it without the dropdown of any type


fun main() {
    TetrisModel.defaultCols = 10
    TetrisModel.defaultRows = 20
    TetrisModel.includeColumnDiffs = true
    TetrisModel.gameOverPenalty = 1000
    TetrisModel.cyclicBlockType = true
    TetrisModel.randomInitialRotation = false
    TetrisModel.randomShapeColours = false
    TetrisModel.gameOverPenalty = 0
    var tg = TetrisGame()
    var agent : SimplePlayerInterface = RandomAgent()

    var valueFunction: AbstractValueFunction? = TetrisValueFunction()
    valueFunction = ColHeightDiff()
    // valueFunction = null
    agent = PolicyEvoAgent(useMutationTransducer = true, discountFactor = 0.99, flipAtLeastOneValue = false,
            nEvals = 100, sequenceLength = 50, probMutation = 0.2, useShiftBuffer = true, policy = null,
            initUsingPolicy = 0.5,
            appendUsingPolicy = 0.5,
            mutateUsingPolicy = 0.5,
            valueFunction = valueFunction,
            analysePlans = false
    )
    // agent = SimpleEvoAgent(probMutation = 0.4, useMutationTransducer = false, discountFactor = 1.0, sequenceLength = 500, nEvals = 100)
    // agent = RandomAgent()
    // agent = TetrisKeyController()

    val tv = TetrisView(tg.tm, null)
    // val frame = JEasyFrame(tv, "Tetris")

    val scoreView = EasyPlot()

    val both = PairView(tv, scoreView.view)
    val frame = JEasyFrame(both, "Grid World Test")

    if (agent is TetrisKeyController) frame.addKeyListener(agent.keyListener)

    val range = StatSummary().add(-10).add(600)

    while (!tg.isTerminal()) {
        tg = tg.copy() as TetrisGame
        val action = agent.getAction(tg.copy(), 0)
        if (action == Actions.Drop.ordinal) println("Drop at tick ${tg.nTicks}")
        tg.next(intArrayOf(action))

        tv.tm = tg.tm
        tv.repaint()
        var score = tg.score()
        if (valueFunction != null) score += valueFunction?.value(tg)
        val message = "${tg.nTicks}\t $score\t $action\t ${tg.totalTicks()}\t ${tg.subGoal()}"
        // println(message)
        frame.title = message
        if (agent is PolicyEvoAgent || agent is SimpleEvoAgent) {
            scoreView.update(agent.scores, ssy = range)
        }
        Thread.sleep(50)
    }
}


class TetrisValueFunction : AbstractValueFunction {
    companion object {
        val rand = Random
        val eps = 1e-10
        val cellFactor = 0.01
    }
    override fun value(gameState: AbstractGameState): Double {
        val noise = rand.nextDouble() * eps
        if (!(gameState is TetrisGame)) return noise
        val a = gameState.tm.a
        var hScore = 0.0
        for (i in 0 until a.size) {
            for (j in 0 until a[i].size) {
                if (a[i][j] != TetrisConstants.BG)
                    hScore += j * j * cellFactor
            }
        }
        return hScore
    }
}

class ColHeightDiff : AbstractValueFunction {
    companion object {
        val rand = Random
        val eps = 1e-10
    }
    override fun value(gameState: AbstractGameState): Double {
        val noise = rand.nextDouble() * eps
        if (!(gameState is TetrisGame)) return noise
        val a = gameState.tm.a
        val tm = (gameState as TetrisGame).tm
        val colHeights = Array<Int>(a.size){ tm.nRows }
        for (i in 0 until tm.nCols) {
            for (j in 0 until tm.nRows) {
                if (a[i][j] != TetrisConstants.BG) {
                    colHeights[i] = Math.min(colHeights[i], j)
                }
            }
        }
        // now sum the differences in heights
        var hScore = 0.0
        for (i in 1 until tm.nCols)
            hScore += Math.abs(colHeights[i-1] - colHeights[i])
        return -hScore
    }
}

enum class Actions { DoNothing, Left, Right, Rotate, Down, Drop}


class TetrisGame : ExtendedAbstractGameState {

    var nTicks = 0
    var tm = TetrisModel()

    // companion object is for a Singleton within this class
    // hence nTotalTicks is similar to a static declaration
    companion object {
        var nTotalTicks = 0L
        val xLeft = -1
        val xRight = 1
        val down = 1
        val cyclicBlockType = true
    }


    override fun totalTicks(): Long {
        return nTotalTicks
    }

    override fun resetTotalTicks() {
        nTotalTicks = 0
    }

    override fun randomInitialState(): AbstractGameState {
        // all initial states are random, so this is fine
        return this
    }

    override fun copy(): AbstractGameState {
        val tg = TetrisGame()
        tg.tm = tm.copy()
        tg.nTicks = nTicks
        return tg
    }

    val dropSkip = 4
    override fun next(actions: IntArray): AbstractGameState {
        // just going to take the first action

        takeAction(actions[0])

        // every so often drop it down anyway
        if (dropSkip > 0 && (nTicks % dropSkip) == 0)
            takeAction(Actions.Down.ordinal)

        // if (tm.tetronSprite == null) tm.newShape()
        nTicks++
        nTotalTicks++
        return this
    }

    fun takeAction(action: Int) {
        // reset this each time - it's used for a sub-goal check
        justLanded = false
        when (action) {
            Actions.DoNothing.ordinal -> {}
            Actions.Left.ordinal -> tm.move(xLeft, 0)
            Actions.Right.ordinal -> tm.move(xRight, 0)
            Actions.Rotate.ordinal -> tm.rotate()
            Actions.Down.ordinal -> {
                if (!tm.move(0, down)) {
                    tm.place()
                    justLanded = true
                    tm.checkRows()
                    tm.newShape()

                }
            }
            Actions.Drop.ordinal -> dropDown()
        }
    }

    var justLanded = false
    fun subGoal() : Boolean {
        return justLanded
    }



    //    private fun checkState() {
//        if (!tm.move(0, Controller.down)) {
//            tm.place()
//            tm.checkRows()
//            tm.tetronSprite = null
//        }
//    }
//
    private fun dropDown() {
        while (tm.move(0, down));
        tm.place()
        justLanded = true
        tm.checkRows()
        tm.newShape()
    }

    override fun nActions(): Int {
        return Actions.values().size
    }

    override fun score(): Double {
        val penalty = if (tm.gameOn()) 0 else gameOverPenalty
        val colDiff = if (TetrisModel.includeColumnDiffs) ColHeightDiff().value(this) else 0.0
        return tm.score.toDouble() - penalty + colDiff
    }

    override fun isTerminal(): Boolean {
        return !tm.gameOn()
    }

    override fun nTicks(): Int {
        return nTicks
    }
}


class TetrisKeyController : SimplePlayerInterface {

    override fun getAgentType(): String {
        return "BreakoutKeyController"
    }

    val keyMap: HashMap<Int, Int> =
            hashMapOf(KeyEvent.VK_LEFT to Actions.Left.ordinal,
                    KeyEvent.VK_UP to Actions.Rotate.ordinal,
                    KeyEvent.VK_RIGHT to Actions.Right.ordinal,
                    KeyEvent.VK_DOWN to Actions.Down.ordinal,
                    KeyEvent.VK_SPACE to Actions.Drop.ordinal)

    val keyListener = GeneralKeyController()

    constructor() {
        keyListener.keyMap = keyMap
    }

    // in fact all that needs doing in this class is to set up
    // the keyMap, so should just push everything to that general class
    override fun getAction(gameState: AbstractGameState, playerId: Int): Int {
        val action = keyListener.selectedAction
        keyListener.selectedAction = Actions.DoNothing.ordinal
        return action
    }

    override fun reset(): SimplePlayerInterface {
        return this
    }

}
