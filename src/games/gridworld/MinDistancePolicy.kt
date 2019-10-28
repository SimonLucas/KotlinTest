package games.gridworld

import ggi.AbstractGameState
import ggi.SimplePlayerInterface
import utilities.Picker
import kotlin.random.Random

class MinDistancePolicy : SimplePlayerInterface {

    val random = Random

    override fun getAction(gameState: AbstractGameState, playerId: Int): Int {

        if (!(gameState is GridWorld)) return random.nextInt(gameState.nActions())

        // great that Kotlin will now treat gameState as being an instance of GridWorld

        // make a picker to choose the minimum
        val picker = Picker<Int>(Picker.MIN_FIRST)
        for (i in 0 until gameState.nActions()) {
            val pos = gameState.gridPosition
            val next = pos.move(i)
            if (gameState.simpleGrid.getCell(next.x, next.y) == GridWorldConstants.navChar) {
                picker.add( next.getVec().distanceTo(gameState.goal.getVec()) , i )
            }
        }
        val best = picker.best
        if (best == null) return random.nextInt(gameState.nActions())
        else return best
    }

    override fun reset(): SimplePlayerInterface {
        // nothing needs doing
        return this
    }

    override fun getAgentType(): String {
        return "MinDistancePolicy (gridgame)"
    }
}