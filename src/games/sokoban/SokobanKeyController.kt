package games.sokoban

import ggi.AbstractGameState
import ggi.SimplePlayerInterface
import ggi.game.GeneralKeyController
import java.awt.event.KeyEvent

class SokobanKeyController : SimplePlayerInterface {

    override fun getAgentType(): String {
        return "BreakoutKeyController"
    }

    val keyMap: HashMap<Int, Int> =
            hashMapOf(KeyEvent.VK_LEFT to SokobanConstants.LEFT,
                      KeyEvent.VK_UP to SokobanConstants.UP,
                      KeyEvent.VK_RIGHT to SokobanConstants.RIGHT,
                    KeyEvent.VK_DOWN to SokobanConstants.DOWN)

    val keyListener = GeneralKeyController()

    constructor() {
        keyListener.keyMap = keyMap
    }

    // in fact all that needs doing in this class is to set up
    // the keyMap, so should just push everything to that general class
    override fun getAction(gameState: AbstractGameState, playerId: Int): Int {
        return keyListener.selectedAction
    }

    override fun reset(): SimplePlayerInterface {
        return this
    }

}
