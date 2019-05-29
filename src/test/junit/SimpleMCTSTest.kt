package test.junit

import games.eventqueuegame.*
import ggi.*
import ggi.game.*

class SimpleMazeGame(val playerCount: Int, val target: Int) : ActionAbstractGameState {

    val eventQueue = EventQueue()
    override fun registerAgent(player: Int, agent: SimpleActionPlayerInterface) = eventQueue.registerAgent(player, agent, nTicks())
    override fun getAgent(player: Int) = eventQueue.getAgent(player)
    override fun planEvent(time: Int, action: Action) {
        eventQueue.add(Event(time, action))
    }

    var currentPosition = IntArray(playerCount) { 0 }  // initialise all players to the origin
    var currentTime = 0

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

    override fun codonsPerAction() = 1
    override fun translateGene(player: Int, gene: IntArray) = possibleActions(player).getOrElse(gene[0]) { NoAction }

    override fun copy(): AbstractGameState {
        val retValue = SimpleMazeGame(playerCount, target)
        retValue.currentPosition = currentPosition.copyOf()
        retValue.currentTime = currentTime
        return retValue
    }

    override fun isTerminal() = currentPosition.any{it >= target}

    override fun nTicks() = currentTime

    override fun next(forwardTicks: Int): ActionAbstractGameState {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

enum class Direction { LEFT, RIGHT }

data class Move(val player: Int, val direction: Direction) : Action {
    override fun apply(state: ActionAbstractGameState): Int {
        if (state is SimpleMazeGame) {
            when (direction) {
                Direction.LEFT -> state.currentPosition[player]--
                Direction.RIGHT -> state.currentPosition[player]++
            }
        }
        return state.nTicks() + 1
    }

    override fun visibleTo(player: Int, state: ActionAbstractGameState) = true
}
