package test.junit

import games.eventqueuegame.*
import math.Vec2d
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.random.Random

class ObjectiveFunctionTest {
    @Test
    fun simpleObjectiveTest() {
        val startState = game.copy()
        startState.scoreFunction = simpleScoreFunction(5.0, 1.0)
        assertEquals(startState.score(0), 0.0)
        assertEquals(startState.score(1), -0.0)

        LaunchExpedition(PlayerId.Red, 1, 1, 1, 0).apply(startState)
        startState.next(5)
        assertEquals(startState.score(0), -5.0)
        assertEquals(startState.score(1), 5.0)
    }
}