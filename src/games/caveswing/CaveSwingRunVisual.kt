package games.caveswing

import agents.RandomAgent
import agents.SimpleEvoAgent
import ggi.SimplePlayerInterface
import utilities.JEasyFrame
import views.CaveView

val frameDelay: Long = 20;
val gameDelay: Long = 2000;

fun main(args: Array<String>) {
    // var params = CaveSwingParams()
    var gameState = CaveGameState()
    val view = CaveView().setGameState(gameState)
    val frame = JEasyFrame(view, "Cave Swing")
    val playerId = 0
    while (true) {
        var player: SimplePlayerInterface = RandomAgent()
        player = SimpleEvoAgent()
        if (player is SimpleEvoAgent) {
            // apply any custom settings here:
            player.useShiftBuffer = true
        }
        while (!gameState.isTerminal()) {
            val actions = intArrayOf(player.getAction(gameState.copy(), playerId))
            gameState.next(actions)
            // println(actions)
            if (player is SimpleEvoAgent)
                view.playouts = player.getSolutionsCopy()
            view.repaint()
            frame.title = "Score = %.0f".format(gameState.score())
            Thread.sleep(frameDelay)
        }
        Thread.sleep(gameDelay)
        gameState = CaveGameState()
        view.setGameState(gameState)
    }
}
