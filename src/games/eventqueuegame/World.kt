package games.eventqueuegame

import math.Vec2d
import kotlin.random.Random

enum class PlayerId {
    Blue, Red, Neutral, Fog
}

fun playerIDToNumber(playerID: PlayerId): Int {
    return when (playerID) {
        PlayerId.Blue -> 0
        PlayerId.Red -> 1
        PlayerId.Neutral -> 99
        PlayerId.Fog -> -1
    }
}

fun numberToPlayerID(player: Int): PlayerId {
    return when (player) {
        0 -> PlayerId.Blue
        1 -> PlayerId.Red
        else -> throw java.lang.AssertionError("Only 0 and 1 supported")
    }
}


data class City(val location: Vec2d, val radius: Int = 40, var pop: Double = 100.0,
                var owner: PlayerId = PlayerId.Neutral, val name: String = "", val fort: Boolean = false)

data class Route(val fromCity: Int, val toCity: Int, val length: Double, val terrainDifficulty: Double)

fun routesCross(start: Vec2d, end: Vec2d, routesToCheck: List<Route>, cities: List<City>): Boolean {
    return routesToCheck.any { r -> routesCross(start, end, cities[r.fromCity].location, cities[r.toCity].location) }
}

fun allCitiesConnected(routes: List<Route>, cities: List<City>): Boolean {
    // check to see if we can get from every city to every other city
    // We start from any one city, and make sure we can reach the others

    var connectedCities = setOf(0)
    do {
        val networkSizeOnLastIteration = connectedCities.size
        connectedCities = routes.filter { r -> r.fromCity in connectedCities }
                .map(Route::toCity)
                .toSet() + connectedCities
    } while (networkSizeOnLastIteration != connectedCities.size)
    return connectedCities.size == cities.size
}

fun routesCross(start1: Vec2d, end1: Vec2d, start2: Vec2d, end2: Vec2d): Boolean {
    // If we consider the two lines in the form parameterised by t: start + t(end - start)
    // then t is between 0 and 1 on each line.
    // what this code does (hopefully) is calculate the two t parameters of for the two lines (as they will cross somewhere, unless parallel)
    // and check that both parameters are in the range that mean the lines cross between the locations

    //   println("Checking $start1 -> $end1 against $start2 -> $end2")
    // first check if lines are parallel
//    if ((end1.y - start1.y) / (end1.x - start1.x) == (end2.y - start2.y) / (end2.x - start2.x)) return false

    val t2 = ((start2.y - start1.y) * (end1.x - start1.x) - (start2.x - start1.x) * (end1.y - start1.y)) /
            ((end2.x - start2.x) * (end1.y - start1.y) - (end2.y - start2.y) * (end1.x - start1.x))
    val t1 = ((start2.x - start1.x) + t2 * (end2.x - start2.x)) / (end1.x - start1.x)

    //   println("\tt1 = $t1 and t2 = $t2")
    return (t2 in 0.001..0.999 && t1 in 0.001..0.999)
}

data class Transit(val nPeople: Double, val fromCity: Int, val toCity: Int, val playerId: PlayerId, val startTime: Int, val endTime: Int) {
    fun currentPosition(time: Int, cities: List<City>): Vec2d {
        val proportion: Double = (time - startTime).toDouble() / (endTime - startTime).toDouble()
        return cities[fromCity].location + (cities[toCity].location - cities[fromCity].location) * proportion
    }

    fun collisionEvent(otherTransit: Transit, world: World, currentTime: Int): Event {
        val currentEnemyPosition = otherTransit.currentPosition(currentTime, world.cities)
        val ourPosition = this.currentPosition(currentTime, world.cities)
        val distance = ourPosition.distanceTo(currentEnemyPosition) / 2.0
        val timeOfCollision = currentTime + (distance / world.params.speed).toInt()
        return Event(timeOfCollision, Battle(this, otherTransit))
    }
}

data class World(var cities: List<City> = ArrayList(), var routes: List<Route> = ArrayList(),
                 val width: Int = 1000, val height: Int = 600,
                 val random: Random = Random(3),
                 val params: EventGameParams = EventGameParams()) {

    var currentTransits: ArrayList<Transit> = ArrayList()
        private set(newTransits) {
            field = newTransits
        }

    var allRoutesFromCity: Map<Int, List<Route>> = HashMap()

    init {
        if (cities.isEmpty()) initialise()
        allRoutesFromCity = routes.groupBy(Route::fromCity)
    }

    private fun initialise() {

        // just keep it like so
        cities = ArrayList()
        with(params) {
            var n = 0
            for (i in 0 until nAttempts) {
                val isFort = random.nextDouble() < percentFort;
                val location = Vec2d(minRad + random.nextDouble((width - 2.0 * minRad)),
                        minRad + random.nextDouble((height - 2.0 * minRad)))
                val city = City(location, minRad, 0.0, name = n.toString(), fort = isFort)
                if (canPlace(city, cities, citySeparation)) {
                    cities += city
                    n++
                }
            }
        }

        for (i in 0 until cities.size) {
            // for each city we connect to all cities within a specified range
            for (j in 0 until cities.size) {
                if (i != j && cities[i].location.distanceTo(cities[j].location) <= params.autoConnect
                        && !routesCross(cities[i].location, cities[j].location, routes, cities)) {
                    routes += Route(i, j, cities[i].location.distanceTo(cities[j].location), 1.0)
                }
            }
            while (routes.filter { r -> r.fromCity == i }.size < params.minConnections) {
                // then connect to random cities up to minimum
                linkRandomCityTo(i)
            }
        }


        var count = 0;
        while (!allCitiesConnected(routes, cities)) {
            linkRandomCityTo(random.nextInt(cities.size))
            count++
            if (count > 50) {
                throw AssertionError("WTF")
            }
        }

        var blueBase = 0
        var redBase = 0
        while (blueBase == redBase) {
            blueBase = random.nextInt(cities.size)
            redBase = random.nextInt(cities.size)
        }
        cities[blueBase].owner = PlayerId.Blue
        cities[blueBase].pop = params.blueForce.toDouble()
        cities[redBase].owner = PlayerId.Red
        cities[redBase].pop = params.redForce.toDouble()
    }

    private fun linkRandomCityTo(cityIndex: Int): Boolean {
        val eligibleCities = cities.filter {
            val distance = cities[cityIndex].location.distanceTo(it.location)
            distance > params.autoConnect && distance <= params.maxDistance
        }.filter {
            !routes.any { r -> r.fromCity == cityIndex && r.toCity == cities.indexOf(it) }
        }.filter {
            !routesCross(cities[cityIndex].location, it.location, routes, cities)
        }
        if (eligibleCities.isEmpty())
            return false

        val proposal = eligibleCities[random.nextInt(eligibleCities.size)]
        val distance = cities[cityIndex].location.distanceTo(proposal.location)
        routes += Route(cityIndex, cities.indexOf(proposal), distance, 1.0)
        routes += Route(cities.indexOf(proposal), cityIndex, distance, 1.0)
        return true
    }

    fun canPlace(c: City, cities: List<City>, minSep: Int): Boolean {
        for (el in cities)
            if (c.location.distanceTo(el.location) < c.radius + el.radius + minSep) return false
        return true
    }

    fun deepCopyWithFog(perspective: PlayerId): World {
        val state = copy()
        state.cities = ArrayList(cities.withIndex().map { (i, c) ->
            if (checkVisible(i, perspective)) City(c.location, c.radius, c.pop, c.owner, c.name, c.fort)
            else City(c.location, c.radius, 0.0, PlayerId.Fog, c.name, c.fort)
        })
        state.currentTransits = ArrayList(currentTransits.filter { t -> checkVisible(t, perspective) }) // each Transit is immutable, but not the list of active ones
        state.routes = routes       // immutable, so safe
        state.allRoutesFromCity = allRoutesFromCity // immutable, so safe
        return state
    }

    fun deepCopy(): World {
        val state = copy()
        state.cities = ArrayList(cities.map { c -> City(c.location, c.radius, c.pop, c.owner, c.name, c.fort) })
        state.currentTransits = ArrayList(currentTransits.filter { true }) // each Transit is immutable, but not the list of active ones
        state.routes = routes       // immutable, so safe
        state.allRoutesFromCity = allRoutesFromCity // immutable, so safe
        return state
    }

    fun checkVisible(city: Int, perspective: PlayerId): Boolean {
        if (!params.fogOfWar) return true
        return (cities[city].owner == perspective) ||
                routes.any { r -> r.toCity == city && cities[r.fromCity].owner == perspective } ||
                currentTransits.any { t -> t.playerId == perspective && (t.toCity == city || t.fromCity == city) }
    }

    fun checkVisible(transit: Transit, perspective: PlayerId): Boolean {
        if (!params.fogOfWar) return true
        return transit.playerId == perspective ||
                cities[transit.toCity].owner == perspective ||
                cities[transit.fromCity].owner == perspective ||
                currentTransits.any { t ->
                    t.playerId == perspective && (
                            (t.toCity == transit.toCity && t.fromCity == transit.fromCity)
                                    || (t.toCity == transit.fromCity && t.fromCity == transit.toCity))
                }
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

    fun nextCollidingTransit(newTransit: Transit, currentTime: Int): Transit? {
        if (currentTransits.any {
                    it.fromCity == newTransit.fromCity
                            && it.toCity == newTransit.toCity
                            && it.playerId == newTransit.playerId
                            && it !== newTransit
                }) return null
        // the check above looks for any pre-existing force by the same player on the arc. If one exists, then it will fight a battle first
        val collidingTransit = currentTransits.filter {
            it.fromCity == newTransit.toCity
                    && it.toCity == newTransit.fromCity
                    && it.playerId != newTransit.playerId
                    && it.endTime > currentTime
        }.minBy(Transit::endTime)
        // find the transit on the rout closest to us
        return collidingTransit
    }

}