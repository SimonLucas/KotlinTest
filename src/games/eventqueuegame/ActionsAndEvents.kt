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
            val enemyCollision = world.nextCollidingTransit(transit)
            world.addTransit(transit)
            if (enemyCollision != null) {
                state.eventQueue.add(transit.collisionEvent(enemyCollision, world))
            }
        }
        return state
    }
}

data class TransitEnd(val player: PlayerId, val fromCity: Int, val toCity: Int, val endTime: Int) : Action {
    override fun apply(state: ActionAbstractGameState): ActionAbstractGameState {
        if (state is EventQueueGame) {
            val transit = state.world.currentTransits.filter {
                it.endTime == endTime && it.playerId == player && it.fromCity == fromCity && it.toCity == toCity
            }.firstOrNull()
            if (transit != null) {
                CityInflux(player, transit.nPeople, toCity).apply(state)
                state.world.removeTransit(transit)
            }
        }
        return state
    }
}

data class CityInflux(val player: PlayerId, val pop: Double, val destination: Int) : Action {
    override fun apply(state: ActionAbstractGameState): ActionAbstractGameState {
        if (state is EventQueueGame) {
            val world = state.world
            val city = world.cities[destination]
            if (city.owner == player) {
                city.pop += pop
            } else {
                fun attackExponent(start: Double, fort: Boolean): Double = if (fort) Math.max(0.0, start - 0.5) else start
                fun attackCoefficient(start: Double, fort: Boolean): Double = start / if (fort) 3.0 else 1.0
                val p = world.params
                val result = lanchesterClosedFormBattle(pop, city.pop,
                        if (player == PlayerId.Blue) p.blueLanchesterCoeff else p.redLanchesterCoeff,
                        attackExponent(if (player == PlayerId.Blue) p.blueLanchesterExp else p.redLanchesterExp, city.fort),
                        if (player == PlayerId.Blue) p.redLanchesterCoeff else p.blueLanchesterCoeff,
                        attackCoefficient(if (player == PlayerId.Blue) p.redLanchesterExp else p.blueLanchesterExp, city.fort)
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
        }
        return state
    }
}

data class Battle(val transit1: Transit, val transit2: Transit) : Action {
    override fun apply(state: ActionAbstractGameState): ActionAbstractGameState {
        if (state is EventQueueGame) {
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
        }
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
}
