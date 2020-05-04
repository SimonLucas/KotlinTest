package agents

import games.gridworld.GridPosition
import games.gridworld.GridWorld
import ggi.AbstractGameState
import ggi.ExtendedAbstractGameState
import kotlin.random.Random

class SubgoalPredicate {
    fun predicate(state: AbstractGameState) : Boolean {
        if (state is GridWorld) {
            // just make something up for now
            return state.isSubgoal() || state.terminal
        }
        return false
    }
}

class GridWorldSubgameAdapter(var microGame: AbstractGameState) : ExtendedAbstractGameState {
    var actionFinder = SubgoalActionFinder()

    override fun copy(): AbstractGameState {
        val cp = GridWorldSubgameAdapter(microGame = microGame.copy())
        cp.actionFinder = actionFinder.deepCopy()
        return this
    }

    override fun next(actions: IntArray): AbstractGameState {
        // for the next step we're going to use the number to seed a random number generator
        val r = Random(actions[0])
        actionFinder.expandState(microGame)
        if (actionFinder.macros.size == 0) {
            // did not find any macro actions, so just take a micro one selected by the RNG
            microGame.next(intArrayOf(r.nextInt(microGame.nActions())))
        } else {
            // pick one of the macro actions at random and execute all of it
            with(actionFinder) {
                // uses the rand seed to hash to the selected macro action
                val macro = macros.get(subgoals[r.nextInt(subgoals.size)])
                if (macro!= null) {
                    for (a in macro.seq) {
                        // then follow the micro actions
                        microGame.next(intArrayOf(a))
                    }
                } else {
                    // take a macro action, but should never get here
                    println("Warning, unxpected code branch")
                    microGame.next(intArrayOf(r.nextInt(microGame.nActions())))
                }
            }
        }
        return this
    }

    override fun nActions(): Int {
        // this is an arbitrary number - the actions will be used to seed a random number generator
        return 100
    }

    override fun score(): Double {
        // eacy, just the micro-game score
        return microGame.score()
    }

    override fun isTerminal(): Boolean {
        return microGame.isTerminal()
    }

    override fun nTicks(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun totalTicks(): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun resetTotalTicks() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun randomInitialState(): AbstractGameState {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

data class ScoredRollout(val seq: ArrayList<Int>, val score: Double)

data class SubgoalActionFinder(var horizon:Int=100, var p0: Double = 0.95, var alpha: Double = 0.001) {

    // ExpandNode is an implementation of Algorithm 1 in the paper mentioned above
    // note: We're currently using GridPosition as a proxy for the
    // actual game state
    // this ties the code to the current GridWorld example

    fun deepCopy() : SubgoalActionFinder {
        val cp = this.copy()
        cp.macros = macros.clone() as HashMap<GridPosition, ScoredRollout>
        cp.subgoals = subgoals.clone() as ArrayList<GridPosition>
        cp.random = random
        cp.subgoalPredicate = subgoalPredicate
        return this
    }

    var macros = HashMap<GridPosition, ScoredRollout>()
    var subgoals = ArrayList<GridPosition>()
    var random = Random
    var subgoalPredicate = SubgoalPredicate()

    fun expandState(s: AbstractGameState) {
        val state = s as GridWorld
        val nMax = Math.log(alpha) / Math.log(p0)
        var nRevisits = 0;
        var n = 0
        var horizonHits = 0
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
            } while (seq.size < horizon && !subgoalPredicate.predicate(sNext) )

            println("${seq.size}, \t ${(sNext.score()*100).toInt()}, \t ${subgoalPredicate.predicate(sNext)}, \t ${(sNext as GridWorld).gridPosition}, ${sNext.gridPosition.hashCode()}")
            // now, why did we exit the loop?
            // are we at a subgoal, and if so, is it a new one?
            if (macros.containsKey(sNext.gridPosition)) {
                nRevisits++

                val scoredRollout = macros.get(sNext.gridPosition)
                // if we've found a better way to get there then update the reward
                if (scoredRollout != null && sNext.score() > scoredRollout.score) {
                    macros.put(sNext.gridPosition, ScoredRollout(seq, sNext.score()))
                }
            } else {
                // found a macro action that leads to a new subgoal
                if (subgoalPredicate.predicate(sNext)) {
                    macros.put(sNext.gridPosition, ScoredRollout(seq, sNext.score()))
                    subgoals.add(sNext.gridPosition)
                } else
                    horizonHits++

            }
        } while (nRevisits < nMax)
        println()
        println("Inner loop iterations: $n")
        println("nRevisits:  \t $nRevisits")
        println("HorizonHits:\t $horizonHits")
        println("Found ${macros.size} macro actions")
        println("Found ${subgoals.size} subgoals")
        println()
        for (key in macros.keys) {
            val testState = state.copy() as GridWorld
            testState.gridPosition = key
            println("$key\t ${subgoalPredicate.predicate(testState)}")
        }
    }
}
