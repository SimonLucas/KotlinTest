package games.simplegridgame

import agents.DoNothingAgent
import agents.SimpleEvoAgent
import forwardmodels.decisiontree.DecisionTree
import forwardmodels.modelinterface.ForwardModelTrainerSimpleGridGame
import games.gridgame.*
import games.gridgame.Constants
import games.gridgame.InputType
import games.gridgame.StatLearner
import ggi.AbstractGameState
import ggi.SimplePlayerInterface
import utilities.JEasyFrame
import utilities.LearningStats
import views.GridView
import agents.RandomAgent
import java.util.*

val fakeRespone = false

fun main(args: Array<String>) {
    val harvestData = true

    val nSteps = 100
    val runs = 10

    var game = SimpleGridGame(10, 10)
    (game.updateRule as MyRule).next = ::generalSumUpdate

    game.rewardFactor = 1.0
    // game.setFast(true)
    println(game.grid)
    val gv = GridView(game.grid)
    val frame = JEasyFrame(gv, "Life Game")
    val actions = intArrayOf(0, 0)
    var agent1: SimplePlayerInterface = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 5, nEvals = 40)
    var agent2: SimplePlayerInterface = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 5, nEvals = 10)
    //agent1 = RandomAgent()
    agent1 = DoNothingAgent(game.doNothingAction())
    agent2 = DoNothingAgent(game.doNothingAction())


    val agents : LinkedList<SimplePlayerInterface> = LinkedList()
    agents.add(SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 5, nEvals = 40))
    agents.add(DoNothingAgent(game.doNothingAction()))
    agents.add(RandomAgent())
    val filenames : LinkedList<String> = LinkedList()
    filenames.add("EvoAgent")
    filenames.add("DoNothingAgent")
    filenames.add("RandomAgent")

    for (x in 0 until agents.size){
        agent1 = agents.get(x)
        for (y in 0 until runs){
            game = SimpleGridGame(10, 10)
            val learningStats : LearningStats? = LearningStats(nSteps)
            val modelTrainer = ForwardModelTrainerSimpleGridGame(InputType.Simple)
            var decisionTree : DecisionTree? = null
            for (i in 0 until nSteps) {
                if (decisionTree != null) {
                    val modelcopy: FMSimpleGridGame = FMSimpleGridGame(game, decisionTree)

                    actions[0] = agent1.getAction(modelcopy.copy(), Constants.player1)
                    actions[1] = agent2.getAction(modelcopy.copy(), Constants.player2)
                } else {
                    actions[0] = agent1.getAction(game.copy(), Constants.player1)
                    actions[1] = agent2.getAction(game.copy(), Constants.player2)
                }

                game.next(actions)

                gv.grid = game.grid
                gv.repaint()
                Thread.sleep(50)
                frame.title = "tick = ${game.nTicks}, score = ${game.score()}"
                // System.exit(0)
                // game = game.copy() as GridGame
                // println(game.updateRule.next)
                //println("$i\t N distinct patterns learned = ${games.gridgame.learner.lut.size}")

                //print("Update tree ...")
                decisionTree = modelTrainer.trainModel(data) as DecisionTree

                learningStats?.storeTick(i, modelTrainer.nrOfKnownPatterns(), modelTrainer.measureAccuracy(), game.score())
            }
            learningStats?.writeToFile("evaluation/" + filenames.get(x) + "_" + y + ".csv")
        }
    }


    val learner = StatLearner()
    if (harvestData) {
        val set = HashSet<Pattern>()
        val input = HashSet<ArrayList<Int>>()
        for (p in games.gridgame.data) {
            // println(p)
            set.add(p)
            input.add(p.ip)
            // learner.add(p.ip, p.op)
        }
        // println("\nUnique IP / OP pairs:")
        // set.forEach { println(it) }
        // println("\nUnique Inputs:")
        // input.forEach { println(it) }
        println("\nN Patterns  =  " + games.gridgame.data.size)
        println("Unique size = " + set.size)
        println("Unique ips  = " + input.size)

        // learner.report()

    }
}


class FMSimpleGridGame : SimpleGridGame {

    var forwardModel : DecisionTree = DecisionTree()

    constructor(game: SimpleGridGame, model: DecisionTree) {
        this.nTicks = game.nTicks
        this.grid = game.grid.deepCopy()
        this.updateRule = game.updateRule
        this.rewardFactor = game.rewardFactor

        forwardModel = model
    }

    constructor(game: FMSimpleGridGame) {
        this.nTicks = game.nTicks
        this.grid = game.grid.deepCopy()
        this.updateRule = game.updateRule
        this.rewardFactor = game.rewardFactor
        this.forwardModel = game.forwardModel
    }

    override fun copy(): AbstractGameState {
        val fmGridGame = FMSimpleGridGame(this)
        return fmGridGame
    }

    override fun next(actions: IntArray): AbstractGameState {
        for (action in actions)
            if (action != doNothingAction())
                grid.invertCell(action)

        // capture the local grid pattern input
        val gridCopy = grid.copy()

        for (i in 0 until grid.w) {
            for (j in 0 until grid.h) {
                gridCopy.setCell(i, j, cellUpdate(grid, i, j, actions))
            }
        }

        grid = gridCopy

        totalTicks++
        nTicks++
        return this
    }

    fun cellUpdate(grid: Grid, x: Int, y: Int, actions: IntArray): Int {

        val off = 0
        val on = 1
        val inputs = grid.copy()
        inputs.setAll(off)

        for (action in actions)
            if (action != doNothingAction())
                inputs.setCell(action, on)

        val p = Pattern(vectorExtractor(grid, x, y), -1)

        when (includeNeighbourInputs) {
            InputType.PlayerInt -> p.ip.add(getActionInt(inputs, x, y))
            InputType.PlayerOneHot -> p.ip.addAll(vectorExtractor(inputs, x, y))
            // the clear() option is to run a sanity check that codes only the actions
            // p.ip.clear()
        }
        if (fakeRespone)
            return 0
        else {
            var response = forwardModel.classify(p)
            if (response == "0")
                return 0
            if (response == "1")
                return 1
        }
        //Todo: what to do if an instance cannot be classified? (should never happen)
        return 0
    }
}

