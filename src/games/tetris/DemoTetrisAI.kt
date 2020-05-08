package games.tetris

import agents.PolicyEvoAgent
import agents.RandomAgent
import agents.SimpleEvoAgent
import games.gridworld.PairView
import ggi.AbstractValueFunction
import ggi.SimplePlayerInterface
import utilities.JEasyFrame
import utilities.StatSummary
import views.EasyPlot


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

