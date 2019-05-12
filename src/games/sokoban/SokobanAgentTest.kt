package games.sokoban

import agents.RandomAgent
import agents.SimpleEvoAgent
import ggi.SimplePlayerInterface
import utilities.ElapsedTimer
import utilities.StatSummary

val nSteps = 1000

fun main() {

    val t = ElapsedTimer()
    var agent: SimplePlayerInterface = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 100, nEvals = 100)
    agent = RandomAgent()
    testModels(100, agent)
    val elapsed = t.elapsed()

    println(t)
    println("total game ticks  = %e".format( Sokoban().totalTicks().toDouble()))
    println("mTicks per second = %.2f".format( Sokoban().totalTicks().toDouble() * 1e-3 / elapsed))

}

fun testModels(n: Int, agent: SimplePlayerInterface) : StatSummary {
    val ss = StatSummary("Sokoban scores")
    for (i in 0 until n) {
        // println("Running game $i")
        val score = runGame(agent)
        ss.add(score)
    }
    println(ss)
    return ss
}

fun runGame(agent: SimplePlayerInterface) : Double {
    var game = Sokoban()
    val actions = intArrayOf(0, 0)
    var i = 0
    var gameOver = false

    while (i < nSteps && !gameOver)
    {
        //Take and execute actions
        actions[0] = agent.getAction(game.copy(), Constants.player1)
        game.next(actions)
        gameOver = game.isTerminal()
        i++
    }
    return game.score()
}
