package games.gridworld

import ggi.AbstractGameState
import ggi.AbstractValueFunction
import ggi.SimplePlayerInterface
import utilities.Picker
import kotlin.random.Random

class ExplorativePolicy : SimplePlayerInterface, AbstractValueFunction {

    // todo: this is currently a work in progress.
    // still need to update with the state observation count
    // and need to decide when to count the visits - i.e.
    // to include when they are visited during a rollout, or
    // only to include when "really" visiting them


    val random = Random
    // if false then only provide distance when at a nav char
    val alwaysGiveDistance = true
    // control size of random noise, at a small level it's just used to randomly break ties
    val eps = 1e-6

    override fun getAction(gameState: AbstractGameState, playerId: Int): Int {

        if (!(gameState is GridWorld)) return random.nextInt(gameState.nActions())

        // great that Kotlin will now treat gameState as being an instance of GridWorld
        // make a picker to choose the minimum

        val picker = Picker<Int>(Picker.MIN_FIRST)
        for (i in 0 until gameState.nActions()) {
            val pos = gameState.gridPosition
            val next = pos.move(i)
            if (gameState.simpleGrid.getCell(next.x, next.y) == GridWorldConstants.navChar) {
                val score =
                        next.getVec().distanceTo(gameState.goal.getVec()) +
                                random.nextDouble() * eps
                picker.add(score, i)
            }
        }
        val best = picker.best
        if (best == null) return random.nextInt(gameState.nActions())
        else return best
    }

    override fun value(gameState: AbstractGameState): Double {
        if (!(gameState is GridWorld)) return 0.0

        return -gameState.goal.getVec().distanceTo(gameState.gridPosition.getVec())

    }


    override fun reset(): SimplePlayerInterface {
        // nothing needs doing
        return this
    }

    override fun getAgentType(): String {
        return "MinDistancePolicy (gridgame)"
    }

}

class ExplorationCounter {
    fun count(stateObservation: Any) {

    }

    fun getCount(stateObservation: Any) {

    }
}
