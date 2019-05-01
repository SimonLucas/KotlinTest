package games.sokoban

import agents.RandomAgent
import agents.SimpleEvoAgent
import ggi.SimplePlayerInterface
import utilities.ElapsedTimer
import utilities.JEasyFrame
import utilities.StatSummary

val nSteps = 1000

fun main() {

    val t = ElapsedTimer()
    runTests(100)
    val elapsed = t.elapsed()
    println(t)
    println("mTicks per second = %.2f".format( Sokoban().totalTicks().toDouble() * 1e-3 / elapsed))

}

fun runTests(n: Int) : StatSummary {
    val ss = StatSummary("Sokoban scores")
    for (i in 0 until n) {
        // println("Running game $i")
        val score = runGame()
        ss.add(score)
    }
    println(ss)
    return ss
}

fun runGame() : Double {
    var game = Sokoban()
    val actions = intArrayOf(0, 0)
    var agent: SimplePlayerInterface = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 100, nEvals = 100)
    // var agent = RandomAgent()
    // var agent = SokobanKeyController()

    // val nSteps = 20000

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

    // println("Game finished: Win: " + gameOver + ", Score: " + game.score() + ", Time: " + i)
    return game.score()

}
