package agents

import games.maxgame.MaxGame
import ggi.AbstractGameState
import ggi.SimplePlayerInterface
import utilities.Picker
import utilities.StatSummary
import java.util.*


import kotlin.math.log2
import kotlin.math.sqrt

val random = Random()

/**
 *  This is a work in progress: not ready for use yet
 */

// ToDo need to finish and test this


fun main(args: Array<String>) {

    var game = MaxGame()
    // val agent = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 20, nEvals = 5, useShiftBuffer = false)
    val agent = MctsAgent()
    val playerId = 0
    // get it to play the game
    var nSteps = 1
    while (!game.isTerminal() && nSteps-- >0) {
        // take an action
        var action = agent.getAction(game.copy(), playerId)
        println(agent.root.size())
        // action = 100
        // println(Arrays.toString(agent.buffer))
        println("Action: ${action}")
        game.next(intArrayOf(action))
    }
    println(game)
    println(game.copy())
    println("Final score = " + game.score)
    println("Total ticks = ${game.totalTicks()}")

}

data class Expansion(val node: TreeNode, val action: Int, val state: AbstractGameState)

data class MctsAgent (
        var rolloutLength: Int = 200,
        var nPlayouts: Int = 20,
        var k: Double = 10.0,
        var opponentModel: SimplePlayerInterface = DoNothingAgent()
): SimplePlayerInterface {

    var root = TreeNode()

    override fun getAction(gameState: AbstractGameState, playerId: Int): Int {
        // for now reset each time an action is required
        root = TreeNode()
        for (i in 0 until nPlayouts) {
            // val toExpand = treePolicy(root)
            var state = gameState.copy()
            // now navigate down the tree to pick a node to expand
            val toExpand = treePolicy(root, state, playerId)
            // toExpand.expand()
            // todo several things to finish here including making the rollout and backing up the reward
            expand(toExpand, playerId)
        }
        return recommendation(root, gameState)
    }

    fun recommendation(node: TreeNode, state: AbstractGameState) : Int {
        // protect against calling this after zero rollouts
        val best = node.bestAction()
        if (best != null) return best
        else return agents.random.nextInt(state.nActions())
    }

    fun expand(ex: Expansion, playerId: Int) {
        // take the desired action
        val child = TreeNode()
        child.parent = ex.node
        ex.node.actions[ex.action] = child
        // val reward
        val state = act(ex.state, ex.action, playerId)
        // roll out from this state
        val reward = rollout(state, playerId, rolloutLength)
        child.backup(reward)
        println("Added a node at depth: " + child.depth())
    }

    // return the node to expand with the selected action
    fun treePolicy(node: TreeNode, state: AbstractGameState, playerId: Int) : Expansion {
        // while we keep getting tree nodes, go down the tree
        val action = node.bestUCT(state)
        println("Tree policy at depth ${node.depth()}, UCT action =  ${action}")
        val child = node.actions[action]
        if (child!=null)
            return treePolicy(child, act(state, action, playerId), playerId)
        else
            return Expansion(node, action, state)
    }

    fun act(state: AbstractGameState, ourAction: Int, playerId: Int) : AbstractGameState {
        // could also force random playouts
        // this takes our players action together with the opponent's action
        val opponentAction = opponentModel.getAction(state, playerId-1)
        val actions = IntArray(2)
        actions[playerId] = ourAction
        actions[1-playerId] = opponentAction
        state.next(actions)
        return state
    }

    fun rollout(state: AbstractGameState, playerId: Int, maxSteps: Int) : Double {
        val startScore = state.score()
        while(maxSteps > 0 && !state.isTerminal()) {
            val ourAction = random.nextInt(state.nActions())
            act(state, ourAction, playerId)
        }
        val delta = state.score() - startScore
        // assume player zero is the maximising player
        return if (playerId == 0)
            delta
        else
            -delta
    }

    override fun reset(): SimplePlayerInterface {
        root = TreeNode()
        return this
    }

    internal var random = Random()

    // we will expand a node by adding the entry to the HashMap


}

data class TreeNode (val k: Double = 1.0){

    val epsilon = 1e-6

    var n : Int = 0
    var sum: Double = 0.0
    val actions = HashMap<Int,TreeNode>()
    var parent: TreeNode? = null

    // constructor()

    fun bestAction() : Int? {
        val picker = Picker<Int>()
        actions.forEach{(action,stats) -> picker.add(stats.mean(), action)}
        // for (action in actions.keys)
        return picker.best
    }

    // should this return an int for the action or a node?
    // note that this could be either an already tried action
    // or one we're about to try for the first time
    fun bestUCT(state: AbstractGameState) : Int {
        val picker = Picker<Int>(Picker.MAX_FIRST)
        for (i in 0 until state.nActions()) {
            var node = actions.get(i)
            var value = epsilon * random.nextDouble()
            if (node != null) value = node.uct(n)
            picker.add(value, i)
        }
        val best = picker.best
        println("n = ${n}, best UCT = ${picker.bestScore}")
        if (best != null) return best
        else return random.nextInt(state.nActions())
    }

    fun backup(reward: Double) {
        sum += reward
        n++
        parent?.backup(reward)
    }

    fun mean() = sum / n

    fun uct(N: Int) = mean() + k * sqrt(log2(N.toDouble() + epsilon) / (n+epsilon))

    // make sure we can check the tree size
    fun size() : Int {
        // be sure to count this node, and then all its children
        var count = 1
        actions.forEach{action, state -> count += state.size()}
        return count
    }

    fun depth() : Int {
        // if (parnt == null) return 0
        val temp = parent
        return if (temp == null) 0 else 1 + temp.depth()
    }

}

// data class NodeStats
