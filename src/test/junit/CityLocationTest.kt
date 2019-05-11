package test.junit

import games.eventqueuegame.routesCross
import math.Vec2d
import org.junit.jupiter.api.*
import kotlin.test.*

class CityLocationTest {

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
}