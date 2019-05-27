package test.junit

import games.eventqueuegame.*
import math.Vec2d
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class FogTests {

    val cityCreationParams = EventGameParams(seed = 6, minConnections = 2, autoConnect = 300, maxDistance = 1000, fogOfWar = true)
    val foggyWorld = World(params = cityCreationParams)
    //

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
    fun transitVisibilityFromDestinationCity() {
        with(foggyWorld) {
            val blueCity = cities.withIndex().filter { (_, c) -> c.owner == PlayerId.Blue }.map { (i, _) -> i }.first()
            val redCity = cities.withIndex().filter { (_, c) -> c.owner == PlayerId.Red }.map { (i, _) -> i }.first()
            val blueToNeutral = routes.filter { r -> r.fromCity == blueCity && cities[r.toCity].owner == PlayerId.Neutral }.first()
            val redToNeutral = routes.filter { r -> r.fromCity == redCity && cities[r.toCity].owner == PlayerId.Neutral }.first()
            val blue_nb = Transit(1.0, blueToNeutral.toCity, blueCity, PlayerId.Blue, 0, 1000)
            val red_nr = Transit(1.0, redToNeutral.toCity, redCity, PlayerId.Red, 0, 1000)
            addTransit(blue_nb); addTransit(red_nr)

            assert(checkVisible(blue_nb, PlayerId.Blue))
            assert(!checkVisible(red_nr, PlayerId.Blue))
            assert(!checkVisible(blue_nb, PlayerId.Red))
            assert(checkVisible(red_nr, PlayerId.Red))

            val fogCopyRed = foggyWorld.deepCopyWithFog(PlayerId.Red)
            val fogCopyBlue = foggyWorld.deepCopyWithFog(PlayerId.Blue)
            assertEquals(fogCopyBlue.currentTransits.size, 1)
            assert(fogCopyBlue.currentTransits.contains(blue_nb))
            assertEquals(fogCopyRed.currentTransits.size, 1)
            assert(fogCopyRed.currentTransits.contains(red_nr))
        }
    }

    @Test
    fun transitVisibilityFromSourceCity() {
        with(foggyWorld) {
            val blueCity = cities.withIndex().filter { (_, c) -> c.owner == PlayerId.Blue }.map { (i, _) -> i }.first()
            val redCity = cities.withIndex().filter { (_, c) -> c.owner == PlayerId.Red }.map { (i, _) -> i }.first()
            val blueToNeutral = routes.filter { r -> r.fromCity == blueCity && cities[r.toCity].owner == PlayerId.Neutral }.first()
            val redToNeutral = routes.filter { r -> r.fromCity == redCity && cities[r.toCity].owner == PlayerId.Neutral }.first()
            val blue_bn = Transit(1.0, blueCity, blueToNeutral.toCity, PlayerId.Blue, 0, 1000)
            val red_rn = Transit(1.0, redCity, redToNeutral.toCity, PlayerId.Red, 0, 1000)
            addTransit(blue_bn); addTransit(red_rn);

            assert(checkVisible(blue_bn, PlayerId.Blue))
            assert(!checkVisible(red_rn, PlayerId.Blue))
            assert(!checkVisible(blue_bn, PlayerId.Red))
            assert(checkVisible(red_rn, PlayerId.Red))

            val fogCopyRed = foggyWorld.deepCopyWithFog(PlayerId.Red)
            val fogCopyBlue = foggyWorld.deepCopyWithFog(PlayerId.Blue)
            assertEquals(fogCopyBlue.currentTransits.size, 1)
            assert(fogCopyBlue.currentTransits.contains(blue_bn))
            assertEquals(fogCopyRed.currentTransits.size, 1)
            assert(fogCopyRed.currentTransits.contains(red_rn))
        }
    }

    @Test
    fun transitVisibilityToOtherPlayerAtDestination() {
        with(foggyWorld) {
            val blueCity = cities.withIndex().filter { (_, c) -> c.owner == PlayerId.Blue }.map { (i, _) -> i }.first()
            val blueToRed = routes.filter { r -> r.fromCity == blueCity && cities[r.toCity].owner == PlayerId.Neutral }.first()
            cities[blueToRed.toCity].owner = PlayerId.Red

            val blue_br = Transit(1.0, blueCity, blueToRed.toCity, PlayerId.Blue, 0, 1000)
            addTransit(blue_br)

            assert(checkVisible(blue_br, PlayerId.Blue))
            assert(checkVisible(blue_br, PlayerId.Red))

            val fogCopyRed = foggyWorld.deepCopyWithFog(PlayerId.Red)
            val fogCopyBlue = foggyWorld.deepCopyWithFog(PlayerId.Blue)
            assertEquals(fogCopyBlue.currentTransits.size, 1)
            assertEquals(fogCopyRed.currentTransits.size, 1)
        }
    }

    @Test
    fun transitVisibilityOnSameRoute() {
        with(foggyWorld) {
            val blueCity = cities.withIndex().filter { (_, c) -> c.owner == PlayerId.Blue }.map { (i, _) -> i }.first()
            val blueToNeutral = routes.filter { r -> r.fromCity == blueCity && cities[r.toCity].owner == PlayerId.Neutral }.first()
            val blue_bn = Transit(1.0, blueCity, blueToNeutral.toCity, PlayerId.Blue, 0, 1000)
            val red_bn = Transit(1.0, blueCity, blueToNeutral.toCity, PlayerId.Red, 0, 1000)
            val blue_nb = Transit(1.0, blueToNeutral.toCity, blueCity, PlayerId.Blue, 0, 1000)
            val red_nb = Transit(1.0, blueToNeutral.toCity, blueCity, PlayerId.Red, 0, 1000)
            addTransit(blue_bn); addTransit(blue_nb); addTransit(red_bn); addTransit(red_nb)
            // these are all visible because Transits can see each other on the same route
            assert(checkVisible(blue_bn, PlayerId.Blue))
            assert(checkVisible(blue_nb, PlayerId.Blue))
            assert(checkVisible(red_bn, PlayerId.Blue))
            assert(checkVisible(red_nb, PlayerId.Blue))
            assert(checkVisible(blue_bn, PlayerId.Red))
            assert(checkVisible(blue_nb, PlayerId.Red))
            assert(checkVisible(red_bn, PlayerId.Red))
            assert(checkVisible(red_nb, PlayerId.Red))

            val fogCopyRed = foggyWorld.deepCopyWithFog(PlayerId.Red)
            val fogCopyBlue = foggyWorld.deepCopyWithFog(PlayerId.Blue)
            assertEquals(fogCopyBlue.currentTransits.size, 4)
            assertEquals(fogCopyRed.currentTransits.size, 4)
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
        // We should now have cities 4 and 8 as Blue, and 3 as Red
        // If Red invades 1 (which is visible to 8), then Blue's projection will not see the result
        LaunchExpedition(PlayerId.Red, 3, 1, 3, 10).apply(game)
        val blueVersion = game.copy(0).next(50)
        assertEquals(blueVersion.world.cities[1].owner, PlayerId.Neutral)
        val redVersion = game.copy(1).next(50)
        assertEquals(redVersion.world.cities[1].owner, PlayerId.Red)
        val masterVersion = game.copy().next(50)
        assertEquals(masterVersion.world.cities[1].owner, PlayerId.Red)
    }

    @Test
    fun copyWithPerspectiveClearsOutInvisibleFutureActionsInQueue() {
        // we want one event of each type
        // 4 is Blue, 3 is Red
        // 1 is visible to both 3 and 4
        foggyWorld.cities[8].owner = PlayerId.Blue
        foggyWorld.cities[8].pop = 5.0
        val game = EventQueueGame(foggyWorld)
        game.eventQueue.add(Event(10, MakeDecision(PlayerId.Red)))  // R
        game.eventQueue.add(Event(10, MakeDecision(PlayerId.Blue))) // B
        assertFalse(game.world.checkVisible(6, PlayerId.Blue))
        game.eventQueue.add(Event(10, CityInflux(PlayerId.Red, 10.0, 6))) // R
        game.eventQueue.add(Event(10, CityInflux(PlayerId.Red, 10.0, 1, 3))) // RB
        game.eventQueue.add(Event(10, CityInflux(PlayerId.Blue, 10.0, 1, 4))) // RB
        game.eventQueue.add(Event(10, CityInflux(PlayerId.Blue, 10.0, 2, 4))) // B
        game.eventQueue.add(Event(10, TransitStart(Transit(10.0, 4, 2, PlayerId.Blue, 11, 20)))) // B
        game.world.addTransit(Transit(4.0, 4, 2, PlayerId.Blue, 10, 20))
        game.eventQueue.add(Event(10, TransitEnd(PlayerId.Blue, 4, 2, 20))) // B
        game.world.addTransit(Transit(4.0, 4, 1, PlayerId.Blue, 10, 20))
        game.eventQueue.add(Event(10, TransitEnd(PlayerId.Blue, 4, 1, 20))) // B
        game.world.addTransit(Transit(2.0, 1, 3, PlayerId.Blue, 10, 20))
        game.eventQueue.add(Event(14, TransitEnd(PlayerId.Blue, 1, 3, 20))) // RB
        val blueForce = Transit(5.0, 1, 3, PlayerId.Blue, 10, 20)
        val redForce = Transit(2.0, 3, 1, PlayerId.Red, 10, 20)
        game.world.addTransit(blueForce); game.world.addTransit(redForce)
        game.eventQueue.add(Event(11, Battle(blueForce, redForce))) // RB
        game.eventQueue.add(Event(20, Wait(PlayerId.Red, 5)))  // R
        game.eventQueue.add(Event(20, Wait(PlayerId.Blue, 5))) // B
        game.eventQueue.add(Event(13, LaunchExpedition(PlayerId.Red, 3, 1, 3, 10))) // R
        game.eventQueue.add(Event(13, LaunchExpedition(PlayerId.Blue, 8, 1, 3, 10))) // B

        val blueVersion = game.copy(0).next(5)
        val redVersion = game.copy(1).next(5)
        val masterVersion = game.copy().next(5)

        assertEquals(masterVersion.eventQueue.size, 15)

        assert(redVersion.eventQueue.contains(Event(10, MakeDecision(PlayerId.Red))))
        assert(redVersion.eventQueue.contains(Event(10, CityInflux(PlayerId.Red, 10.0, 6))))
        assert(redVersion.eventQueue.contains(Event(10, CityInflux(PlayerId.Red, 10.0, 1, 3))))
        assert(redVersion.eventQueue.contains(Event(14, TransitEnd(PlayerId.Blue, 1, 3, 20))))
        assert(redVersion.eventQueue.contains(Event(11, Battle(blueForce, redForce)))) // RB
        assert(redVersion.eventQueue.contains(Event(20, Wait(PlayerId.Red, 5))))  // R
        assert(redVersion.eventQueue.contains(Event(13, LaunchExpedition(PlayerId.Red, 3, 1, 3, 10)))) // R
        assertEquals(redVersion.eventQueue.size, 7)

        assert(blueVersion.eventQueue.contains(Event(10, MakeDecision(PlayerId.Blue)))) // B
        assert(blueVersion.eventQueue.contains(Event(10, CityInflux(PlayerId.Red, 10.0, 1, 3)))) // RB
        assert(blueVersion.eventQueue.contains(Event(10, CityInflux(PlayerId.Blue, 10.0, 1, 4)))) // RB
        assert(blueVersion.eventQueue.contains(Event(10, CityInflux(PlayerId.Blue, 10.0, 2, 4)))) // B
        assert(blueVersion.eventQueue.contains(Event(10, TransitStart(Transit(10.0, 4, 2, PlayerId.Blue, 11, 20))))) // B
        assert(blueVersion.eventQueue.contains(Event(10, TransitEnd(PlayerId.Blue, 4, 2, 20)))) // B
        assert(blueVersion.eventQueue.contains(Event(10, TransitEnd(PlayerId.Blue, 4, 1, 20)))) // RB
        assert(blueVersion.eventQueue.contains(Event(11, Battle(blueForce, redForce)))) // RB
        assert(blueVersion.eventQueue.contains(Event(20, Wait(PlayerId.Blue, 5)))) // B
        assert(blueVersion.eventQueue.contains(Event(13, LaunchExpedition(PlayerId.Blue, 8, 1, 3, 10)))) // B
        assert(blueVersion.eventQueue.contains(Event(14, TransitEnd(PlayerId.Blue, 1, 3, 20))))
        assertEquals(blueVersion.eventQueue.size, 11)
    }
}