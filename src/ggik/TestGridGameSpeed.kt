package ggik

import agents.RandomAgent
import agents.SimpleEvoAgent
import games.breakout.BreakoutGameState
import games.gridgame.GridGame
import ggi.ExtendedAbstractGameState
import ggi.SimplePlayerInterface

fun main(args: Array<String>) {
    // now play a random game
    val games = listOf<ExtendedAbstractGameState>(
            GridGame(15,15)

    )
    val agents = listOf<SimplePlayerInterface>(
            // SimpleEvoAgent(),
            // SimpleEvoAgent(useShiftBuffer = false),
            // SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 20, nEvals = 10),
            SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 10, nEvals = 20),
            SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 5, nEvals = 40),
            // SimpleEvoAgent(repeatProb = 0.0),
            // SimpleEvoAgent(repeatProb = 0.5)
            // SimpleEvoAgent(repeatProb = 0.8),
            // SimpleEvoAgent(probMutation = 0.1, repeatProb = 0.8)
            RandomAgent()

    )
    val runner = GameRunner()
    runner.maxTicks = 1000
    val nGames = 30
    for (game in games) {
        for (agent in agents) {
            runner.runGames(game, agent, nGames)
        }
    }
}

