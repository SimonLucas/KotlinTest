package games.maxgame

import agents.SimpleEvoAgent
import java.util.*

fun main(args: Array<String>) {
    var game = MaxGame()
    val agent = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 20, nEvals = 5, useShiftBuffer = false)
    val playerId = 0
    // get it to play the game
    while (!game.isTerminal()) {
        // take an action
        var action = agent.getAction(game.copy(), playerId)
        // action = 100
        println(Arrays.toString(agent.buffer))
        println("Action: ${action}")
        game.next(intArrayOf(action))
    }
    println(game)
    println(game.copy())
    println("Final score = " + game.score)
    println("Total ticks = ${game.totalTicks()}")
}