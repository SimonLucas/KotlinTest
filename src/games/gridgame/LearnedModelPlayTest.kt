package games.gridgame

import agents.DoNothingAgent
import agents.SimpleEvoAgent
import forwardmodels.decisiontree.DecisionTree
import forwardmodels.modelinterface.ForwardModelTrainerSimpleGridGame
import ggi.AbstractGameState
import ggi.SimplePlayerInterface
import utilities.JEasyFrame
import views.GridView

fun main(args: Array<String>) {

    val harvestData = true

    var game = GridGame(30, 30, harvestData).setFast(false)
    var decisionTree : DecisionTree? = null

    (game.updateRule as MyRule).next = ::generalSumUpdate

    game.rewardFactor = 1.0
    // game.setFast(true)
    println(game.grid)
    val gv = GridView(game.grid)
    val frame = JEasyFrame(gv, "Life Game")
    val actions = intArrayOf(0, 0)
    var agent1: SimplePlayerInterface = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 5, nEvals = 10)
    var agent2: SimplePlayerInterface = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 5, nEvals = 10)
    // agent1 = RandomAgent()
    //agent1 = DoNothingAgent(game.doNothingAction())
    agent2 = DoNothingAgent(game.doNothingAction())

    val modelTrainer = ForwardModelTrainerSimpleGridGame(InputType.PlayerInt)

    val nSteps = 200
    for (i in 0 until nSteps) {
        if (decisionTree != null) {
            val modelcopy : FMGridGame = FMGridGame(game, decisionTree)

            actions[0] = agent1.getAction(modelcopy.copy(), Constants.player1)
            actions[1] = agent2.getAction(modelcopy.copy(), Constants.player2)
        } else {
            actions[0] = agent1.getAction(game.copy(), Constants.player1)
            actions[1] = agent2.getAction(game.copy(), Constants.player2)
        }

        game.next(actions)

        gv.repaint()
        Thread.sleep(50)
        frame.title = "tick = ${game.nTicks}, score = ${game.score()}"
        // System.exit(0)
        // game = game.copy() as GridGame
        // println(game.updateRule.next)
        println("$i\t N distinct patterns learned = ${learner.lut.size}")

        //print("Update tree ...")
        //decisionTree = modelTrainer.trainModel(data) as DecisionTree
        //println(" done")
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


class FMGridGame : GridGame {

    var forwardModel : DecisionTree = DecisionTree()

    constructor(game: GridGame, model: DecisionTree) {
        this.nTicks = game.nTicks
        this.grid = game.grid.deepCopy()
        this.fastUpdate = game.fastUpdate
        this.updateRule = game.updateRule
        this.rewardFactor = game.rewardFactor

        forwardModel = model
    }

    constructor(game: FMGridGame) {
        this.nTicks = game.nTicks
        this.grid = game.grid.deepCopy()
        this.fastUpdate = game.fastUpdate
        this.updateRule = game.updateRule
        this.rewardFactor = game.rewardFactor
        this.forwardModel = game.forwardModel
    }

    override fun copy(): AbstractGameState {
        val fmGridGame = FMGridGame(this)
        return fmGridGame
    }

    override fun next(actions: IntArray): AbstractGameState {

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
                    gridCopy.setCell(i, j, cellUpdate(grid, i, j, actions))
                }
            }
        }

        if (harvestData || learner!=null) addData(grid, gridCopy, actions, data)

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

        var response = forwardModel.classify(p)
        if (response=="0")
            return 0
        if (response=="1")
            return 1
        //Todo: what to do if an instance cannot be classified?
        return 0
    }
}

