package test.junit

import games.citywars.LEFT
import games.eventqueuegame.NoAction
import ggi.*
import ggi.game.*

class SimpleMazeGame(val playerCount: Int, val target: Int) : ActionAbstractGameState {

    var currentPosition = IntArray(playerCount) { 0 }  // initialise all players to the origin

    override fun nActions() = 3
    // LEFT, RIGHT, STOP

    override fun score(player: Int): Double {
        // we just have a score of 1.0 for reaching the goal
        return if (currentPosition[player] >= target) 1.0 else 0.0
    }

    override fun playerCount() = playerCount

    override fun possibleActions(player: Int) = listOf(
            Move(player, Direction.LEFT),
            Move(player, Direction.RIGHT),
            NoAction)

    override fun translateGene(player: Int, gene: IntArray) = possibleActions(player).getOrElse(gene[0]) { NoAction }

    override fun registerAgent(player: Int, agent: SimpleActionPlayerInterface) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAgent(player: Int): SimpleActionPlayerInterface {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun copy(): AbstractGameState {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isTerminal(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun nTicks(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun next(forwardTicks: Int): ActionAbstractGameState {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}

enum class Direction { LEFT, RIGHT }

data class Move(val player: Int, val direction: Direction) : Action {
    override fun apply(state: ActionAbstractGameState): ActionAbstractGameState {
        if (state is SimpleMazeGame) {
            when (direction) {
                Direction.LEFT -> state.currentPosition[player]--
                Direction.RIGHT -> state.currentPosition[player]++
            }
        }
        return state
    }

    override fun visibleTo(player: Int, state: ActionAbstractGameState) = true
}
