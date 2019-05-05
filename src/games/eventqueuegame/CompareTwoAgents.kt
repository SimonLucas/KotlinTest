package games.eventqueuegame

import agents.SimpleEvoAgent
import utilities.JEasyFrame
import kotlin.random.Random

    val calendar = java.util.Calendar.getInstance()

    fun main() {
        val params = EventGameParams(minSep = 50)
        val agents = HashMap<PlayerId, SimpleEvoAgent>()
        agents[PlayerId.Blue] = SimpleEvoAgent(nEvals = 20, sequenceLength = 500)
        agents[PlayerId.Red] = SimpleEvoAgent(nEvals = 5, sequenceLength = 500)

        var blueWins = 0
        val maxGames = 100
        val startTime = calendar.timeInMillis
        for (g in 1..maxGames) {
            val world = World(speed = 10.0, random = Random(1), params = params)
            val game = EventQueueGame(world)
            while (!game.isTerminal()) {
                val redGene = agents[PlayerId.Red]?.getActions(game, 1)?.slice(0..3) ?: listOf(0, 0, 0, 0)
                val redAction = LaunchExpedition(PlayerId.Red, redGene.get(0), redGene.get(1), redGene.get(2), redGene.get(3))
                val blueGene = agents[PlayerId.Blue]?.getActions(game, 0)?.slice(0..3) ?: listOf(0, 0, 0, 0)
                val blueAction = LaunchExpedition(PlayerId.Blue, blueGene.get(0), blueGene.get(1), blueGene.get(2), blueGene.get(3))
                game.next(listOf(blueAction, redAction))
            }
            println(game.score())
            if (game.score() > 0.0) blueWins++
        }
        println("$blueWins wins for Blue out of $maxGames in ${calendar.timeInMillis - startTime} ms")
    }
