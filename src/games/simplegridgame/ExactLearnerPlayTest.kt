package games.simplegridgame

import agents.DoNothingAgent
import agents.RandomAgent
import agents.SimpleEvoAgent
import games.gridgame.MyRule
import games.gridgame.UpdateRule
import games.simplegridgame.fdc.LifeFun
import games.simplegridgame.fdc.TruthTableRule
import ggi.SimplePlayerInterface
import utilities.ElapsedTimer
import utilities.JEasyFrame
import utilities.StatSummary
import views.GridView
import java.awt.Color
import java.awt.FlowLayout
import java.util.*
import javax.swing.JComponent

// this test is to see how well we play when a learned model
// is used as a substitute for the true forward model

// step 1: train the agent for a number of steps
// step 2: run a number of games using its forward model

// learnSteps is the number of simulation ticks (state transitions)
// to run the model for to collect the training data
// however, there is a bug in that SimpleEvoAgent will run
// many simulations and the data harvester by default will
// collect data from all of them!

val learnSteps = 5
val testSteps = 100
val gamesPerEval = 1
val nPredictionTests = 30
val w = 30
val h = 30
val visual = true
val lutSizeLimit = 0
val diceRoll = false
val rewardFactor = 1.0

// made an update with


fun main() {

    val t = ElapsedTimer()

    val nReps = 3

    val lutSizes = 480 .. 512 step 32
    println(lutSizes)
    val results = TreeMap<Int,StatSummary>()
    for (lut in lutSizes) {
        println("Getting results for lut size: $lut")
        val ss = StatSummary()
        for (i in 0 until nReps)
            ss.add(trainAndPlay(lut))
        results.put(lut,ss)
        println(ss)
        println()
    }

    // now format the results

    results.forEach{key, value -> println("$key\t %.1f\t %.1f".format(value.mean(), value.stdErr()))}



    println("Total time: " + t)
}



fun trainAndPlay(lutSizeLimit: Int) : StatSummary {

    var game = SimpleGridGame(w, h)
    // (game.updateRule as MyRule).next = ::generalSumUpdate
    //
    //
    // game.updateRule = LifeUpdateRule()

    // game.updateRule = CaveUpdateRule()
    // game.updateRule =
    learner = StatLearner()
    game.rewardFactor = rewardFactor
    learner.lutSizeLimit = lutSizeLimit
    learner.diceRoll = diceRoll

    // var agent1: SimplePlayerInterface = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 3, nEvals = 50)
    var agent1: SimplePlayerInterface = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 25, nEvals = 5)
    var agent2: SimplePlayerInterface = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 5, nEvals = 10)
    // agent1 = RandomAgent()
    // agent1 = DoNothingAgent(game.doNothingAction())
    agent2 = DoNothingAgent(game.doNothingAction())

    // first of all train the learner

    harvestData = true

    println("Training")
    val t = ElapsedTimer()
    for (i in 0 until learnSteps) {
        val actions = intArrayOf(0, 0)
        actions[0] = agent1.getAction(game.copy(), Constants.player1)
        actions[1] = agent2.getAction(game.copy(), Constants.player2)
        game.next(actions)
        println("$i\t N distinct patterns learned = ${learner.lut.size}")
    }

    val learnedTTR = TruthTableRule().setRule(learner)
    val model = TruthTableRule().setRule(LifeFun())

    println("TTR Distance from true model = ${model.distance(learnedTTR )}")

    // learner.reportComparison()
    println(t)

    // todo: fix the error in the way the learner learns or is applied
    // even when trained with DoNothing agents and it sees ALL the patterns,
    harvestData = false

    // predictionTest(learner)

    // System.exit(0)

    println("Testing")
    val ss = runGames(agent1, learner, visual)
    println(ss)

    // learner.reportComparison()

    println(t)



    return ss

}




fun runGames(agent: SimplePlayerInterface, learnedRule: UpdateRule, visual: Boolean): StatSummary {
    val ss = StatSummary()
    for (i in 0 until gamesPerEval) {
        val game = SimpleGridGame(w, h)
        game.rewardFactor = rewardFactor

        game.updateRule = CaveUpdateRule()
        // game.updateRule = LifeUpdateRule()

        // game.updateRule = learnedRule

        val gridView = GridView(game.grid)
        if (visual) {
            JEasyFrame(gridView, "Grid Game")
        }
        for (i in 0 until testSteps) {
            val actions = intArrayOf(0, 0)

            val agentCopy = game.copy() as SimpleGridGame
            if (learnedRule != null) {
                agentCopy.updateRule = learnedRule
                // game.updateRule = learnedRule
            }

            // need to know how good this is
            actions[0] = agent.getAction(agentCopy, Constants.player1)

            // actions[0] = RandomAgent().getAction(agentCopy, Constants.player1)

            // println("Selected action: ${actions[0]}")

            // play against a DoNothing opponent for now...
            actions[1] = DoNothingAgent().getAction(game.copy(), Constants.player2)
            game.next(actions)
            // println(game.score())
            if (visual) {
                gridView.grid = game.grid
                gridView.repaint()
                Thread.sleep(100)
            }

        }
        println("Game: ${i+1}, score = ${game.score()}")
        ss.add(game.score())
    }
    return ss
}


fun predictionTest(learnedRule: UpdateRule) {
    // test and checking the differences a number of times
    val ss = StatSummary("Prediction errors")
    for (i in 0 until nPredictionTests) {
        val game = SimpleGridGame(w, h)
        val other = game.copy() as SimpleGridGame
        other.updateRule = learnedRule
        // game.updateRule = learnedRule
        game.updateRule = LifeUpdateRule()

        val agent = DoNothingAgent()

        // see if the two grids end in the same state
        val action = agent.getAction(game.copy(), 0)
        val actions = intArrayOf(action, action)

        // now see what goes next... one with learned rule, one with not

        game.next(actions)
        other.next(actions)
        val diff = game.grid.difference(other.grid)
        println("Test ${i}, \t diff = ${diff}")
        ss.add(diff)

    }
    println(ss)
}

