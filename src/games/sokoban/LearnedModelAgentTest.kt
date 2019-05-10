package games.sokoban

import agents.RandomAgent
import agents.SimpleEvoAgent
import games.simplegridgame.hyper.SimpleEvoFactorySpace
import games.simplegridgame.hyper.SimpleEvoParams
import ggi.SimplePlayerInterface
import utilities.ElapsedTimer
import utilities.StatSummary


fun main() {

    val nGames = 100
    val useLearnedModel = false
    val dummySpeedTest = false
    val span = 0
    val maxSteps = 100
    val gatherer = GatherData(span)

    var lfm: ForwardGridModel = LocalForwardModel(gatherer.tileData, gatherer.rewardData, CrossGridIterator(2), dummySpeedTest)
    // lfm = GPModel()
    lfm = DummyForwardModel()
    val t = ElapsedTimer()
    var agent: SimplePlayerInterface = SimpleEvoAgent(
            useMutationTransducer = false, sequenceLength = 40, nEvals = 50,
//            discountFactor = 0.999,
            flipAtLeastOneValue = false,
            probMutation = 0.2)

//    val afs = SimpleEvoFactorySpace().setSearchSpace(SimpleEvoParams())
//    val agent = afs.agent(intArrayOf(1, 1, 4, 4, 1, 0, 3, 1))

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

    fun runModelTests(n: Int, agent: SimplePlayerInterface, lfm: ForwardGridModel): StatSummary {
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

    fun runModelGame(agent: SimplePlayerInterface, lfm: ForwardGridModel): Double {
        var game = Sokoban(1)
        val actions = intArrayOf(0, 0)
        var i = 0
        var gameOver = false

        while (i < maxSteps && !gameOver) {

            // set the current state up in the Learned Forward Model
            // lfm.setGridArray((game.copy() as Sokoban).board.grid, game.board.playerX, game.board.playerY)
            lfm.setGrid((game.copy() as Sokoban).board.getSimpleGrid())


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
