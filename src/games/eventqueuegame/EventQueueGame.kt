package games.eventqueuegame

import ggi.game.*
import math.Vec2d
import java.util.PriorityQueue
import kotlin.math.*
import kotlin.collections.*
import kotlin.random.Random
import ggi.SimpleActionPlayerInterface as SimpleActionPlayerInterface

// todo : Decide which effects to add next

// todo: An invasion model?

// todo: A recce model - send units to observe

// todo: a win ratio

// todo: Rapid planner example: and when to retreat

data class EventGameParams(
        val nAttempts: Int = 10,
        val width: Int = 1000,
        val height: Int = 600,
        val minRad: Int = 25,
        val maxRad: Int = 25,
        val minPop: Int = 10,
        val maxPop: Int = 100,
        val minSep: Int = 30,
        val seed: Long = 10,
        val autoConnect: Int = 300,
        val minConnections: Int = 2,
        val maxDistance: Int = 1000,
        val speed: Double = 10.0,
        val defaultOODALoop: Int = 10,
        val blueLanchesterCoeff: Double = 0.05,
        val redLanchesterCoeff: Double = 0.05,
        val blueLanchesterExp: Double = 1.0,    // should be between 0.0 and 1.0
        val redLanchesterExp: Double = 1.0,  // should be between 0.0 and 1.0
        val planningHorizon: Int = 100
)

var totalTicks: Long = 0


data class Event(val tick: Int, val action: Action) : Comparable<Event> {
    operator override fun compareTo(other: Event): Int {
        return tick.compareTo(other.tick)
    }
}

class EventQueueGame(val world: World = World()) : ActionAbstractGameState {

    val eventQueue = PriorityQueue<Event>()
    var scoreFunction: (EventQueueGame) -> Double = {
        // as a default we count the number of Blue cities, and subtract the number of red cities
        val blueCities = it.world.cities.count { c -> c.owner == PlayerId.Blue }
        val redCities = it.world.cities.count { c -> c.owner == PlayerId.Red }
        (blueCities - redCities).toDouble()
    }
    private val playerAgentMap = HashMap<Int, SimpleActionPlayerInterface>()

    override fun registerAgent(player: Int, agent: SimpleActionPlayerInterface) {
        playerAgentMap[player] = agent
        val playerID =  if (player == 0) PlayerId.Blue else PlayerId.Red
        if (eventQueue.none{e -> e.action is MakeDecision && e.action.player == playerID}) {
            eventQueue.add(Event(world.currentTicks, MakeDecision(playerID)))
        }
    }

    override fun getAgent(player: Int) = playerAgentMap[player] ?: SimpleActionDoNothing

    override fun copy(): EventQueueGame {
        val state = EventQueueGame(world.deepCopy())
        state.eventQueue.addAll(eventQueue)
        state.scoreFunction = scoreFunction
        playerAgentMap.forEach { (k, v) -> state.registerAgent(k, v.getForwardModelInterface()) }
        return state
    }

    override fun playerCount() = 2

    override fun codonsPerAction() = 4

    override fun nActions() = world.cities.size

    override fun possibleActions(player: Int): List<Action> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun translateGene(player: Int, gene: IntArray): Action {
        // if the gene does not encode a valid LaunchExpedition, then we interpret it as a Wait action
        // if we take a real action, then we must wait for a minimum period before the next one
        val playerId: PlayerId = if (player == 0) PlayerId.Blue else PlayerId.Red
        val proposedAction = LaunchExpedition(playerId, gene[0], gene[1], gene[2], max(gene[3], world.params.defaultOODALoop))
        if (!proposedAction.isValid(this.world))
            return Wait(playerId, max(gene[3], 1))
        return proposedAction
    }

    override fun next(forwardTicks: Int): EventQueueGame {
        for (i in 1..forwardTicks) {
            world.currentTicks++

            var finished = false
            do {
                // we may have multiple events triggering in the same tick
                val event = eventQueue.peek()
                if (event != null && event.tick < world.currentTicks) {
                    // the time has come to trigger it
                    eventQueue.poll()
                    event.action.apply(this)
                    //           println("Triggered event: ${event} in Game $this")
                } else {
                    finished = true
                }
            } while (!finished)
        }
        return this
    }

    override fun score(): Double {
        return scoreFunction(this)
    }

    override fun isTerminal(): Boolean {
        // game is over if all cities are controlled by the same player, whoever that is
        val player0 = world.cities[0].owner
        return (world.currentTicks > 1000 || world.cities.all { c -> c.owner == player0 })
    }

    override fun nTicks(): Int {
        return world.currentTicks
    }
}
