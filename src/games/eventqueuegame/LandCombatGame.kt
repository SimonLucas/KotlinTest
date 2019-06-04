package games.eventqueuegame

import ggi.game.*
import test.Player
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
        // world set up
        val nAttempts: Int = 10,
        val width: Int = 1000,
        val height: Int = 600,
        val minRad: Int = 25,
        val maxRad: Int = 25,
        val blueForce: Int = 100,
        val redForce: Int = 100,
        val citySeparation: Int = 30,
        val seed: Long = 10,
        val autoConnect: Int = 300,
        val minConnections: Int = 2,
        val maxDistance: Int = 1000,
        val percentFort: Double = 0.25,
        val fogOfWar: Boolean = false,
        // force and combat attributes
        val speed: Double = 10.0,
        val fortAttackerCoeffDivisor: Double = 3.0,
        val fortDefenderExpIncrease: Double = 0.5,
        val blueLanchesterCoeff: Double = 0.05,
        val redLanchesterCoeff: Double = 0.05,
        val blueLanchesterExp: Double = 1.0,    // should be between 0.0 and 1.0
        val redLanchesterExp: Double = 1.0,  // should be between 0.0 and 1.0
        // agent behaviour
        val OODALoop: IntArray = intArrayOf(10, 10),
        val planningHorizon: Int = 100,
        val maxActionsPerState: Int = 7

)

var totalTicks: Long = 0


data class Event(val tick: Int, val action: Action) : Comparable<Event> {
    operator override fun compareTo(other: Event): Int {
        return tick.compareTo(other.tick)
    }
}

class LandCombatGame(val world: World = World(), val targets: Map<PlayerId, List<Int>> = emptyMap()) : ActionAbstractGameState {
    companion object {
        val stateToActionMap: MutableMap<String, List<Action>> = mutableMapOf()
        val rnd: Random = Random(10)
    }

    val eventQueue = EventQueue()
    override fun registerAgent(player: Int, agent: SimpleActionPlayerInterface) = eventQueue.registerAgent(player, agent, nTicks())
    override fun getAgent(player: Int) = eventQueue.getAgent(player)
    override fun planEvent(time: Int, action: Action) {
        eventQueue.add(Event(time, action))
    }

    var scoreFunction: MutableMap<PlayerId, (LandCombatGame, Int) -> Double> = mutableMapOf(
            PlayerId.Blue to simpleScoreFunction(5.0, 1.0),
            PlayerId.Red to simpleScoreFunction(5.0, 1.0)
    )

    override fun copy(perspective: Int): LandCombatGame {
        val newWorld = if (world.params.fogOfWar) world.deepCopyWithFog(numberToPlayerID(perspective)) else world.deepCopy()
        val retValue = copyHelper(newWorld)
        // We also need to strip out any events in the queue that are not visible to the perspective player!
        retValue.eventQueue.addAll(eventQueue) { e -> e.action.visibleTo(perspective, this) }
        return retValue
    }

    override fun copy(): LandCombatGame {
        val retValue = copyHelper(world.deepCopy())
        retValue.eventQueue.addAll(eventQueue) { true }
        return retValue
    }

    private fun copyHelper(world: World): LandCombatGame {
        val state = LandCombatGame(world, targets)
        state.scoreFunction = scoreFunction
        state.eventQueue.currentTime = nTicks()
        (0 until playerCount()).forEach { p ->
            state.registerAgent(p, getAgent(p).getForwardModelInterface())
        }
        return state
    }

    override fun playerCount() = 2

    override fun codonsPerAction() = 4

    override fun nActions() = world.cities.size

    override fun possibleActions(player: Int): List<Action> {
        val stateRep = LandCombatStateFunction(this)
        // we create X random actions on the same lines as an EvoAgent would
        if (!stateToActionMap.containsKey(stateRep)) {
            val randomActions = (0 until world.params.maxActionsPerState).map {
                translateGene(player, IntArray(codonsPerAction()) { rnd.nextInt(nActions()) })
            }.distinct()
            stateToActionMap[stateRep] = randomActions
        }
        return stateToActionMap[stateRep] ?: emptyList()
    }

    override fun translateGene(player: Int, gene: IntArray): Action {
        // if the gene does not encode a valid LaunchExpedition, then we interpret it as a Wait action
        // if we take a real action, then we must wait for a minimum period before the next one
        val playerId = numberToPlayerID(player)
        val proposedAction = LaunchExpedition(playerId, gene[0], gene[1], gene[2], max(gene[3], world.params.OODALoop[player]))
        if (!proposedAction.isValid(this.world))
            return Wait(player, max(gene[3], 1))
        return proposedAction
    }

    override fun next(forwardTicks: Int): LandCombatGame {
        eventQueue.next(forwardTicks, this)
        return this
    }

    override fun score(player: Int) = scoreFunction[numberToPlayerID(player)]?.invoke(this, player) ?: 0.0

    override fun isTerminal(): Boolean {
        // game is over if all cities are controlled by the same player, whoever that is
        val player0 = world.cities[0].owner
        return (nTicks() > 1000 || world.cities.all { c -> c.owner == player0 })
    }

    override fun nTicks() = eventQueue.currentTime
}
