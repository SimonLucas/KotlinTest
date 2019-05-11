package test.junit


import agents.*
import games.eventqueuegame.*
import ggi.game.Action
import math.Vec2d
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertEquals

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
}