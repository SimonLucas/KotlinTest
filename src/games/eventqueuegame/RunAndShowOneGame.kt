package games.eventqueuegame

import agents.SimpleEvoAgent
import utilities.JEasyFrame
import kotlin.random.Random

    fun main() {
        val params = EventGameParams(minSep = 50)
        val world = World(speed = 10.0, random = Random(1), params = params)
        val game = EventQueueGame(world)
        game.registerAgent(PlayerId.Blue, SimpleActionEvoAgent(SimpleEvoAgent(nEvals = 100, sequenceLength = 100)))
        game.registerAgent(PlayerId.Red, SimpleActionEvoAgent(SimpleEvoAgent(nEvals = 20, sequenceLength = 100)))
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
