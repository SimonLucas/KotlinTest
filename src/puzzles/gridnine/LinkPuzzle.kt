package puzzles.gridnine

/**
 *  Just playing with Kotlin - in this case solving a number constraint puzzle from the New Scientist
 */

import java.util.*
import kotlin.collections.HashMap
import kotlin.math.abs

fun main() {
    // println(fac(6))

    val board = LinkBoard()
    board.setup()
    board.place(4,3)
    val nSolutions = permutationSearch(board)
    println("nSolutions = $nSolutions")
}

var rejections = 0

fun permutationSearch(board: LinkBoard): Int {
    // println(board)
    if (board.tokens.isEmpty() || board.positions.isEmpty()) {
        println("Solution: \n${board.printBoard()}")
        return 1
    } else {
        var tot = 0
        for (token in board.tokens) {
            // for each possible token to place, see if we can place it
            // only at the first available position
            // since other alternatives will be tried later
            // for each one, take a copy of the board then try to place it
            val position = board.positions[0]
            if (board.canPlace(token, position)) {
                val cp = board.deepCopy()
                cp.place(token, position)
                tot += permutationSearch(cp)
            } else {
                // println("Rejecting line of search: $alloc")
                rejections++
            }
        }
        return tot
    }
}

val allowedDiff = 2

typealias Graph = Map<Int, List<Int>>

// sample from New Scientist puzzle
val connections: Graph = hashMapOf<Int, List<Int>>(
        1 to listOf(2, 4, 5),
        2 to listOf(1, 3),
        3 to listOf(2, 6),
        4 to listOf(1, 5, 7, 8, 9),
        5 to listOf(1, 4, 9),
        6 to listOf(3, 8, 9),
        7 to listOf(4, 8),
        8 to listOf(4, 6, 7, 9),
        9 to listOf(4, 5, 6, 8)
)

fun checkSymmetry(c: HashMap<Int, List<Int>>): Boolean {
    for (i in c.keys) {
        val next = c[i]
        if (next != null)
            for (j in next) {
                // perform the check
                val reflex = c[j]
                if (reflex != null && !reflex.contains(i)) {
                    println("testing $i and $j")
                    println("\t$i is not in $reflex")
                    return false
                }
            }
    }

    return true
}

fun allowed(x: Int?, y: Int?) = x == null || y == null || abs(x - y) > 2


fun valid(alloc: HashMap<Int, Int>, c: Graph): Boolean {

    // this is not the most efficient way to check allocations - much more efficient
    // to perform incremental checks each time a new token is allocated

    // for each allocation, we check the neighbours

    for ((position, token) in alloc) {
        val neighbours = c[position]
        if (neighbours != null) {
            for (ix in neighbours)
                if (!allowed(token, alloc[ix])) return false
        }
    }
    return true
}

data class LinkBoard(val c: Graph = connections,
                     val tokens: MutableList<Int> = ArrayList(),
                     val positions: MutableList<Int> = ArrayList(),
                     val allocated: MutableMap<Int, Int> = HashMap()) {

    // tied to a nine-cell board
    fun setup() {
        (1..9).forEach { t -> tokens.add(t); positions.add(t) }
    }

    fun deepCopy() =
            LinkBoard(c.toMap(), tokens.toMutableList(), positions.toMutableList(), allocated.toMutableMap())

    fun canPlace(token: Int, position: Int): Boolean {
        // check the validity of placing here

        val linked = c[position]

        if (linked != null) {
            for (p in linked)
                if (!allowed(token, allocated[p])) return false
        }
        return true
    }

    fun place(token: Int, position: Int) {
        tokens.remove(token)
        positions.remove(position)
        allocated[position] = token
    }

    fun printBoard() : String {
        var s = ""
        for (i in 1..9) {
            s = s + allocated[i] + " "
            if (i % 3 == 0) s += "\n"
        }
        return s
    }
}

