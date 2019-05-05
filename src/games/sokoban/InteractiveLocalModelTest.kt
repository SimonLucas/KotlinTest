package games.sokoban

import utilities.JEasyFrame
import java.awt.Color
import java.awt.FlowLayout
import javax.swing.JComponent

fun main() {

    val span = 2

    var game = Sokoban()
    val gatherer = GatherData(span)

    var lfm = LocalForwardModel(gatherer.tileData, gatherer.rewardData, span)
    lfm.setGrid(game.board.grid, game.board.playerX, game.board.playerY)

    val gv = SokobanView(game.board)
    // set up with the same board for now, but change late
    val gvShadow = SokobanView(game.board)
    val lc = ListComponent()
    lc.add(gv)
    lc.add(gvShadow)
    val frame = JEasyFrame(lc, "Sokoban!")

    val actions = intArrayOf(0, 0)
    var agent = SokobanKeyController()

    val nSteps = 2000
    if (agent is SokobanKeyController)
        frame.addKeyListener(agent.keyListener)

    var i = 0

    while (i < nSteps && !game.isTerminal()) {
        //Take and execute actions
        actions[0] = agent.getAction(game.copy(), Constants.player1)
        game.next(actions)
        lfm.next(actions)


        if (actions[0] != 0) lfm.grid.print()

        //visuals
        gv.grid = game.board
        gv.repaint()

        // setting this will inject tile patterns
        // that never occur during training, and
        // can have a domino effect on the level falling apart
        // lfm.grid.setCell(0, 0, 'o')
        gvShadow.grid = Grid().forceArray(lfm.grid.grid)
        gvShadow.repaint()


        frame.title = "tick = ${game.nTicks}, true score = ${game.score()}, estimate = ${lfm.score()}"
        Thread.sleep(2000)

        //next step
        i++
        println()
    }
    println("Game finished: Win: " + game.isTerminal() + ", Score: " + game.score() + ", Time: " + i)
}


internal class ListComponent : JComponent() {
    init {
        background = Color.getHSBColor(0.7f, 1.0f, 1.0f)
        layout = FlowLayout(FlowLayout.CENTER, 20, 20)
    }
}
