package test.junit


import agents.*
import games.eventqueuegame.*
import ggi.game.Action
import math.Vec2d
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.random.Random

// we create a simple world of 3 cities. One Blue and one Red, with a Neutral world sandwiched between them
val cities = listOf(
        City(Vec2d(0.0, 0.0), 0, 10, PlayerId.Blue),
        City(Vec2d(0.0, 20.0), 0, 1, PlayerId.Red),
        City(Vec2d(0.0, 10.0), 0, 0, PlayerId.Neutral)
)
val world = World(cities, 20, 20, 5.0, Random(10))
val game = EventQueueGame(world)

class TransitTest {

    @Test
    fun TransitHasMaxForce() {
        val fullInvasion = game.translateGene(0, intArrayOf(0, 2, 2, 0))
        assert(fullInvasion is LaunchExpedition)
        val gameCopy = game.copy()
        fullInvasion.apply(gameCopy)
        assertEquals(gameCopy.world.currentTransits.size, 1)
        val transit = gameCopy.world.currentTransits.first()
        assertEquals(transit.fromCity, 0)
        assertEquals(transit.toCity, 2)
        assertEquals(transit.playerId, PlayerId.Blue)
        assertEquals(transit.nPeople, 10)
        assertEquals(transit.startTime, 0)
        assertEquals(transit.endTime, 2)
    }

    @Test
    fun TransitHasMinimumOfOne() {
        val tokenInvasion = game.translateGene(1, intArrayOf(1, 0, 0, 0))
        assert(tokenInvasion is LaunchExpedition)
        val gameCopy = game.copy()
        tokenInvasion.apply(gameCopy)
        assertEquals(gameCopy.world.currentTransits.size, 1)
        val transit = gameCopy.world.currentTransits.first()
        assertEquals(transit.fromCity, 1)
        assertEquals(transit.toCity, 0)
        assertEquals(transit.playerId, PlayerId.Red)
        assertEquals(transit.nPeople, 1)
        assertEquals(transit.startTime, 0)
        assertEquals(transit.endTime, 4)
    }
}