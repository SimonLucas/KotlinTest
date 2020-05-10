package games.tetris

import agents.PolicyEvoAgent
import agents.RandomAgent
import agents.SimpleEvoAgent
import games.gridworld.PairView
import ggi.AbstractGameState
import ggi.AbstractValueFunction
import ggi.SimplePlayerInterface
import ggi.game.GeneralKeyController
import utilities.JEasyFrame
import utilities.StatSummary
import views.EasyPlot
import java.awt.event.KeyEvent


fun main() {
    TetrisModel.defaultCols = 10
    TetrisModel.defaultRows = 20
    TetrisModel.includeColumnDiffs = false
    TetrisModel.gameOverPenalty = 0
    TetrisModel.cyclicBlockType = false
    TetrisModel.randomInitialRotation = true
    TetrisModel.randomShapeColours = false
    TetrisModel.gameOverPenalty = 0

    var tg = TetrisGame()
    // the game will drop the current piece once every dropSkip frames
    // or every frame if dropSkip is zero
    TetrisModel.dropSkip = 50
    val agent = TetrisKeyController()

    val tv = TetrisView(tg.tm.nCols, tg.tm.nRows)
    val frame = JEasyFrame(tv, "Tetris")
    frame.addKeyListener(agent.keyListener)

    while (!tg.isTerminal()) {
        tg = tg.copy() as TetrisGame
        val action = agent.getAction(tg.copy(), 0)
        tg.next(intArrayOf(action))

        tv.setData(tg.tm.a, tg.tm.tetronSprite, tg.tm.getGhost())
        tv.repaint()
        var score = tg.score()
        val message = "${tg.nTicks}\t $score\t $action\t ${tg.totalTicks()}\t ${tg.subGoal()}"
        // println(message)
        frame.title = message
        Thread.sleep(20)
    }
}

class TetrisKeyController : SimplePlayerInterface {

    override fun getAgentType(): String {
        return "BreakoutKeyController"
    }

    val keyMap: HashMap<Int, Int> =
            hashMapOf(KeyEvent.VK_LEFT to Actions.Left.ordinal,
                    KeyEvent.VK_UP to Actions.Rotate.ordinal,
                    KeyEvent.VK_RIGHT to Actions.Right.ordinal,
                    KeyEvent.VK_DOWN to Actions.Down.ordinal,
                    KeyEvent.VK_SPACE to Actions.Drop.ordinal)

    val keyListener = GeneralKeyController()

    constructor() {
        keyListener.keyMap = keyMap
    }

    // in fact all that needs doing in this class is to set up
    // the keyMap, so should just push everything to that general class
    override fun getAction(gameState: AbstractGameState, playerId: Int): Int {
        val action = keyListener.selectedAction
        keyListener.selectedAction = Actions.DoNothing.ordinal
        return action
    }

    override fun reset(): SimplePlayerInterface {
        return this
    }

}
