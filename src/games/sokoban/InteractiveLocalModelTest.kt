package games.sokoban

import utilities.JEasyFrame
import java.awt.Color
import java.awt.FlowLayout
import javax.swing.JComponent

fun main() {

    val span = 2

    val gatherer = Gatherer()
    ModelTrainer(trainLevels = 0..9).trainModel(gatherer)

    // gatherer.report()
    println("Hashmap has ${gatherer.tileData.size} entries")

    var lfm  = LocalForwardModel(gatherer.tileData, gatherer.rewardData, CrossGridIterator(2), false)

    var game = Sokoban(1)

    // var lfm = GPModel()

    lfm.setGrid(game.board.getSimpleGrid())

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


        if (actions[0] != 0) lfm.getGrid().print()

        //visuals
        gv.grid = game.board
        gv.repaint()

        // setting this will inject tile patterns
        // that never occur during training, and
        // can have a domino effect on the level falling apart
        // lfm.grid.setCell(0, 0, 'o')
        gvShadow.grid = game.board.deepCopy().forceArray(lfm.getGrid().grid)
        gvShadow.repaint()


        frame.title = "tick = ${game.nTicks}, true score = ${game.score()}, estimate = ${lfm.score()}"
        Thread.sleep(500)

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

