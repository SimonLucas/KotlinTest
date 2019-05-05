package games.sokoban

import agents.RandomAgent
import agents.SimpleEvoAgent
import ggi.SimplePlayerInterface
import utilities.ElapsedTimer
import utilities.JEasyFrame
import utilities.StatSummary


fun main() {

    val nGames = 10
    val useLearnedModel = false
    val span = 2
    val maxSteps = 100
    val gatherer = GatherData(span)

    var lfm = LocalForwardModel(gatherer.tileData, gatherer.rewardData, span)
    val t = ElapsedTimer()
    var agent: SimplePlayerInterface = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 50, nEvals = 20)
    // agent = RandomAgent()

    // learn a forward model

    val tester = AgentTester(maxSteps, useLearnedModel)
    tester.runModelTests(nGames, agent, lfm)
    val elapsed = t.elapsed()

    println(t)
    println("total game ticks     = %e".format( Sokoban().totalTicks().toDouble()))
    println("learned model ticks  = %e".format( lfm.totalTicks().toDouble()))
    println("mTicks/s for game    = %.2f".format( Sokoban().totalTicks().toDouble() * 1e-3 / elapsed))
    println("mTicks/s for model   = %.2f".format( lfm.totalTicks().toDouble() * 1e-3 / elapsed))

}

class AgentTester(val maxSteps: Int  = 1000, val useLearnedModel: Boolean = true) {

    fun runModelTests(n: Int, agent: SimplePlayerInterface, lfm: LocalForwardModel): StatSummary {
        val ss = StatSummary("Sokoban scores")
        for (i in 0 until n) {
            println("Running game $i")
            val score = runModelGame(agent, lfm)
            ss.add(score)
            println()
        }
        println(ss)
        return ss
    }

    fun runModelGame(agent: SimplePlayerInterface, lfm: LocalForwardModel): Double {
        var game = Sokoban()
        val actions = intArrayOf(0, 0)
        var i = 0
        var gameOver = false

        while (i < maxSteps && !gameOver) {

            // set the current state up in the Learned Forward Model
            lfm.setGrid(game.board.grid, game.board.playerX, game.board.playerY)


            //Take and execute actions

            // for direct comparisons allow switching between the learned forward model
            // and other options

            if (useLearnedModel) {
                actions[0] = agent.getAction(lfm, Constants.player1)
            } else {
                actions[0] = agent.getAction(game.copy(), Constants.player1)
            }
            game.next(actions)
            gameOver = game.isTerminal()
            i++
        }
        println(game.score())
        return game.score()
    }
}
