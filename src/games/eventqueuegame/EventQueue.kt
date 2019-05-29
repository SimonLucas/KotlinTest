package games.eventqueuegame

import ggi.SimpleActionPlayerInterface
import ggi.game.ActionAbstractGameState
import java.util.*
import kotlin.collections.HashMap


class EventQueue(val eventQueue: Queue<Event> = PriorityQueue<Event>()) : Queue<Event> by eventQueue  {

    private val playerAgentMap = HashMap<Int, SimpleActionPlayerInterface>()
    var currentTime = 0
        set(time) {
            if (eventQueue.isNotEmpty() && eventQueue.peek().tick < time){
                throw AssertionError("Cannot set time to later than an event in the queue")
            }
            field = time
        }

    fun registerAgent(player: Int, agent: SimpleActionPlayerInterface, currentTime: Int) {
        if (agent is SimpleActionDoNothing) return
        playerAgentMap[player] = agent
        val playerID = if (player == 0) PlayerId.Blue else PlayerId.Red
        if (eventQueue.none { e -> e.action is MakeDecision && e.action.player == playerID }) {
            eventQueue.add(Event(currentTime, MakeDecision(playerID)))
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
        } while (timeToFinish > currentTime)
    }
}