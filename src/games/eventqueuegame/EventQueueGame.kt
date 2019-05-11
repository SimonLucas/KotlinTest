package games.eventqueuegame

import ggi.game.*
import math.Vec2d
import java.lang.Math.pow
import kotlin.math.*
import java.util.*
import kotlin.collections.*
import kotlin.random.Random
import ggi.SimpleActionPlayerInterface as SimpleActionPlayerInterface

// todo : Decide which effects to add next

// todo: An invasion model?

// todo: A recce model - send units to observe

// todo: a win ratio

// todo: Rapid planner example: and when to retreat


data class Event(val tick: Int, val action: Action) : Comparable<Event> {
    override fun compareTo(other: Event): Int {
        return tick.compareTo(other.tick)
    }
}

enum class PlayerId {
    Blue, Red, Neutral, Fog
}

data class City(val location: Vec2d, val radius: Int = 40, var pop: Int = 100, var owner: PlayerId = PlayerId.Neutral)

data class Route(val fromCity: Int, val toCity: Int, val length: Int, val terrainDifficulty: Double)

/*
 A returned positive value is the number of surviving attackers; a negative value is interpreted as the number of
 surviving defenders, with the attack repulsed
 */
fun lanchesterLinearBattle(attack: Double, defence: Double, attackerDamageCoeff: Double, defenderDamageCoeff: Double): Double {
    var attackingForce = attack
    var defendingForce = defence
    var count = 0
    do {
        val attackDmg = attackingForce * attackerDamageCoeff
        val defenceDmg = defendingForce * defenderDamageCoeff
        attackingForce -= defenceDmg
        defendingForce -= attackDmg
        count++
    } while (attackingForce > 0.0 && defendingForce > 0.0 && count < 100)
    return if (defendingForce > 0.0) -defendingForce else attackingForce
}

fun lanchesterClosedFormBattle(attack: Double, defence: Double, attCoeff: Double, attExp: Double, defCoeff: Double, defExp: Double): Double {
    // firstly calculate which side will win
    val constant: Double = attCoeff * pow(attack, attExp + 1) - defCoeff * pow(defence, defExp + 1)
    if (constant > 0.0) {
        // attacker wins
        return pow(constant / attCoeff, 1.0 / (attExp + 1.0))
    } else {
        // defender wins
        return -pow(-constant / defCoeff, 1.0 / (defExp + 1.0))
    }
}

data class EventGameParams(
        val nAttempts: Int = 10,
        val width: Int = 1000,
        val height: Int = 600,
        val minRad: Int = 20,
        val maxRad: Int = 100,
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
        val redLanchesterExp: Double = 1.0   // should be between 0.0 and 1.0
)


data class World(var cities: List<City> = ArrayList(), var routes: List<Route> = ArrayList(),
                 val width: Int = 1000, val height: Int = 600,
                 val speed: Double = 1.0,
                 val random: Random = Random(3),
                 val params: EventGameParams = EventGameParams()) {


    var currentTransits: ArrayList<Transit> = ArrayList()
    var currentTicks: Int = 0
    var allRoutesFromCity: Map<Int, List<Route>> = HashMap()

    init {
        if (cities.isEmpty()) initialise()
        allRoutesFromCity = routes.groupBy(Route::fromCity)
    }

    private fun initialise() {
        // just keep it like so
        cities = ArrayList()
        with(params) {
            for (i in 0 until nAttempts) {
                val location = Vec2d(minSep + random.nextDouble((width - 2.0 * minSep)),
                        minSep + random.nextDouble((height - 2.0 * minSep)))
                val city = City(location, minSep / 2, 0)
                if (canPlace(city, cities, minSep)) cities += city
            }
        }

        for (i in 0 until cities.size) {
            // for each city we connect to all cities within a specified range
            var connections = 0
            for (j in 0 until cities.size) {
                if (i != j && cities[i].location.distanceTo(cities[j].location) <= params.autoConnect) {
                    routes += Route(i, j, cities[i].location.distanceTo(cities[j].location).toInt(), 1.0)
                    connections++
                }
            }
            while (connections < params.minConnections) {
                // then connect to random cities up to minimum
                val eligibleCities = cities.filter {
                    val distance = cities[i].location.distanceTo(it.location)
                    distance > params.autoConnect && distance <= params.maxDistance
                }.filter {
                    !routes.any { r -> r.fromCity == i && r.toCity == cities.indexOf(it) }
                }
                if (eligibleCities.isEmpty())
                    break

                val proposal = random.nextInt(eligibleCities.size)
                connections++
                val distance = cities[i].location.distanceTo(eligibleCities[proposal].location).toInt()
                routes += Route(i, proposal, distance, 1.0)
                routes += Route(proposal, i, distance, 1.0)
            }

            // TODO: Add in a check for routes to not cross each other, or cross the radius of another city
            //  TODO: Add in a check that cities for a fully connected graph
        }

        var blueBase = 0
        var redBase = 0
        while (blueBase == redBase) {
            blueBase = random.nextInt(cities.size)
            redBase = random.nextInt(cities.size)
        }
        cities[blueBase].owner = PlayerId.Blue
        cities[blueBase].pop = params.maxPop
        cities[redBase].owner = PlayerId.Red
        cities[redBase].pop = params.maxPop
    }

    fun canPlace(c: City, cities: List<City>, minSep: Int): Boolean {
        for (el in cities)
            if (c.location.distanceTo(el.location) < c.radius + el.radius + minSep) return false
        return true
    }

    fun fogTest(id: PlayerId): World {
        cities.forEach { c ->
            if (c.owner != id) {
                // fog it out
                c.owner = PlayerId.Fog
                c.pop = -1
            }
        }
        return this
    }

    fun deepCopy(): World {
        val state = copy()
        state.cities = ArrayList(cities.map { c -> City(c.location, c.radius, c.pop, c.owner) })
        state.currentTransits = ArrayList(currentTransits.filter { true }) // each Transit is immutable, but not the list of active ones
        state.currentTicks = currentTicks
        state.routes = routes       // immutable, so safe
        state.allRoutesFromCity = allRoutesFromCity // immutable, so safe
        return state
    }

    fun addTransit(transit: Transit) {
        currentTransits.add(transit)
    }

    fun removeTransit(transit: Transit) {
        if (currentTransits.contains(transit))
            currentTransits.remove(transit)
        else
            throw AssertionError("Transit to be removed is not recognised")
    }

}

data class Transit(val nPeople: Int, val fromCity: Int, val toCity: Int, val playerId: PlayerId, val startTime: Int, val endTime: Int)

data class TransitStart(val transit: Transit) : Action {
    override fun apply(state: ActionAbstractGameState): ActionAbstractGameState {
        if (state is EventQueueGame) {
            val world = state.world
            val city = world.cities[transit.fromCity]
            if (city.owner == transit.playerId) {
                if (city.pop < transit.nPeople)
                    throw AssertionError("Invalid Transit - maximum force move is limited to city population")
                city.pop -= transit.nPeople
            } else {
                throw AssertionError("Invalid Transit - must be from city owned by playerId")
            }
            world.addTransit(transit)
        }
        return state
    }
}

data class TransitEnd(val transit: Transit) : Action {
    override fun apply(state: ActionAbstractGameState): ActionAbstractGameState {
        if (state is EventQueueGame) {
            CityInflux(transit.playerId, transit.nPeople, transit.toCity).apply(state)
            state.world.removeTransit(transit)
        }
        return state
    }
}

data class CityInflux(val player: PlayerId, val pop: Int, val destination: Int) : Action {
    override fun apply(state: ActionAbstractGameState): ActionAbstractGameState {
        if (state is EventQueueGame) {
            val world = state.world
            val city = world.cities[destination]
            if (city.owner == player) {
                city.pop += pop
            } else {
                val p = world.params
                val result = lanchesterClosedFormBattle(pop.toDouble(), city.pop.toDouble(),
                        if (player == PlayerId.Blue) p.blueLanchesterCoeff else p.redLanchesterCoeff,
                        if (player == PlayerId.Blue) p.blueLanchesterExp else p.redLanchesterExp,
                        if (player == PlayerId.Blue) p.redLanchesterCoeff else p.blueLanchesterCoeff,
                        if (player == PlayerId.Blue) p.redLanchesterExp else p.blueLanchesterExp
                )
                if (result > 0.0) {
                    // attackers win
                    city.owner = player
                    city.pop = result.toInt()
                } else {
                    // defenders win
                    city.pop = -result.toInt()
                }
            }
        }
        return state
    }
}

data class Wait(val playerId: PlayerId, val wait: Int) : Action {
    override fun apply(state: ActionAbstractGameState): ActionAbstractGameState {
        if (state is EventQueueGame) {
            val world = state.world
            state.eventQueue.add(Event(world.currentTicks + wait, MakeDecision(playerId)))
        }
        return state
    }
}

data class MakeDecision(val player: PlayerId) : Action {
    override fun apply(state: ActionAbstractGameState): ActionAbstractGameState {
        if (state is EventQueueGame) {
            val agent = state.getAgent(player)
            val playerRef = when (player) {
                PlayerId.Blue -> 0
                PlayerId.Red -> 1
                else -> throw AssertionError("Decision-making not supported for $player")
            }
            val action = agent.getAction(state, playerRef)
            action.apply(state)
        }
        return state
    }
}

data class LaunchExpedition(val player: PlayerId, val from: Int, val toCode: Int, val proportion: Int, val wait: Int) : Action {

    override fun apply(state: ActionAbstractGameState): ActionAbstractGameState {
        if (state is EventQueueGame) {
            val world = state.world
            val routes = world.allRoutesFromCity[from] ?: emptyList()
            val to = routes[toCode % routes.size].toCity
            if (isValid(world.cities[from], world.cities[to])) {
                val sourceCityPop = world.cities[from].pop
                val maxActions = world.cities.size.toDouble()
                val distance = world.cities[from].location.distanceTo(world.cities[to].location)
                val arrivalTime = world.currentTicks + (distance / world.speed).toInt()
                var forcesSent = ((proportion + 1.0) / maxActions * sourceCityPop).roundToInt()
                if (forcesSent == 0) forcesSent = 1
                val transit = Transit(forcesSent, from, to, player, world.currentTicks, arrivalTime)
                // we execute the troop departure immediately
                TransitStart(transit).apply(state)
                // and put their arrival in the queue for the game state
                state.eventQueue.add(Event(arrivalTime, TransitEnd(transit)))
            }
            state.eventQueue.add(Event(world.currentTicks + wait, MakeDecision(player)))
        }
        return state
    }

    fun isValid(from: City, to: City): Boolean {
        return from.owner == player &&
                from.pop > 0 &&
                from != to
    }
}

var totalTicks: Long = 0

class EventQueueGame(val world: World = World()) : ActionAbstractGameState {

    val eventQueue = PriorityQueue<Event>()
    var scoreFunction: (EventQueueGame) -> Double = {
        // as a default we count the number of Blue cities, and subtract the number of red cities
        val blueCities = it.world.cities.count { c -> c.owner == PlayerId.Blue }
        val redCities = it.world.cities.count { c -> c.owner == PlayerId.Red }
        (blueCities - redCities).toDouble()
    }
    private val playerAgentMap = HashMap<PlayerId, SimpleActionPlayerInterface>()

    fun registerAgent(player: PlayerId, agent: SimpleActionPlayerInterface) {
        playerAgentMap[player] = agent
    }

    fun getAgent(player: PlayerId) = playerAgentMap[player] ?: SimpleActionDoNothing

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
        val playerId: PlayerId = if (player == 0) PlayerId.Blue else PlayerId.Red
        if (gene[3] == 0) return Wait(playerId, 1) // special code for Do Nothing...wait one tick and see what has changed
        return LaunchExpedition(playerId, gene[0], gene[1], gene[2], max(gene[3], world.params.defaultOODALoop))
    }

    override fun next(actions: List<Action>): EventQueueGame {
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

        actions.forEach({ a -> a.apply(this) })

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
