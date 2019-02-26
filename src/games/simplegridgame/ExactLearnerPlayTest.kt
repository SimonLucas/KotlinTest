package games.simplegridgame

import agents.DoNothingAgent
import agents.SimpleEvoAgent
import games.gridgame.MyRule
import games.gridgame.UpdateRule
import ggi.SimplePlayerInterface
import utilities.ElapsedTimer
import utilities.StatSummary

// this test is to see how well we play when a learned model
// is used as a substitute for the true forward model

// step 1: train the agent for a number of steps
// step 2: run a number of games using its forward model


val learnSteps = 3
val testSteps = 100
val gamesPerEval = 1
val w = 20
val h = 20

fun main(args: Array<String>) {

    var game = SimpleGridGame(w, h)
    (game.updateRule as MyRule).next = ::generalSumUpdate
    game.rewardFactor = 1.0;

    var agent1: SimplePlayerInterface = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 5, nEvals = 20)
    var agent2: SimplePlayerInterface = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 5, nEvals = 10)
    // agent1 = RandomAgent()
    agent1 = DoNothingAgent(game.doNothingAction())
    agent2 = DoNothingAgent(game.doNothingAction())

    // first of all train the learner

    println("Training")
    val t = ElapsedTimer()
    for (i in 0 until learnSteps) {
        val actions = intArrayOf(0, 0)
        actions[0] = agent1.getAction(game.copy(), Constants.player1)
        actions[1] = agent2.getAction(game.copy(), Constants.player2)
        game.next(actions)
        println("$i\t N distinct patterns learned = ${learner.lut.size}")
    }
    println(t)

    // todo: fix the error in the way the learner learns or is applied
    // even when trained with DoNothing agents and it sees ALL the patterns,
    predictionTest(learner)


    println("Testing")
    val ss = runGames(agent1, learner)


    println(ss)

    println(t)

    learner.report()
}

fun predictionTest(learnedRule: UpdateRule) {

    // test and checking the differences a number of times

    val ss = StatSummary("Prediction errors")
    for (i in 0 until 10) {
        val game = SimpleGridGame(w, h)
        val other = game.copy() as SimpleGridGame
        other.updateRule = learnedRule
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

fun runGames(agent: SimplePlayerInterface, learnedRule: UpdateRule): StatSummary {
    val ss = StatSummary()
    for (i in 0 until gamesPerEval) {
        var game = SimpleGridGame(w, h)
        for (i in 0 until testSteps) {
            val actions = intArrayOf(0, 0)

            val agentCopy = game.copy()
            if (learnedRule != null) {
                (agentCopy as SimpleGridGame).updateRule = learnedRule
            }

            actions[0] = agent.getAction(agentCopy, Constants.player1)
            // play against a DoNothing opponent for now...
            actions[1] = DoNothingAgent().getAction(game.copy(), Constants.player2)
            game.next(actions)
            println(game.score())

        }
        println("Game: ${i+1}, score = ${game.score()}")
        ss.add(game.score())
    }
    return ss
}
