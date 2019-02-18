package games.maxgame

import ggi.AbstractGameState
import ggi.ExtendedAbstractGameState

/**
 *  A simple game equivalent of OneMax
 */

var totalTicks: Long = 0


data class MaxGame(val n: Int=10, val m: Int=2) : ExtendedAbstractGameState {
    var score: Int = 0
    var position = 0
    var nTicks =0

    override fun copy(): AbstractGameState {
        val maxGame = MaxGame(n=this.n, m = this.m)
        maxGame.score = score
        maxGame.position = position
        maxGame.nTicks = nTicks
        return maxGame
    }

    override fun next(actions: IntArray): AbstractGameState {
        if (isTerminal()) return this
        // otherwise advance
        score += actions[0]
        position++
        totalTicks++
        nTicks++
        return this
    }

    override fun nActions(): Int {
        return m
    }

    override fun score(): Double {
        return score.toDouble()
    }

    override fun isTerminal(): Boolean {
        return position >= n
    }

    override fun nTicks(): Int {
        return nTicks
    }

    override fun totalTicks(): Long {
        return totalTicks
    }

    override fun resetTotalTicks() {
        totalTicks = 0
    }

    override fun randomInitialState(): AbstractGameState {
        return this
    }
}


