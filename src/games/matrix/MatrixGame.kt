package games.matrix

import java.util.*
import kotlin.collections.ArrayList

interface MatrixGame {

    fun nActions(): Int
    fun payOff(a1: Int, a2: Int): IntArray
    fun actionNames(): Array<String>
}

data class LeaguePlayer(var points: Int = 0, val strategy: Strategy)

fun main() {
    var game: MatrixGame = RPS()
    // game = PD()

    for (i in 0 until 20)
        println(MixedStrategy(game.nActions()).getAction())


    for (i in 0 until game.nActions()) {
        for (j in 0 until game.nActions()) {
            println("$i versus $j yields: ${Arrays.toString(game.payOff(i, j))} ")
        }
    }

    println(MixedStrategy(3))

    val nPlayers = 0
    val nRounds = 1000

    val players = ArrayList<LeaguePlayer>()
    for (i in 0 until nPlayers)
        players.add(LeaguePlayer(0, MixedStrategy(game.nActions()).randomise()))

    players.add(LeaguePlayer(0, FixedStrategy(0)))
    players.add(LeaguePlayer(0, FixedStrategy(1)))
    // players.add(LeaguePlayer(0, FixedStrategy(2)))
    players.add(LeaguePlayer(0, MixedStrategy(game.nActions())))

    // now play some games
    var nGames = 0
    for (r in 0 until nRounds) {
        for (i in 0 until players.size) {
            for (j in 0 until players.size) {
                // play a game and update the results for each player
                val p1 = players[i]
                val p2 = players[j]
                val payOff = game.payOff(p1.strategy.getAction(), p2.strategy.getAction())
                // record the results
                p1.points += payOff[0]
                p2.points += payOff[1]
                // keep a sanity check on the number of games played
                nGames++
            }
        }
    }
    // now show the league
    println("Played $nGames games")

    // how show the league
    players.sortBy { -it.points }

    for (p in players) println(p)
}

// Rock-Paper-Scissors
val actionsRPS = arrayOf("Rock", "Paper", "Scissors")

class RPS : MatrixGame {
    override fun actionNames() = actionsRPS

    override fun nActions() = 3

    override fun payOff(a1: Int, a2: Int): IntArray {
        var diff = a1 - a2
        if (Math.abs(diff) == 2) diff = -diff / 2
        return intArrayOf(diff, -diff)
    }
}

val actionsPD = arrayOf("Rock", "Paper", "Scissors")

class PD : MatrixGame {
    override fun actionNames() = actionsPD

    override fun nActions() = mat.size

    companion object {
        val mat = arrayOf(
                arrayOf(
                        intArrayOf(2, 2),
                        intArrayOf(0, 3)
                ),
                arrayOf(
                        intArrayOf(3, 0),
                        intArrayOf(1, 1)
                )
        )
    }

    override fun payOff(a1: Int, a2: Int): IntArray {
        return mat[a1][a2]
    }
}

val rand = Random()

interface Strategy {
    fun getAction(): Int
}

class FixedStrategy(val a: Int) : Strategy {
    override fun getAction() = a
    override fun toString(): String {
        return "Fixed action: " + a
    }
}

class MixedStrategy(val nActions: Int) : Strategy {
    val pa = DoubleArray(nActions)
    val eps: Double = 1e-10

    init {
        // for ()
        normalise()
        println("Normalised")
    }

    private fun normalise(): MixedStrategy {
        var sum = pa.sum()
        if (sum == 0.0) sum = pa.size * eps
        for (i in 0 until pa.size) {
            pa[i] = (eps + pa[i]) / sum
        }
        return this
    }

    public fun randomise(): MixedStrategy {
        for (i in 0 until pa.size) pa[i] = rand.nextDouble()
        normalise()
        return this
    }

    override fun getAction(): Int {
        var cum = 0.0
        val x = rand.nextDouble()
        for (i in 0 until pa.size) {
            cum += pa[i]
            if (x < cum) return i
        }
        println("Should never get here!")
        return pa.size - 1
    }

    override fun toString(): String {
        return Arrays.toString(pa)
    }
}

