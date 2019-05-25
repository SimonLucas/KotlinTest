package test.junit

import games.eventqueuegame.*
import math.Vec2d
import org.junit.jupiter.api.*
import kotlin.random.Random
import kotlin.test.*

object CityLocationTest {

    @Test
    fun routeCrossingDetectionTest() {
        assertTrue(routesCross(Vec2d(0.0, 0.0), Vec2d(1.0, 1.0), Vec2d(0.4, 1.0), Vec2d(0.6, 0.0)))
        assertFalse(routesCross(Vec2d(0.0, 0.0), Vec2d(0.6, 0.0), Vec2d(0.4, 1.0), Vec2d(0.6, 0.0)))
        assertTrue(routesCross(Vec2d(0.0, 0.0), Vec2d(0.6, 0.0), Vec2d(0.4, 1.0), Vec2d(0.4, -1.0)))
        assertFalse(routesCross(Vec2d(1.0, 1.0), Vec2d(0.5, 0.0), Vec2d(0.6, 1.0), Vec2d(0.2, -1.0)))
        assertTrue(routesCross(Vec2d(0.0, 0.0), Vec2d(1.0, 1.0), Vec2d(0.5, 1.0), Vec2d(0.5, 0.0)))
        assertFalse(routesCross(Vec2d(0.0, 0.0), Vec2d(1.0, 1.0), Vec2d(0.1, 0.1), Vec2d(1.1, 1.1)))

        assertFalse(routesCross(Vec2d(772.8, 413.6), Vec2d(833.9, 123.9), Vec2d(836.5, 530.6), Vec2d(772.8, 413.6)))
        assertFalse(routesCross(Vec2d(833.9, 123.9), Vec2d(772.8, 413.6), Vec2d(836.5, 530.6), Vec2d(772.8, 413.6)))
        assertFalse(routesCross(Vec2d(222.1, 346.5), Vec2d(116.2, 204.8), Vec2d(175.8, 488.6), Vec2d(116.2, 204.8)))
    }

    @Test
    fun fullyConnectedNetworkTest() {
        for (i in 1..25) {
            val world = World(random = Random(i))
            assertTrue(allCitiesConnected(world.routes, world.cities))
        }
    }
}

object CityCreationTest {

    val cityCreationParams = EventGameParams(seed = 3, minConnections = 2, autoConnect = 300, maxDistance = 1000)
    val cityCreationWorld = World(params = cityCreationParams)

    @Test
    fun allCitiesHaveTwoMinimumConnections() {
        for ((i, _) in cityCreationWorld.cities.withIndex()) {
            assert(cityCreationWorld.allRoutesFromCity[i]?.size ?: 0 >= 2)
        }
    }

    @Test
    fun allCitiesHaveThreeMinimumConnections() {
        val localCityCreationWorld = World(params = cityCreationParams.copy(minConnections = 3))
        for ((i, _) in localCityCreationWorld.cities.withIndex()) {
            assert(localCityCreationWorld.allRoutesFromCity[i]?.size ?: 0 >= 3)
        }
    }

    @Test
    fun allCitiesHaveConnectionsToNeighbours() {
        with(cityCreationWorld) {
            val allCityPairs = cities.flatMap { c1 -> cities.map { c2 -> c1 to c2 } }
            val allRoutes = routes.map { r -> cities[r.fromCity] to cities[r.toCity] }
            for ((c1, c2) in allCityPairs) {
                if (c1 != c2 && c1.location.distanceTo(c2.location) <= cityCreationParams.minConnections) {
                    assert((c1 to c2) in allRoutes)
                    assert((c2 to c1) in allRoutes)
                }
            }
        }
    }

    @Test
    fun noDuplicateRoutes() {
        with(cityCreationWorld) {
            assertEquals(routes.size, routes.distinct().size)
        }
    }
}

object CityCopyTest {

    val cityCreationParams = EventGameParams(seed = 6, minConnections = 2, autoConnect = 300, maxDistance = 1000)
    val world = World(params = cityCreationParams)

    @Test
    fun fortStatusIsCopied() {
        val city1 = City(Vec2d(10.0, 10.0), fort = true)
        val city2 = city1.copy()
        assert(city2.fort)

        val world = World(listOf(city1))
        assert(world.deepCopy().cities[0].fort)
    }

    @Test
    fun allCitiesAreVisible() {
        val blueCity: Int = world.cities.withIndex().filter { (_, c) -> c.owner == PlayerId.Blue }.map { (i, _) -> i }.first()
        val neighbours = world.allRoutesFromCity.getOrDefault(blueCity, emptyList())
                .map(Route::toCity)
                .toSet()
        val nonNeighbours = (0 until world.cities.size).toSet() - neighbours - blueCity
        assertFalse(nonNeighbours.isEmpty())
        assert(world.checkVisible(blueCity, PlayerId.Blue))
        assert(nonNeighbours.all { i -> world.checkVisible(i, PlayerId.Blue) })
        assert(neighbours.all { i -> world.checkVisible(i, PlayerId.Blue) })
    }
}
