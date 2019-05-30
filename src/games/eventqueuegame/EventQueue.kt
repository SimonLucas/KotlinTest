package games.eventqueuegame

import ggi.SimpleActionPlayerInterface
import ggi.game.*
import java.util.*
import kotlin.collections.HashMap


class EventQueue(val eventQueue: Queue<Event> = PriorityQueue<Event>()) : Queue<Event> by eventQueue {

    private val playerAgentMap = HashMap<Int, SimpleActionPlayerInterface>()
    var currentTime = 0
        set(time) {
            if (eventQueue.isNotEmpty() && eventQueue.peek().tick < time) {
                throw AssertionError("Cannot set time to later than an event in the queue")
            }
            field = time
        }

    fun registerAgent(player: Int, agent: SimpleActionPlayerInterface, currentTime: Int) {
        if (agent is SimpleActionDoNothing) {
            eventQueue.removeIf { e -> e.action is MakeDecision && e.action.playerRef == player }
        } else {
            playerAgentMap[player] = agent
            if (eventQueue.none { e -> e.action is MakeDecision && e.action.playerRef == player }) {
                eventQueue.add(Event(currentTime, MakeDecision(player)))
            }
        }
    }

    fun getAgent(player: Int) = playerAgentMap[player] ?: SimpleActionDoNothing

    inline fun addAll(oldQueue: EventQueue, filterLogic: (Event) -> Boolean) {
        eventQueue.addAll(oldQueue.eventQueue.filter(filterLogic))
    }

    fun next(forwardTicks: Int, state: ActionAbstractGameState) {
        val timeToFinish = currentTime + forwardTicks
        do {
            // we may have multiple events triggering in the same tick
            val event = eventQueue.peek()
            if (event != null && event.tick < timeToFinish) {
                // the time has come to trigger it
                eventQueue.poll()
                if (event.tick < currentTime) {
                    throw AssertionError("Should not have an event on the queue that should have been processed in the past")
                }
                currentTime = event.tick
                event.action.apply(state)
                //           println("Triggered event: ${event} in Game $this")
            } else {
                currentTime = timeToFinish
            }
        } while (timeToFinish > currentTime && !state.isTerminal())
    }
}

data class MakeDecision(val playerRef: Int) : Action {
    override fun apply(state: ActionAbstractGameState): Int {
        val agent = state.getAgent(playerRef)
        val perceivedState = state.copy(playerRef) as ActionAbstractGameState
        val action = agent.getAction(perceivedState, playerRef)
        val nextDecisionPoint = action.apply(state)
        if (nextDecisionPoint < state.nTicks())
            throw AssertionError("Next Decision point must be in the future")
        state.planEvent(nextDecisionPoint, MakeDecision(playerRef))
        return -1
    }

    // only visible to planning player
    override fun visibleTo(player: Int, state: ActionAbstractGameState) = player == playerRef
}