package test.junit

import agents.*
import games.eventqueuegame.*
import math.Vec2d
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.random.Random

class SimpleEvoAgentTest {

    // we create a simple world of 3 cities. One Blue and one Red, with a Neutral world sandwiched between them
    val cities = listOf(
            City(Vec2d(0.0, 0.0), 0, 10, PlayerId.Blue),
            City(Vec2d(0.0, 20.0), 0, 10, PlayerId.Red),
            City(Vec2d(0.0, 10.0), 0, 0, PlayerId.Neutral)
    )
    val routes = listOf(
            Route(0, 1, 20, 1.0),
            Route(0, 2, 10, 1.0),
            Route(1, 0, 20, 1.0),
            Route(1, 2, 10, 1.0),
            Route(2, 0, 10, 1.0),
            Route(2, 1, 10, 1.0)
    )
    val world = World(cities, routes, 20, 20, 5.0, Random(10))
    val game = EventQueueGame(world)

    @Test
    fun shiftLeftOperatorWithOneAction() {
        val startArray = intArrayOf(0, 3, 7, 23, 56, 2, -89)
        val endArray = shiftLeftAndRandomAppend(startArray, 1, 5)
        for ((i, n) in startArray.withIndex()) {
            if (i >= 1) assertEquals(n, endArray[i - 1])
        }
        assertEquals(endArray.size, 7)
        assert(endArray.last() < 5 && endArray.last() >= 0)
    }

    @Test
    fun shiftLeftOperatorWithFourActions() {
        val startArray = intArrayOf(0, 3, 7, 23, 56, 2, -89)
        val endArray = shiftLeftAndRandomAppend(startArray, 4, 1)
        for ((i, n) in startArray.withIndex()) {
            if (i >= 4) assertEquals(n, endArray[i - 4])
        }
        assertEquals(endArray.size, 7)
        for (i in 1..4)
            assert(endArray[7 - i] == 0 || endArray[7 - i] == 1)

    }

    @Test
    fun blueExpeditionRollsForwardToTakeNeutralWorld() {
        // launches half of force to the Neutral city
        val blueGenome1 = intArrayOf(0, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0)
        val projectedState1 = game.copy()
        var reward = evaluateSequenceDelta(projectedState1, blueGenome1, 0, 1.0, 5)
        assertEquals(reward, +1.0)
        assert(projectedState1.world.cities[2].owner == PlayerId.Blue)
        assert(game.world.cities[2].owner == PlayerId.Neutral)

        val blueGenome2 = intArrayOf(0, 1, 1, 1)
        val projectedState2 = game.copy()
        reward = evaluateSequenceDelta(projectedState2, blueGenome2, 0, 1.0, 1)
        assertEquals(reward, 0.0)       // not yet reached
        assert(projectedState2.world.cities[2].owner == PlayerId.Neutral)
        assert(game.world.cities[2].owner == PlayerId.Neutral)
        assertEquals(projectedState2.world.currentTransits.size, 1)
    }

    @Test
    fun redExpeditionLaunchedWhileBlueInProgressUpdatesCorrectly() {
        val blueGenome2 = intArrayOf(0, 1, 1, 1, 1, 1, 1, 0)
        val projectedState2 = game.copy()
        var reward = evaluateSequenceDelta(projectedState2, blueGenome2, 0, 1.0, 2)
        assert(projectedState2.world.cities[2].owner == PlayerId.Neutral)
        assertEquals(projectedState2.world.currentTicks, 2)
        assert(game.world.cities[2].owner == PlayerId.Neutral)
        assertEquals(projectedState2.world.currentTransits.size, 1)
        assertEquals(reward, 0.0)
        // blue force now in transit

        val redGenome1 = intArrayOf(1, 0, 2, 1, 1, 1, 1, 0)
        val projectedState3 = projectedState2.copy()
        reward = evaluateSequenceDelta(projectedState3, redGenome1, 1, 1.0, 2)
        assertEquals(projectedState2.world.currentTicks, 2)
        assertEquals(projectedState3.world.currentTicks, 4)
        assertEquals(reward, -1.0) // blue force reaches neutral city

        val redGenome2 = intArrayOf(1, 0, 2, 1, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0)
        val projectedState4 = projectedState2.copy()
        reward = evaluateSequenceDelta(projectedState4, redGenome2, 1, 1.0, 6)
        assertEquals(reward, 1.0) // blue force reaches neutral city, and red force conquers blue base
    }

}