package ggi.game

import games.breakout.Constants
import ggi.AbstractGameState
import ggi.SimplePlayerInterface
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent

class GeneralKeyController : KeyAdapter(), SimplePlayerInterface {
    override fun getAgentType(): String {
        return "GeneralKeyController"
    }

    var selectedAction = Constants.doNothing
    var keyMap: HashMap<Int,Int> = HashMap()

    override fun keyPressed(e: KeyEvent?) {
        // System.out.println(e);
        super.keyPressed(e)
        val key = e!!.keyCode
        val candidate = keyMap.get(key)
        if (candidate != null) selectedAction = candidate;
        // println(selectedAction)
    }

    override fun keyTyped(e: KeyEvent?) {
        super.keyTyped(e)
    }

    // override fun ke

    override fun keyReleased(e: KeyEvent?) {
        super.keyReleased(e)
        selectedAction = Constants.doNothing
    }

    override fun getAction(gameState: AbstractGameState, playerId: Int): Int {
        return selectedAction
    }

    override fun reset(): SimplePlayerInterface {
        return this
    }
}
