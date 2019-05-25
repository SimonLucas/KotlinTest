package test.junit

import games.eventqueuegame.*
import math.Vec2d
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class FogTests {

    val cityCreationParams = EventGameParams(seed = 6, minConnections = 2, autoConnect = 300, maxDistance = 1000, fogOfWar = true)
    val foggyWorld = World(params = cityCreationParams)

    @Test
    fun allCitiesAreVisible() {
        val blueCity: Int = foggyWorld.cities.withIndex().filter { (_, c) -> c.owner == PlayerId.Blue }.map { (i, _) -> i }.first()
        val neighbours = foggyWorld.allRoutesFromCity.getOrDefault(blueCity, emptyList())
                .map(Route::toCity)
                .toSet()
        val nonNeighbours = (0 until foggyWorld.cities.size).toSet() - neighbours - blueCity
        assertFalse(nonNeighbours.isEmpty())
        assert(foggyWorld.checkVisible(blueCity, PlayerId.Blue))
        assert(nonNeighbours.none { i -> foggyWorld.checkVisible(i, PlayerId.Blue) })
        assert(neighbours.all { i -> foggyWorld.checkVisible(i, PlayerId.Blue) })
    }

    @Test
    fun transitVisibility() {
        with (foggyWorld) {
            val blueCity = cities.withIndex().filter { (_, c) -> c.owner == PlayerId.Blue }.map { (i, _) -> i }.first()
            val blueToNeutral = routes.filter { r -> r.fromCity == blueCity && cities[r.toCity].owner == PlayerId.Neutral }.first()
            val blue_bn = Transit(1.0, blueCity, blueToNeutral.toCity, PlayerId.Blue, 0, 1000)
            val red_bn = Transit(1.0, blueCity, blueToNeutral.toCity, PlayerId.Red, 0, 1000)
            val blue_nb = Transit(1.0, blueToNeutral.toCity, blueCity, PlayerId.Blue, 0, 1000)
            val red_nb = Transit(1.0, blueToNeutral.toCity, blueCity, PlayerId.Red, 0, 1000)
            addTransit(blue_bn); addTransit(blue_nb); addTransit(red_bn); addTransit(red_nb)
            assert(checkVisible(blue_bn, PlayerId.Blue))
            assert(checkVisible(blue_nb, PlayerId.Blue))
            assert(checkVisible(red_bn, PlayerId.Blue))
            assert(checkVisible(red_nb, PlayerId.Blue))
            assert(!checkVisible(blue_bn, PlayerId.Red))
            assert(!checkVisible(blue_nb, PlayerId.Red))
            assert(checkVisible(red_bn, PlayerId.Red))
            assert(checkVisible(red_nb, PlayerId.Red))

            // TODO: A little silly currently, as a Transit of your colour on a route shoudl give visibility of that route
            val fogCopyRed = foggyWorld.deepCopyWithFog(PlayerId.Red)
            val fogCopyBlue = foggyWorld.deepCopyWithFog(PlayerId.Blue)
            assertEquals(fogCopyBlue.currentTransits.size, 4)
            assertEquals(fogCopyRed.currentTransits.size, 2)
            assert(fogCopyRed.currentTransits.contains(red_bn))
            assert(fogCopyRed.currentTransits.contains(red_nb))
        }
    }

    @Test
    fun testFogOnCityCopy() {
        foggyWorld.cities[0].owner = PlayerId.Blue
        val fullCopy = foggyWorld.deepCopy()
        val fogCopyRed = foggyWorld.deepCopyWithFog(PlayerId.Red)
        val fogCopyBlue = foggyWorld.deepCopyWithFog(PlayerId.Blue)
        val check = BooleanArray(4) { false }
        foggyWorld.cities.withIndex()
                .forEach { (i, c) ->
                    when (c.owner) {
                        PlayerId.Blue -> assert(fogCopyBlue.cities[i] == fullCopy.cities[i])
                        PlayerId.Red -> assert(fogCopyRed.cities[i] == fullCopy.cities[i])
                    }
                    when (Pair(fullCopy.checkVisible(i, PlayerId.Red), fullCopy.checkVisible(i, PlayerId.Blue))) {
                        Pair(false, false) -> {
                            assertEquals(fogCopyBlue.cities[i].owner, PlayerId.Fog)
                            assertEquals(fogCopyRed.cities[i].owner, PlayerId.Fog)
                            check[0] = true
                        }
                        Pair(true, true) -> {
                            assertEquals(fogCopyBlue.cities[i].owner, c.owner)
                            assertEquals(fogCopyRed.cities[i].owner, c.owner)
                            check[1] = true
                        }
                        Pair(true, false) -> {
                            assertEquals(fogCopyBlue.cities[i].owner, PlayerId.Fog)
                            assertEquals(fogCopyRed.cities[i].owner, c.owner)
                            check[2] = true
                        }
                        Pair(false, true) -> {
                            assertEquals(fogCopyBlue.cities[i].owner, c.owner)
                            assertEquals(fogCopyRed.cities[i].owner, PlayerId.Fog)
                            check[3] = true
                        }
                    }
                }
        assert(check.all { b -> b == true })
    }

    @Test
    fun rollForwardDoesNotIncludeInvisibleInvasion() {
        foggyWorld.cities[8].owner = PlayerId.Blue
        val game = EventQueueGame(foggyWorld)
        // We should now have cities 4 and 8 as Blue, and 4 as Red
        // If Red invades 1 (which is visible to 8), then Blue's projection will not see the result
        LaunchExpedition(PlayerId.Red, 3, 1, 3, 10).apply(game)
        val blueVersion = game.copy(0).next(50)
        assertEquals(blueVersion.world.cities[1].owner, PlayerId.Neutral)
        val redVersion = game.copy(1).next(50)
        assertEquals(redVersion.world.cities[1].owner, PlayerId.Red)
        val masterVersion = game.copy().next(50)
        assertEquals(masterVersion.world.cities[1].owner, PlayerId.Red)
    }
}