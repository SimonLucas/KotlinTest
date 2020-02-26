package eval

import kotlin.random.Random

var pId = 0

fun main() {


    val n = 4

    val league = League(n)
    league.compareWithWinRates()
    println()
    league.playGames()
    league.compareWithWinRates()



}

data class League(val n:Int) {
    val players = ArrayList<PlayerRating>()
    val rand = Random

    init {
        for (i in 0 until n) {
            players.add(PlayerRating())
        }
    }

    fun print() {
        for (p in players) println(p)
    }

    fun playGames(nRounds: Int = 100) {
        for (i in 0 until nRounds) playRound()
    }

    fun playRound() {
        for (i in 0 until n) {
            for (j in 0 until n) {
                if (j != i) {
                    val pWin = sigmoid(winRates[i][j])
                    val result = if (rand.nextDouble() < pWin) 1.0 else 0.0
                    EloM().adjust(players[i], players[j], result)
                }
            }
        }
    }

    fun compareWithWinRates() {
        for (i in 0 until n) {
            println( "${winRates[i].sum()}  \t :  ${players[i]} ")
        }
    }

    val winRates = arrayOf(
            doubleArrayOf(0.0, 0.5, 2.0, 0.5),
            doubleArrayOf(-0.5, 0.0, 0.5, 1.0),
            doubleArrayOf(-2.0, -0.5, 0.0, 1.0),
            doubleArrayOf(-0.5, -1.0, -1.0, 0.0)
    )

}

data class PlayerRating(
        val id: Int = pId++,
        var r: Double = 0.0,
        val c:DoubleArray = doubleArrayOf(0.0, 0.0),
        var n:Int = 0,
        var nWin:Double = 0.0
)



class EloM {

    /*

From Balduzzi et al: https://arxiv.org/pdf/1806.02643.pdf

def mElo2_update(i, j, p_ij, r, c) :
  p_hat_ij = sigmoid(r[i] − r[j] + c[i, 0] ∗ c[j, 1] − c[j, 0] ∗ c[i, 1])
  delta = p_ij − p_hat_ij
  r_update = [ 16 ∗ delta, −16 ∗ delta ]
F
# r has higher learning rate than c c_update = [
[ +delta ∗ c[j, 1], −delta ∗ c[i, 1] ],
[ −delta ∗ c[j, 0], +delta ∗ c[i, 0] ] ]
return r_update, c_update

 */

    val alpha = 0.001

    fun adjust(p1: PlayerRating, p2: PlayerRating, p_ij: Double) {
        val pHat_ij = pHat(p1, p2)
        val delta = p_ij - pHat_ij
        // update base ratings
        p1.r += alpha * 16 * delta
        p2.r -= alpha * 16 * delta

        p1.nWin += p_ij
        p2.nWin += (1-p_ij)

        // update intransitive discounts
        // p1.

        p1.c[0] += delta * p2.c[1]
        p1.c[1] -= delta * p1.c[1]

        p2.c[0] += delta * p2.c[0]
        p2.c[1] -= delta * p1.c[0]

        p1.n++
        p2.n++
    }

    fun pHat(p1: PlayerRating, p2: PlayerRating) : Double {
        return sigmoid(p1.r - p2.r + p1.c[0] * p2.c[1] - p2.c[0] * p1.c[1])
    }

    fun adjust(i:Int, j:Int, p_ij: Double, r:DoubleArray, c: Array<DoubleArray>) {
        val pHat_ij = sigmoid(
                r[i] - r[j] + c[i][0] * c[j][1] - c[j][0] * c[i][1]
        )
        val delta = p_ij - pHat_ij
        val rUpdate = doubleArrayOf(16 * delta, -16 * delta)
        // val
    }



}

fun sigmoid(x: Double) : Double {
    return 1.0 / (1 + Math.exp(-x))
}

