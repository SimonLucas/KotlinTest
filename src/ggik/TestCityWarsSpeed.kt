package ggik

import agents.DoNothingAgent
import agents.RandomAgent
import agents.SimpleEvoAgent
import games.breakout.BreakoutGameState
import games.citywars.CityWars
import games.gridgame.GridGame
import ggi.ExtendedAbstractGameState
import ggi.SimplePlayerInterface

fun main(args: Array<String>) {
    // now play a random game
    val game = CityWars()
    val games = listOf<ExtendedAbstractGameState>(
            game

    )
    val agents = listOf<SimplePlayerInterface>(
            DoNothingAgent(game.doNothingAction()),
            // SimpleEvoAgent(),
            // SimpleEvoAgent(useShiftBuffer = false),
            SimpleEvoAgent(useMutationTransducer = false, useShiftBuffer = true, sequenceLength = 20, nEvals = 5)
//            SimpleEvoAgent(useMutationTransducer = false, useShiftBuffer = true, sequenceLength = 20, nEvals = 5),
//            SimpleEvoAgent(useMutationTransducer = false, useShiftBuffer = true, sequenceLength = 5, nEvals = 20),
//            SimpleEvoAgent(useMutationTransducer = true, useShiftBuffer = true, sequenceLength = 5, nEvals = 20)
            // SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 10, nEvals = 20),
            // SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 5, nEvals = 40),
            // SimpleEvoAgent(repeatProb = 0.0),
            // SimpleEvoAgent(repeatProb = 0.5)
            // SimpleEvoAgent(repeatProb = 0.8),
            // SimpleEvoAgent(probMutation = 0.1, repeatProb = 0.8)
            // RandomAgent()

    )
    val runner = GameRunner()
    runner.maxTicks = 2000
    runner.verbose = true
    val nGames = 10
    for (game in games) {
        for (agent in agents) {
            runner.runGames(game, agent, nGames=nGames)
        }
    }
}

