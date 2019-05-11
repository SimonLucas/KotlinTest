package games.eventqueuegame

import agents.SimpleEvoAgent
import utilities.JEasyFrame
import kotlin.random.Random

    fun main() {
        val params = EventGameParams(minSep = 50)
        val world = World(speed = 10.0, random = Random(1), params = params)
        val game = EventQueueGame(world)
        game.registerAgent(PlayerId.Blue, SimpleActionEvoAgent(SimpleEvoAgent(nEvals = 1000, sequenceLength = 40)))
        game.registerAgent(PlayerId.Red, SimpleActionEvoAgent(SimpleEvoAgent(nEvals = 200, sequenceLength = 40)))
        MakeDecision(PlayerId.Blue).apply(game)
        MakeDecision(PlayerId.Red).apply(game)

        println(world)

        val view = WorldView(game)
        val frame = JEasyFrame(view, "Event Based Game")

        while (!game.isTerminal()) {
            game.next(listOf())
            frame.title = "${game.nTicks()}"
            view.repaint()
            Thread.sleep(50)
        }
    }
