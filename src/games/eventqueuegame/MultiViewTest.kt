package games.eventqueuegame

import utilities.JEasyFrame
import java.awt.Color
import java.awt.FlowLayout
import javax.swing.JComponent

fun main() {

    val params = EventGameParams(nAttempts = 20, minRad = 20, maxRad = 50)
    val world = World().randomize(params).randomiseIds()
    val game = EventQueueGame()
    game.world = world

    game.eventQueue.add(Event(200,
            CityInflux(nPeople = 100, cityId = 0, playerId = PlayerId.Blue)))

    println(world)

    val omniView = WorldView(game)
    val redView = WorldView(game)
    val blueView = WorldView(game)

    val multiView =ListComponent()

    multiView.add(omniView)
    multiView.add(redView)
    multiView.add(blueView)

    val frame = JEasyFrame(multiView, "Event Based Game")

    while (true) {
        multiView.repaint()
        game.next(intArrayOf())
        // now process each version of the game

        // create separate fogged out copies for each view
        val redGame = game.copy() as EventQueueGame
        val blueGame = game.copy() as EventQueueGame

        redGame.world.fogTest(PlayerId.Red)
        blueGame.world.fogTest(PlayerId.Blue)
        redView.game = redGame
        blueView.game = blueGame

        Thread.sleep(50)
        frame.title = "${game.nTicks()}"
    }
}



internal class ListComponent : JComponent() {
    init {
        background = Color.getHSBColor(0.7f, 1.0f, 1.0f)
        layout = FlowLayout(FlowLayout.CENTER, 5, 5)
    }

    //        public void add(PairView pairView) {
    //            add(pairView);
    //        }

}

