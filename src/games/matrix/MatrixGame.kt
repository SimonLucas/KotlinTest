package games.matrix

import evodef.EvolutionLogger
import evodef.FitnessSpace
import evodef.SearchSpace
import java.util.*
import java.util.logging.Logger
import kotlin.collections.ArrayList

interface MatrixGame {

    fun nActions(): Int
    fun payOff(a1: Int, a2: Int): IntArray
    fun actionNames(): Array<String>

}

data class LeaguePlayer(var points: Int = 0, val strategy: Strategy)

fun main() {
    var game: MatrixGame = RPS()
    game = PD()

    for (i in 0 until 20)
        println(MixedStrategy(game.nActions()).getAction())


    for (i in 0 until game.nActions()) {
        for (j in 0 until game.nActions()) {
            println("$i versus $j yields: ${Arrays.toString(game.payOff(i, j))} ")
        }
    }

    println(MixedStrategy(3))

    val nPlayers = 10
    val nRounds = 100

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

val actionsPD = arrayOf("Cooperate", "Defect")

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

object weights {
    val p = doubleArrayOf(0.0, 0.25, 0.5, 0.75, 1.0)
}

class RPSSearchSpace : SearchSpace {
    override fun nDims(): Int {
        return 3
    }

    override fun nValues(i: Int): Int {
        return weights.p.size
    }
}



class RPSEvoMixedStrategy : FitnessSpace {

    override fun evaluate(x: IntArray): Double {
        // note that this must be made to work just for point p, even if it really makes no sense for the RPS example
        // for now, pick a random strategy to play against for one game

        // convert the int array to probability weights (they need normalising to convert to probabilities
        val p = weights.p
        val wp = doubleArrayOf( p[x[0]], p[x[1]], p[x[2]] )

        // create a mixed strategy with these weights
        // setProbs does the normalisation
        val mixedStrategy = MixedStrategy(wp.size).setProbs(wp)

        val game = RPS()

        // calling normalise with the default all zero weights will create a pure random player
        var opponent: Strategy = MixedStrategy(wp.size).normalise()
        opponent = FixedStrategy(0)
        val outcome = game.payOff(mixedStrategy.getAction(), opponent.getAction())

        // just return this players payoff
        val fitness = outcome[0].toDouble()
        logger.log(fitness, x, false)
        return fitness
    }

    var logger: EvolutionLogger

    init {
        logger = EvolutionLogger()
    }

    override fun nDims(): Int {
        return 3
    }

    override fun optimalFound(): Boolean {
        return false
    }

    override fun optimalIfKnown(): Double {
        return 0.0
    }

    override fun logger(): EvolutionLogger {
        return logger
    }

    override fun nValues(p0: Int): Int {
        return weights.p.size
    }

    override fun searchSpace(): SearchSpace {
        return this
    }

    override fun reset() {
        logger = EvolutionLogger()
    }

    override fun nEvals(): Int {
        return logger.nEvals()
    }
}

class MixedStrategy(val nActions: Int) : Strategy {
    val pa = DoubleArray(nActions)
    val eps: Double = 1e-10

    init {
        // for ()
        normalise()
        // println("Normalised")
    }

    public fun normalise(): MixedStrategy {
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

    public fun setProbs(x: DoubleArray) : MixedStrategy {
        for (i in 0 until pa.size) pa[i] = x[i]
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

