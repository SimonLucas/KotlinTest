package test.junit


import games.eventqueuegame.*
import math.Vec2d
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LanchesterTest {
    @Test
    fun lanchesterOutnumber() {
        val defenceResult = lanchesterLinearBattle(10.0, 20.0, 0.05, 0.05)
        assertEquals(defenceResult, lanchesterClosedFormBattle(10.0, 20.0, 0.05, 1.0, 0.05, 1.0), 0.5)

        val attackResult = lanchesterLinearBattle(20.0, 10.0, 0.05, 0.05)
        assertEquals(attackResult, lanchesterClosedFormBattle(20.0, 10.0, 0.05, 1.0, 0.05, 1.0), 0.5)
    }

    @Test
    fun lanchesterTie() {
        assertEquals(0.0, lanchesterClosedFormBattle(10.0, 10.0, 0.05, 0.0, 0.05, 0.0), 0.05)
        assertEquals(0.0, lanchesterClosedFormBattle(10.0, 10.0, 0.05, 1.0, 0.05, 1.0), 0.05)
        assertEquals(0.0, lanchesterClosedFormBattle(10.0, 10.0, 0.1, 0.5, 0.1, 0.5), 0.05)
    }

    @Test
    fun lanchesterExponentVariation() {
        val result_1_1 = lanchesterClosedFormBattle(20.0, 10.0, 0.05, 1.0, 0.05, 1.0)
        val result_1_0 = lanchesterClosedFormBattle(20.0, 10.0, 0.05, 1.0, 0.05, 0.0)
        val result_0_1 = lanchesterClosedFormBattle(20.0, 10.0, 0.05, 0.0, 0.05, 1.0)

        // result is the surviving attacking force
        assert(result_1_1 < result_1_0)
        assert(result_1_1 > result_0_1)
        assert(result_1_0 > result_0_1)
    }

    @Test
    fun lanchesterCoefficientVariation() {
        val result_1_1 = lanchesterClosedFormBattle(15.0, 20.0, 0.10, 0.5, 0.10, 0.5)
        val result_1_0 = lanchesterClosedFormBattle(15.0, 20.0, 0.10, 0.5, 0.05, 0.5)
        val result_0_1 = lanchesterClosedFormBattle(15.0, 20.0, 0.05, 0.5, 0.10, 0.5)

        // result is the surviving attacking force
        assert(result_1_1 < result_1_0)
        assert(result_1_1 > result_0_1)
        assert(result_1_0 > result_0_1)
    }

    @Test
    fun fortVariationsBlueOnRed() {
        val cities = listOf(City(Vec2d(10.0, 10.0), pop = 20.0, owner = PlayerId.Red, fort = true),
                City(Vec2d(20.0, 20.0), pop = 20.0, owner = PlayerId.Red, fort = false))
        val routes = listOf(Route(0, 1, 15.0, 1.0),
                Route(1, 0, 15.0, 1.0))
        val world = World(cities, routes)
        assertEquals(world.routes.size, 2)
        val state = LandCombatGame(world)

        CityInflux(PlayerId.Blue, 30.0, 0).apply(state)
        CityInflux(PlayerId.Blue, 30.0, 1).apply(state)

        assert(cities[0].owner == PlayerId.Red)
        assert(cities[1].owner == PlayerId.Blue)

        assert(Math.abs(cities[0].pop - 18.5) < 0.1)
        assert(Math.abs(cities[1].pop - 22.4) < 0.1)
    }

    @Test
    fun fortVariationsRedOnBlue() {
        val cities = listOf(City(Vec2d(10.0, 10.0), pop = 20.0, owner = PlayerId.Blue, fort = true),
                City(Vec2d(20.0, 20.0), pop = 20.0, owner = PlayerId.Blue, fort = false))
        val routes = listOf(Route(0, 1, 15.0, 1.0),
                Route(1, 0, 15.0, 1.0))
        val world = World(cities, routes)
        assertEquals(world.routes.size, 2)
        val state = LandCombatGame(world)

        CityInflux(PlayerId.Red, 30.0, 0).apply(state)
        CityInflux(PlayerId.Red, 30.0, 1).apply(state)

        assert(cities[0].owner == PlayerId.Blue)
        assert(cities[1].owner == PlayerId.Red)

        assert(Math.abs(cities[0].pop - 18.5) < 0.1)
        assert(Math.abs(cities[1].pop - 22.4) < 0.1)
    }
}