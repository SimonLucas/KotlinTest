package ggik

import agents.DoNothingAgent
import agents.RandomAgent
import agents.SimpleEvoAgent
import games.breakout.BreakoutGameState
import games.gridgame.GridGame
import ggi.ExtendedAbstractGameState
import ggi.SimplePlayerInterface

fun main(args: Array<String>) {
    // now play a random game
    val game = GridGame(10,10).setFast(false)
    val games = listOf<ExtendedAbstractGameState>(
            game

    )
    val agents = listOf<SimplePlayerInterface>(
            DoNothingAgent(game.doNothingAction()),
            // SimpleEvoAgent(),
            // SimpleEvoAgent(useShiftBuffer = false),
            // SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 20, nEvals = 10),
            // SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 10, nEvals = 20),
            SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 5, nEvals = 40),
            // SimpleEvoAgent(repeatProb = 0.0),
            // SimpleEvoAgent(repeatProb = 0.5)
            // SimpleEvoAgent(repeatProb = 0.8),
            // SimpleEvoAgent(probMutation = 0.1, repeatProb = 0.8)
            RandomAgent()

    )
    val runner = GameRunner()
    runner.maxTicks = 500
    val nGames = 30
    for (game in games) {
        for (agent in agents) {
            runner.runGames(game, agent, nGames)
        }
    }
}

