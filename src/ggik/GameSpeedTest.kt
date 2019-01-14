package ggik

import agents.RandomAgent
import agents.SimpleEvoAgent
import games.breakout.BreakoutGameState
import games.caveswing.CaveGameState
import games.caveswing.CaveSwingParams
import games.caveswing.Map
import ggi.AbstractGameState
import ggi.ExtendedAbstractGameState
import ggi.SimplePlayerInterface
import utilities.ElapsedTimer
import utilities.StatSummary
import java.util.*


fun main(args: Array<String>) {
    // now play a random game
    val games = listOf<ExtendedAbstractGameState>(
            BreakoutGameState().setUp()
            // CaveGameState().setup()

    )
    val agents = listOf<SimplePlayerInterface>(
            SimpleEvoAgent(),
            // SimpleEvoAgent(useMutationTransducer = false),
            // SimpleEvoAgent(repeatProb = 0.0),
            SimpleEvoAgent(repeatProb = 0.5)
            // RandomAgent()

    )
    val runner = GameRunner()
    val nGames = 30
    for (game in games) {
        for (agent in agents) {
            runner.runGames(game, agent, nGames)
        }
    }
}

class GameRunner {

    var verbose = false

    fun runGames(gameState: ExtendedAbstractGameState, agent: SimplePlayerInterface, nGames: Int = 100) {
        val message = "%s playing %s".format(agent, gameState)
        val scores = StatSummary("Scores for: " + message)
        val durations = StatSummary("Durations for: " + message)
        val timer = ElapsedTimer()

        gameState.resetTotalTicks()
        for (i in 0 until nGames) {
            val finalState = runOneGame(gameState.copy(), agent)
            scores.add(finalState.score())
            durations.add(finalState.nTicks())
        }
        val elapsed = timer.elapsed().toDouble()
        // println(durations)
        println(scores)
        println(timer)
        println("Total ticks: " + gameState.totalTicks())
        println("Millions of ticks per second: %.1f".format(gameState.totalTicks() * 1e-3 / elapsed))
        println()

    }

    fun runOneGame(gameState: AbstractGameState, player: SimplePlayerInterface): AbstractGameState {
        val playerId = 0
        player.reset()
        while (!gameState.isTerminal()) {
            // val actions = intArrayOf(player.getAction(deepCopy(gameState)))
            val actions = intArrayOf(player.getAction(gameState, playerId))
            // println(Arrays.toString(actions))
            gameState.next(actions, playerId)
        }
        if (verbose) {
            println("Game score: ${gameState.score()}")
            println("Game ticks: ${gameState.nTicks()}")
            println()
        }
        return gameState

    }
}
