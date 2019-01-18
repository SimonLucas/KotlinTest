package games.breakout

import agents.RandomAgent
import agents.SimpleEvoAgent
import games.caveswing.frameDelay
import games.caveswing.gameDelay
import ggi.SimplePlayerInterface
import utilities.JEasyFrame

fun main(args: Array<String>) {
    var gameState = BreakoutGameState().setUp()
    println(gameState)
    val view = BreakoutView()
    view.gameState = gameState
    val frame = JEasyFrame(view, "Breakout")
    val playerId = 0;

    val frameDelay: Long = 20

    while(true) {
        var player: SimplePlayerInterface = RandomAgent()
        player = SimpleEvoAgent()
        if (player is SimpleEvoAgent) {
            player.sequenceLength *= 2
            player.nEvals *= 2
            player.useShiftBuffer = true
            player.useMutationTransducer = true
            player.probMutation = 0.2
            player.repeatProb = 0.5
        }
        // player = BreakoutKeyController()
        if (player is BreakoutKeyController)
            frame.addKeyListener(player.keyListener)
        while (!gameState.isTerminal()) {
            val actions = intArrayOf(player.getAction(gameState.copy(), playerId))
            gameState.next(actions, playerId)
            // println(actions)
            // if (player is SimpleEvoAgent)
                // view.playouts = player.getSolutionsCopy()

            view.repaint()
            frame.title = "Score = %.0f : %d".format(gameState.score(), gameState.nTicks())
            Thread.sleep(frameDelay)

        }
        Thread.sleep(gameDelay)
        gameState = BreakoutGameState().setUp()
        view.gameState = gameState
    }
}

