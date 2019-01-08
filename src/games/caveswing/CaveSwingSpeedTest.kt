package games.caveswing

import agents.RandomAgent
import agents.SimpleEvoAgent
import ggi.SimplePlayerInterface
import utilities.ElapsedTimer
import java.util.*

fun main(args: Array<String>) {
    val params = CaveSwingParams()
    println(params)

    val p2 = params.copy(maxTicks = 500)
    println(p2)

    val map = Map().setup(params)
    println(map)

    // now play a random game
    val timer = ElapsedTimer()
    val nGames = 10
    var totalTicks = 0
    for (i in 0 until nGames) {
        val finalState = runOneGame(params)
        totalTicks += finalState.nTicks
    }
    val elapsed = timer.elapsed().toDouble()
    println(timer)
    println("Total ticks: $totalTicks")
    println("Millions of ticks per second: %.1f".format(totalTicks * 1e-3/elapsed))
}

fun runOneGame (params: CaveSwingParams) : CaveGameState {
    val gameState = CaveGameState().setup(params)
    var player: SimplePlayerInterface = RandomAgent()
    player = SimpleEvoAgent()
    val playerId = 0
    while (!gameState.isTerminal()) {
        // val actions = intArrayOf(player.getAction(deepCopy(gameState)))
        val actions = intArrayOf(player.getAction(gameState, playerId))
        println(Arrays.toString(actions))
        gameState.next(actions, playerId)
    }
//    println("Game score: ${gameState.score()}")
//    println("Game ticks: ${gameState.nTicks}")
//    println()
    return gameState

}
