package games.sokoban

import agents.RandomAgent
import agents.SimpleEvoAgent
import ggi.SimplePlayerInterface
import utilities.ElapsedTimer
import utilities.JEasyFrame
import utilities.StatSummary


fun main() {

    val nSteps = 10

    val span = 2

    var game = Sokoban()
    val gatherer = GatherData(span)

    var lfm = LocalForwardModel(gatherer.tileData, gatherer.rewardData, span)

    val t = ElapsedTimer()
    var agent: SimplePlayerInterface = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 100, nEvals = 100)
    // agent = RandomAgent()

    // learn a forward model

    runModelTests(nSteps, agent, lfm)
    val elapsed = t.elapsed()

    println(t)
    println("total game ticks  = %e".format( Sokoban().totalTicks().toDouble()))
    println("mTicks per second = %.2f".format( Sokoban().totalTicks().toDouble() * 1e-3 / elapsed))

}

fun runModelTests(n: Int, agent: SimplePlayerInterface, lfm: LocalForwardModel) : StatSummary {
    val ss = StatSummary("Sokoban scores")
    for (i in 0 until n) {
        // println("Running game $i")
        val score = runModelGame(agent, lfm)
        ss.add(score)
    }
    println(ss)
    return ss
}

fun runModelGame(agent: SimplePlayerInterface, lfm: LocalForwardModel) : Double {
    var game = Sokoban()
    val actions = intArrayOf(0, 0)
    var i = 0
    var gameOver = false

    while (i < nSteps && !gameOver)
    {

        // set the current state up in the Learned Forward Model
        lfm.setGrid(game.board.grid, game.board.playerX, game.board.playerY)


        //Take and execute actions
        actions[0] = agent.getAction(lfm, Constants.player1)
        game.next(actions)
        gameOver = game.isTerminal()
        i++
    }
    return game.score()
}
