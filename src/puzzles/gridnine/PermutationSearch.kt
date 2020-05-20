package puzzles.gridnine

import java.util.*
import kotlin.collections.HashMap
import kotlin.math.abs

fun main() {
    val available = mutableListOf<Int>(1, 2, 3, 4, 5, 6)
    val allocated = mutableListOf<Int>()

    val total = permutationSearch(allocated, available)

    println("nSolutions = $total")
    println("possibles  = ${fac(available.size)}")

    // println(checkSymmetry(connections))
}

fun fac(n: Int): Int = if (n <= 1) 1 else n * fac(n - 1)

fun permutationSearch(allocated: MutableList<Int>, available: MutableList<Int>): Int {
    if (available.isEmpty()) {
        println("Solution: \t $allocated")
        return 1
    } else {
        var tot = 0
        for (i in available) {
            val alloc = allocated.toMutableList()
            val avail = available.toMutableList()
            alloc.add(i)
            if (valid(alloc)) {
                avail.remove(i)
                tot += permutationSearch(alloc, avail)

            }
        }
        return tot
    }
}

fun valid(alloc: List<Int>): Boolean {
    for (i in 0 until alloc.size - 1) {
        if (abs(alloc.get(i) - alloc.get(1 + i)) > allowedDiff) return false
    }
    return true
}
