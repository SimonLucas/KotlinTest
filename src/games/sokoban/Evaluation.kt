package games.sokoban

import agents.RandomAgent
import agents.SimpleEvoAgent
import ggi.SimplePlayerInterface
import utilities.ElapsedTimer
import utilities.StatSummary


fun main() {
    val pattern = CrossGridIterator(2)
    val maxSteps = 100

    //define model and agent
    val dtModel = DTModel(pattern, false)
    //val hsModel = HashSetModel(pattern)

    //run agent test
    val timer = ElapsedTimer()
    val tester = Evaluation(maxSteps)
    //tester.testPatternLearning(nGames, learnedModel)
    val elapsed = timer.elapsed()
    println("Time elapsed: $elapsed")
}

class Evaluation(private val maxSteps: Int  = 1000, private val useLearnedModel: Boolean = true) {

    fun testPatternLearning(n: Int, dtm: DTModel, hsm: ForwardGridModel) {
        val agent: SimplePlayerInterface = RandomAgent()

        for (i in 0 until 10) {
            println("Running game $i")
            evaluatePredictionErrors(i, agent, dtm, hsm)
        }
    }

    private fun evaluatePredictionErrors(levelindex: Int, agent: SimplePlayerInterface, dtm: DTModel, hsm: ForwardGridModel): Double {
        val game = Sokoban(levelindex)
        val actions = intArrayOf(0, 0)

        while (game.totalTicks() < maxSteps) {
            //Take and execute actions
            actions[0] = agent.getAction(game, Constants.player1)

            game.next(actions)
        }
        return game.score()
    }
}

