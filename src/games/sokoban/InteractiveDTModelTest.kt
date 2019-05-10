package games.sokoban

import utilities.JEasyFrame

fun main() {

    val pretrainModel = true
    val span = 2


    val game = Sokoban()
    val iterator = CrossGridIterator(2)
    val dtm = DTModel(iterator)
    if (pretrainModel) ModelTrainer().trainModel(dtm)

    // dtm.setGridArray(game.board.grid, game.board.playerX, game.board.playerY)
    dtm.setGrid((game.copy() as Sokoban).board.getSimpleGrid())


    val gv = SokobanView(game.board)
    val gvShadow = SokobanView(game.board)

    val lc = ListComponent()
    lc.add(gv)
    lc.add(gvShadow)
    val frame = JEasyFrame(lc, "Sokoban!")

    val actions = intArrayOf(0, 0)
    val agent = SokobanKeyController()

    val nSteps = 2000
    if (agent is SokobanKeyController)
        frame.addKeyListener(agent.keyListener)

    var i = 0

    while (i < nSteps && !game.isTerminal()) {
        //Take and execute actions
        actions[0] = agent.getAction(game.copy(), Constants.player1)
        game.next(actions)
        dtm.next(actions)


        if (actions[0] != 0) dtm.getGrid().print()

        //visuals
        gv.grid = game.board
        gv.repaint()

        // setting this will inject tile patterns
        // that never occur during training, and
        // can have a domino effect on the level falling apart
        // lfm.grid.setCell(0, 0, 'o')
        gvShadow.grid = Grid().forceArray(dtm.getGrid().grid)
        gvShadow.repaint()


        frame.title = "tick = ${game.nTicks}, true score = ${game.score()}, estimate = ${dtm.score()}"
        Thread.sleep(2000)

        //next step
        i++
        println()
    }
    println("Game finished: Win: " + game.isTerminal() + ", Score: " + game.score() + ", Time: " + i)
}

