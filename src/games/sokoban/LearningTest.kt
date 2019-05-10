package games.sokoban

import agents.SimpleEvoAgent
import ggi.SimplePlayerInterface
import utilities.ElapsedTimer
import utilities.StatSummary

var printErrors = false
var printDetails = false

fun main() {
    val nGames = 100
    val useLearnedModel = true
    val pretrainModel = true
    val span = 2
    val maxSteps = 100

    //define model and agent
    val gridIterator = CrossGridIterator(2)
    val dtm = DTModel(gridIterator)
    if (pretrainModel) ModelTrainer().trainModel(dtm)

    val agent: SimplePlayerInterface = SimpleEvoAgent(
            useMutationTransducer = false, sequenceLength = 40, nEvals = 50,
//            discountFactor = 0.999,
            flipAtLeastOneValue = false,
            probMutation = 0.2)
    println("total analysed patter ${dtm.totalAnalysedPatterns}")
    //run agent test
    val timer = ElapsedTimer()
    val tester = AgentTesterDT(maxSteps, useLearnedModel)
    val ss = tester.runModelTests(nGames, agent, dtm)
    val elapsed = timer.elapsed()

    //report stats
    println("total game ticks     = %e".format( Sokoban().totalTicks().toDouble()))
    println("learned model ticks  = %e".format( dtm.totalTicks().toDouble()))
    println("mTicks/s for game    = %.2f".format( Sokoban().totalTicks().toDouble() * 1e-3 / elapsed))
    println("mTicks/s for model   = %.2f".format( dtm.totalTicks().toDouble() * 1e-3 / elapsed))
}

class AgentTesterDT(private val maxSteps: Int  = 1000, private val useLearnedModel: Boolean = true) {

    fun runModelTests(n: Int, agent: SimplePlayerInterface, lfm: ForwardGridModel): StatSummary {
        val ss = StatSummary("Sokoban scores")
        for (i in 0 until n) {
            println("Running game $i")
            val score = runModelGame(agent, lfm)
            ss.add(score)
            //println()
        }
        //println(ss)
        //println()
        return ss
    }

    private fun runModelGame(agent: SimplePlayerInterface, fm: ForwardGridModel): Double {
        val game = Sokoban()
        val actions = intArrayOf(0, 0)
        var i = 0
        var gameOver = false

        while (i < maxSteps && !gameOver) {
            //Take and execute actions
            // for direct comparisons allow switching between the learned forward model
            // and other options
            if (useLearnedModel) {
                // fm.setGridArray((game.copy() as Sokoban).board.grid, game.board.playerX, game.board.playerY)
                fm.setGrid((game.copy() as Sokoban).board.getSimpleGrid())

                actions[0] = agent.getAction(fm, Constants.player1)
            } else {
                actions[0] = agent.getAction(game.copy(), Constants.player1)
            }

            game.next(actions)
            gameOver = game.isTerminal()
            i++
        }
        //println(game.score())
        return game.score()
    }
}

