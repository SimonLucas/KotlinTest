package games.eventqueuegame

import ggi.game.Action
import ggi.game.ActionAbstractGameState
import kotlin.math.*

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
        val playerRef = when (player) {
            PlayerId.Blue -> 0
            PlayerId.Red -> 1
            else -> throw AssertionError("Decision-making not supported for $player")
        }
        if (state is EventQueueGame) {
            val agent = state.getAgent(playerRef)
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
            val to = destinationCity(world, from, toCode)
            if (isValid(world)) {
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

    private fun destinationCity(world: World, from: Int, toCode: Int): Int {
        val routes = world.allRoutesFromCity[from] ?: emptyList()
        return routes[toCode % routes.size].toCity
    }

    fun isValid(world: World): Boolean {
        val to = destinationCity(world, from, toCode)
        return world.cities[from].owner == player &&
                world.cities[from].pop > 0 &&
                from != to
    }
}
