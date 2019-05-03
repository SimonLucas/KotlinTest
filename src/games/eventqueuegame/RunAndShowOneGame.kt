package games.eventqueuegame

import agents.SimpleEvoAgent
import utilities.JEasyFrame
import kotlin.random.Random

    fun main() {
        val params = EventGameParams(minSep = 50)
        val world = World(speed = 10.0, random = Random(1), params = params)
        val game = EventQueueGame()
        game.world = world
        val agents = HashMap<PlayerId, SimpleEvoAgent>()
        agents[PlayerId.Blue] = SimpleEvoAgent(nEvals = 100, sequenceLength = 300)
        agents[PlayerId.Red] = SimpleEvoAgent(nEvals = 20, sequenceLength = 100)

        println(world)

        val view = WorldView(game)
        val frame = JEasyFrame(view, "Event Based Game")

        while (!game.isTerminal()) {
            val redGene = agents[PlayerId.Red]?.getActions(game, 1)?.slice(0..3) ?: listOf(0, 0, 0, 0)
            val redAction = LaunchExpedition(PlayerId.Red, redGene.get(0), redGene.get(1), redGene.get(2), redGene.get(3))
            val blueGene = agents[PlayerId.Blue]?.getActions(game, 0)?.slice(0..3) ?: listOf(0, 0, 0, 0)
            val blueAction = LaunchExpedition(PlayerId.Blue, blueGene.get(0), blueGene.get(1), blueGene.get(2), blueGene.get(3))
            game.next(listOf(blueAction, redAction))
            frame.title = "${game.nTicks()}"
            view.repaint()
            Thread.sleep(50)
        }
    }
