package games.gridgame

import agents.DoNothingAgent
import agents.RandomAgent
import agents.SimpleEvoAgent
import decisiontree.com.machine.learning.decisiontrees.DecisionTree
import decisiontree.test.ForwardModelTrainer
import ggi.SimplePlayerInterface
import utilities.JEasyFrame
import views.GridView

fun main(args: Array<String>) {
    var game = GridGame(30, 30).setFast(false)
    var decisionTree : DecisionTree

    game.updateRule.next = ::generalUpdate

    game.rewardFactor = 1.0;
    // game.setFast(true)
    println(game.grid)
    val gv = GridView(game)
    val frame = JEasyFrame(gv, "Life Game")
    val actions = intArrayOf(0, 0)
    var agent1: SimplePlayerInterface = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 5, nEvals = 40)
    var agent2: SimplePlayerInterface = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 5, nEvals = 10)
    agent1 = RandomAgent()
    // agent1 = DoNothingAgent(game.doNothingAction())
    agent2 = DoNothingAgent(game.doNothingAction())


    val nSteps = 20
    for (i in 0 until nSteps) {
        actions[0] = agent1.getAction(game.copy(), Constants.player1)
        actions[1] = agent2.getAction(game.copy(), Constants.player2)
        game.next(actions)

        gv.repaint()
        Thread.sleep(50)
        frame.title = "tick = ${game.nTicks}, score = ${game.score()}"
        // System.exit(0)
        // game = game.copy() as GridGame
        // println(game.updateRule.next)
        println("$i\t N distinct patterns learned = ${learner.lut.size}")

        decisionTree = ForwardModelTrainer.trainDecisionTree(data) as DecisionTree

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
