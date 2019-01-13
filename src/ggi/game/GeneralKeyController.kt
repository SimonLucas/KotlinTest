package ggi.game

import games.breakout.Constants
import ggi.AbstractGameState
import ggi.SimplePlayerInterface
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent

class GeneralKeyController : KeyAdapter(), SimplePlayerInterface {

    internal var selectedAction = Constants.doNothing
    var keyMap: HashMap<Int,Int> = HashMap()

    override fun keyPressed(e: KeyEvent?) {
        // System.out.println(e);
        val key = e!!.keyCode
        val candidate = keyMap.get(key)
        if (candidate != null) selectedAction = candidate;
        // println(selectedAction)
    }

    override fun keyReleased(e: KeyEvent?) {
        selectedAction = Constants.doNothing
    }

    override fun getAction(gameState: AbstractGameState, playerId: Int): Int {
        return selectedAction
    }

    override fun reset(): SimplePlayerInterface {
        return this
    }
}
