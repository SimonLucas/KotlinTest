package test.junit

import agents.shiftLeftAndRandomAppend
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SimpleEvoAgentTest {
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

}