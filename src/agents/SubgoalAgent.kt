package agents

import games.gridworld.GridPosition
import games.gridworld.GridWorld
import ggi.AbstractGameState
import kotlin.random.Random


fun main() {
    println(Math.log(0.001) / Math.log(0.95))
}

class SubgoalPredicate {
    fun p(state: AbstractGameState) : Boolean {
        if (state is GridWorld) {
            // just make something up for now
            return state.isSubgoal()
        }
        return false
    }
}

data class ScoredRollout(val seq: ArrayList<Int>, val score: Double)

class SubgoalAgent(var H:Int=200, var p0: Double = 0.95, var alpha: Double = 0.001) {

    // ExpandNode is an implementation of Algorithm 1 in the paper mentioned above

    // note: We're currently using GridPosition as a proxy for the
    // actual game state
    // this ties the code to the current GridWorld example

    val macros = HashMap<GridPosition, ScoredRollout>()
    val random = Random
    val g = SubgoalPredicate()

    fun expandState(s: AbstractGameState) {
        val state = s as GridWorld

        val nMax = Math.log(alpha) / Math.log(p0)

        var nRevisits = 0;
        var n = 0
        do {
            val sNext = state.copy()
            // create a new empty macro action
            val seq = ArrayList<Int>()

            // now add random actions until we meet a subgoal
            // or we reach the horizon (the horizon is the maximum length of the macro action
            do {
                val action = random.nextInt(sNext.nActions())
                sNext.next(intArrayOf(action))
                seq.add(action)
                n++
            } while (seq.size < H && !g.p(sNext) )

            println("${seq.size}, \t ${sNext.score()}, \t ${g.p(sNext)}, \t ${(sNext as GridWorld).gridPosition}, ${sNext.gridPosition.hashCode()}")

            // now, why did we exit the loop?
            // are we at a subgoal, and if so, is it a new one?
            if (macros.containsKey(sNext.gridPosition)) {
                nRevisits++

                val scoredRollout = macros.get(sNext.gridPosition)
                // if we've found a better way to get there thene update the reward
                if (scoredRollout != null && sNext.score() > scoredRollout.score) {
                    macros.put(sNext.gridPosition, ScoredRollout(seq, sNext.score()))
                }
            } else {
                // found a macro action that leads to a new subgoal
                macros.put(sNext.gridPosition, ScoredRollout(seq, sNext.score()))
            }
        } while (nRevisits < nMax)
        println("Inner loop iterations: $n")
        println(macros.size)
    }
}
