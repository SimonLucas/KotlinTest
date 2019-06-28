package ggik

import agents.DoNothingAgent
import agents.RandomAgent
import agents.SimpleEvoAgent
import games.breakout.BreakoutGameState
import games.caveswing.CaveGameState
import games.caveswing.CaveSwingParams
import games.caveswing.Map
import games.coopdrive.CoopDriveState
import games.sokoban.Sokoban
import ggi.AbstractGameState
import ggi.ExtendedAbstractGameState
import ggi.SimplePlayerInterface
import utilities.ElapsedTimer
import utilities.StatSummary
import java.util.*


fun main(args: Array<String>) {
    // now play a random game
    val games = listOf<ExtendedAbstractGameState>(
            // BreakoutGameState().setUp()
            CoopDriveState()
            // CaveGameState().setup()
            // Sokoban()

    )
    val agents = listOf<SimplePlayerInterface>(
            // SimpleEvoAgent(),
            // SimpleEvoAgent(useShiftBuffer = false),
            SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 200)
            // SimpleEvoAgent(repeatProb = 0.0),
            // SimpleEvoAgent(repeatProb = 0.5)
            // SimpleEvoAgent(repeatProb = 0.8),
            // SimpleEvoAgent(probMutation = 0.1, repeatProb = 0.8)
            // RandomAgent()

    )
    val runner = GameRunner()
    val nGames = 5
    for (game in games) {
        for (agent in agents) {
            runner.runGames(game, agent, DoNothingAgent(), nGames)
        }
    }
}

class GameRunner {

    var verbose = false
    var maxTicks = 5000

    fun runGames(gameState: ExtendedAbstractGameState, agent: SimplePlayerInterface, opponent: SimplePlayerInterface = DoNothingAgent(), nGames: Int = 100) {
        val message = "%s playing %s".format(agent, gameState)
        val scores = StatSummary("Scores for: " + message)
        val durations = StatSummary("Durations for: " + message)
        val timer = ElapsedTimer()

        gameState.resetTotalTicks()
        for (i in 0 until nGames) {
            gameState.randomInitialState()
            val finalState = runOneGame(gameState.copy(), agent, opponent)
            scores.add(finalState.score())
            durations.add(finalState.nTicks())
        }
        val elapsed = timer.elapsed().toDouble()
        // println(durations)
        println(scores)
        println(timer)
        println("Total ticks: " + gameState.totalTicks())
        println("Millions of ticks per second: %.4f".format(gameState.totalTicks() * 1e-3 / elapsed))
        println()

    }

    fun runOneGame(gameState: AbstractGameState, player: SimplePlayerInterface, opponent: SimplePlayerInterface): AbstractGameState {
        val playerId = 0
        player.reset()
        var n = 0
        println("maxTicks = " + maxTicks)
        while (!gameState.isTerminal() && n++ < maxTicks) {
            // val actions = intArrayOf(player.getAction(deepCopy(gameState)))
            val actions = intArrayOf(player.getAction(gameState.copy(), playerId), opponent.getAction(gameState.copy(), 1 - playerId))
            // println(Arrays.toString(actions))
            // println("$n\t ${gameState.score()}")
            gameState.next(actions)
        }
        if (verbose) {
            println("Game score: ${gameState.score()}")
            println("Game ticks: ${gameState.nTicks()}")
            println()
        }
        return gameState

    }
}
