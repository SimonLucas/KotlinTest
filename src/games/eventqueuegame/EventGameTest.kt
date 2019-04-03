package games.eventqueuegame

import utilities.JEasyFrame

fun main() {

    val params = EventGameParams(nAttempts = 20, minRad = 20, maxRad = 50)
    val world = World().randomize(params).randomiseIds()
    val game = EventQueueGame()
    game.world = world
    game.eventQueue.add(Event(100, CityInflux(100, 0, PlayerId.Blue)))

    println(world)

    val view = WorldView(game)
    val frame = JEasyFrame(view, "Event Based Game")

    while (true) {
        view.repaint()
        game.next(intArrayOf())
        Thread.sleep(50)
        frame.title = "${game.nTicks()}"
    }

}
