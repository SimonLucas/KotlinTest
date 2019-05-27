package games.eventqueuegame

import ggi.game.Action
import ggi.game.ActionAbstractGameState
import kotlin.math.*

data class TransitStart(val transit: Transit) : Action<EventQueueGame> {
    override fun apply(state: EventQueueGame): EventQueueGame {
        val world = state.world
        val city = world.cities[transit.fromCity]
        if (city.owner == transit.playerId) {
            if (city.pop < transit.nPeople)
                throw AssertionError("Invalid Transit - maximum force move is limited to city population")
            city.pop -= transit.nPeople
        } else {
            throw AssertionError("Invalid Transit - must be from city owned by playerId")
        }
        val enemyCollision = world.nextCollidingTransit(transit)
        world.addTransit(transit)
        if (enemyCollision != null) {
            state.eventQueue.add(transit.collisionEvent(enemyCollision, world))
        }
        return state
    }

    override fun visibleTo(player: Int, state: EventQueueGame) = state.world.checkVisible(transit, numberToPlayerID(player))
}

data class TransitEnd(val player: PlayerId, val fromCity: Int, val toCity: Int, val endTime: Int) : Action<EventQueueGame> {
    override fun apply(state: EventQueueGame): EventQueueGame {
        val transit = getTransit(state)
        if (transit != null) {
            CityInflux(player, transit.nPeople, toCity, fromCity).apply(state)
            state.world.removeTransit(transit)
        }
        return state
    }

    override fun visibleTo(player: Int, state: EventQueueGame): Boolean {
        val transit = getTransit(state)
        return transit != null && state.world.checkVisible(transit, numberToPlayerID(player))
    }

    private fun getTransit(state: EventQueueGame): Transit? {
        return state.world.currentTransits.filter {
            it.endTime == endTime && it.playerId == player && it.fromCity == fromCity && it.toCity == toCity
        }.firstOrNull()
    }
}

data class CityInflux(val player: PlayerId, val pop: Double, val destination: Int, val origin: Int = -1) : Action<EventQueueGame> {
    override fun apply(state: EventQueueGame): EventQueueGame {
        val world = state.world
        val city = world.cities[destination]
        if (city.owner == player) {
            city.pop += pop
        } else {
            val p = world.params
            val result = lanchesterClosedFormBattle(pop, city.pop,
                    (if (player == PlayerId.Blue) p.blueLanchesterCoeff else p.redLanchesterCoeff)
                            / if (city.fort) p.fortAttackerCoeffDivisor else 1.0,
                    if (player == PlayerId.Blue) p.blueLanchesterExp else p.redLanchesterExp,
                    if (player == PlayerId.Blue) p.redLanchesterCoeff else p.blueLanchesterCoeff,
                    (if (player == PlayerId.Blue) p.redLanchesterExp else p.blueLanchesterExp)
                            + if (city.fort) p.fortDefenderExpIncrease else 0.0
            )
            if (result > 0.0) {
                // attackers win
                city.owner = player
                city.pop = result
            } else {
                // defenders win
                city.pop = -result
            }
        }
        return state
    }

    override fun visibleTo(player: Int, state: EventQueueGame): Boolean {
        // we can't just check the destination city...as what matters is where the force is coming *from*
        // i.e. which Route they are travelling on
        val playerId = numberToPlayerID(player)
        with(state.world) {
            if (origin == -1) return this@CityInflux.player == playerId || checkVisible(destination, playerId)
            return checkVisible(Transit(0.0, origin, destination, this@CityInflux.player, 0, 0), playerId)
            // If we could see a Transit by another player on that route, then we can see the CityInflux
        }
    }
}

data class Battle(val transit1: Transit, val transit2: Transit) : Action<EventQueueGame> {
    override fun apply(state: EventQueueGame): EventQueueGame {
        val p = state.world.params
        val result = lanchesterClosedFormBattle(transit1.nPeople, transit2.nPeople,
                if (transit1.playerId == PlayerId.Blue) p.blueLanchesterCoeff else p.redLanchesterCoeff,
                if (transit1.playerId == PlayerId.Blue) p.blueLanchesterExp else p.redLanchesterExp,
                if (transit1.playerId == PlayerId.Blue) p.redLanchesterCoeff else p.blueLanchesterCoeff,
                if (transit1.playerId == PlayerId.Blue) p.redLanchesterExp else p.blueLanchesterExp
        )
        val winningTransit = if (result > 0.0) transit1 else transit2
        val losingTransit = if (result > 0.0) transit2 else transit1

        state.world.removeTransit(losingTransit)
        state.world.removeTransit(winningTransit)
        if (Math.abs(result).toInt() == 0) {
            // do nothing
        } else {
            val successorTransit = winningTransit.copy(nPeople = Math.abs(result));
            state.world.addTransit(successorTransit)
            val nextCollidingTransit = state.world.nextCollidingTransit(successorTransit)
            if (nextCollidingTransit != null) {
                state.eventQueue.add(successorTransit.collisionEvent(nextCollidingTransit, state.world))
            }
        }
        return state
    }

    override fun visibleTo(player: Int, state: EventQueueGame): Boolean {
        val playerId = numberToPlayerID(player)
        return state.world.checkVisible(transit1, playerId) && state.world.checkVisible(transit2, playerId)
    }
}

data class Wait(val playerId: PlayerId, val wait: Int) : Action<EventQueueGame> {
    override fun apply(state: EventQueueGame): EventQueueGame {
        val world = state.world
        state.eventQueue.add(Event(world.currentTicks + wait, MakeDecision(playerId)))
        return state
    }

    // only visible to planning player
    override fun visibleTo(player: Int, state: EventQueueGame) = player == playerIDToNumber(playerId)
}

data class MakeDecision(val player: PlayerId) : Action<EventQueueGame> {
    override fun apply(state: EventQueueGame): EventQueueGame {
        val playerRef = playerIDToNumber(player)
        val agent = state.getAgent(playerRef)
        val action = agent.getAction(state, playerRef)
        (action as Action<EventQueueGame>).apply(state)
        return state
    }

    // only visible to planning player
    override fun visibleTo(player: Int, state: EventQueueGame) = player == playerIDToNumber(this.player)
}

data class LaunchExpedition(val player: PlayerId, val from: Int, val toCode: Int, val proportion: Int, val wait: Int) : Action<EventQueueGame> {

    override fun apply(state: EventQueueGame): EventQueueGame {
        val world = state.world
        val to = destinationCity(world, from, toCode)
        if (isValid(world)) {
            val sourceCityPop = world.cities[from].pop
            val maxActions = world.cities.size.toDouble()
            val distance = world.cities[from].location.distanceTo(world.cities[to].location)
            val arrivalTime = world.currentTicks + (distance / world.params.speed).toInt()
            var forcesSent = ((proportion + 1.0) / maxActions * sourceCityPop)
            if (forcesSent < 1.0) forcesSent = Math.min(1.0, sourceCityPop)
            val transit = Transit(forcesSent, from, to, player, world.currentTicks, arrivalTime)
            // we execute the troop departure immediately
            TransitStart(transit).apply(state)
            // and put their arrival in the queue for the game state
            state.eventQueue.add(Event(arrivalTime, TransitEnd(transit.playerId, transit.fromCity, transit.toCity, transit.endTime)))
        }
        state.eventQueue.add(Event(world.currentTicks + wait, MakeDecision(player)))
        return state
    }

    private fun destinationCity(world: World, from: Int, toCode: Int): Int {
        val routes = world.allRoutesFromCity[from] ?: emptyList()
        if (routes.isEmpty())
            throw AssertionError("Should not be empty")
        return routes[toCode % routes.size].toCity
    }

    fun isValid(world: World): Boolean {
        val to = destinationCity(world, from, toCode)
        return world.cities[from].owner == player &&
                world.cities[from].pop > 0 &&
                from != to
    }

    // only visible to planning player
    override fun visibleTo(player: Int, state: EventQueueGame) = player == playerIDToNumber(this.player)
}
