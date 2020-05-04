package games.tetris

import games.citywars.ACTIONS
import utilities.StatSummary
import kotlin.random.Random

fun main() {

    // nTicks works - so what is broken?

    val game = TetrisGame()
    val nSteps = 200
    val rand = Random

    val nTrials = 10
    for (i in 0 until nTrials) {
        val nRepeats = 100
        val ss = StatSummary("Test $i")
        val sequence = Array<Int>(nSteps) { rand.nextInt(game.nActions()) }
        for (j in 0 until nRepeats) {
            val state = game.copy()
            // val state = TetrisGame()
            for (action in sequence) state.next( intArrayOf(action))
            ss.add(state.score())
//            ss.add(sequence.sum())
//            ss.add(state.nTicks)
        }
        println(ss)
        println()
    }
    // println(game.nActions())

    for (a in Actions.values()) println(a)
}

