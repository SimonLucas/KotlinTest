package agents.MCTS

import games.eventqueuegame.NoAction
import games.eventqueuegame.SimpleActionDoNothing
import ggi.SimpleActionPlayerInterface
import ggi.game.*
import java.util.*

class MCTSTranspositionTableAgentMaster(val params: MCTSParameters,
                                        val stateFunction: StateSummarizer,
                                        val opponentModel: SimpleActionPlayerInterface = SimpleActionDoNothing
) : SimpleActionPlayerInterface {

    val tree: MutableMap<String, TTNode> = mutableMapOf()

    override fun getAgentType(): String {
        return "MCTSTranspositionTableAgentMaster"
    }

    override fun getAction(gameState: ActionAbstractGameState, playerId: Int): Action {

        val startTime = System.currentTimeMillis()
        var iteration = 0

        resetTree(gameState, playerId)
        do {
            val clonedState = gameState.copy() as ActionAbstractGameState
            // TODO: At some point, we may then resample state here for IS-MCTS
            clonedState.registerAgent(playerId, getForwardModelInterface())
            (0 until clonedState.playerCount()).forEach {
                if (it != playerId)
                    clonedState.registerAgent(it, opponentModel)
                // TODO: When we have more interesting opponent models (e.g. MCTS agents), we need to instantiate/initialise them
            }

            clonedState.next(params.horizon)

            (0 until clonedState.playerCount()).forEach {
                val reward = clonedState.score(it)
                clonedState.getAgent(it).backPropagate(reward)
            }
            iteration++
        } while (iteration < params.maxPlayouts && System.currentTimeMillis() < startTime + params.timeLimit)

    //    println("$iteration iterations executed for player $playerId")
        return getBestAction(gameState)
    }

    fun getBestAction(state: ActionAbstractGameState): Action {
        val key = stateFunction(state)
        val actionMap: Map<Action, MCStatistics> = tree[key]?.actionMap ?: mapOf()
        val chosenAction = actionMap.maxBy {
            when (params.selectionMethod) {
                MCTSSelectionMethod.SIMPLE -> it.value.mean
                MCTSSelectionMethod.ROBUST -> it.value.visitCount.toDouble()
            }
        }?.key
        return chosenAction ?: NoAction
    }

    fun resetTree(root: ActionAbstractGameState, playerId: Int) {
        // may be overridden to prune tree
        tree.clear()
        val key = stateFunction(root)
        tree[key] = TTNode(params, root.possibleActions(playerId))
    }

    override fun getPlan(gameState: ActionAbstractGameState, playerId: Int): List<Action> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun reset(): SimpleActionPlayerInterface {
        return MCTSTranspositionTableAgentMaster(params, stateFunction, opponentModel)
    }

    override fun getForwardModelInterface(): SimpleActionPlayerInterface {
        return MCTSTranspositionTableAgentChild(tree, params, stateFunction)
    }

    override fun backPropagate(finalScore: Double) {
        // should never need to back-propagate here...that is done in the Child agent
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}


class MCTSTranspositionTableAgentChild(val tree: MutableMap<String, TTNode>,
                                       val params: MCTSParameters,
                                       val stateFunction: StateSummarizer) : SimpleActionPlayerInterface {

    // node, possibleActtions from node, action taken

    private val trajectory: Deque<Triple<String, List<Action>, Action>> = ArrayDeque()

    val nodesPerIteration = 1
    var nodesToExpand = nodesPerIteration
        private set(n) {
            field = n
        }

    override fun getAgentType(): String {
        return "MCTSTranspositionTableAgentChild"
    }

    override fun getAction(gameState: ActionAbstractGameState, playerId: Int): Action {
        val currentState = stateFunction(gameState)
        val possibleActions = gameState.possibleActions(playerId)
        val node = tree[currentState]
        val actionChosen = when {
            node == null -> rolloutPolicy(gameState, possibleActions)
            node.hasUnexploredActions() -> expansionPolicy(node, currentState, possibleActions)
            else -> treePolicy(node, gameState, possibleActions)
        }
        trajectory.addLast(Triple(currentState, possibleActions, actionChosen))
        return actionChosen
    }

    fun expansionPolicy(node: TTNode, state: String, possibleActions: List<Action>): Action {
        return node.getRandomUnexploredAction(possibleActions)
    }

    fun treePolicy(node: TTNode, state: ActionAbstractGameState, possibleActions: List<Action>): Action {
        return node.getUCTAction(possibleActions)
    }

    fun rolloutPolicy(state: ActionAbstractGameState, possibleActions: List<Action>): Action {
        return possibleActions.random()
    }


    override fun getPlan(gameState: ActionAbstractGameState, playerId: Int): List<Action> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun reset(): SimpleActionPlayerInterface {
        return MCTSTranspositionTableAgentChild(tree, params, stateFunction)
    }

    override fun getForwardModelInterface(): SimpleActionPlayerInterface {
        return this
    }

    override fun backPropagate(finalScore: Double) {
        // Here we go forwards through the trajectory
        // we decrement nodesExpanded as we need to expand a node
        // We can discount if needed
        var totalDiscount = Math.pow(params.discountRate, trajectory.size.toDouble())
        trajectory.forEach { (state, possibleActions, action) ->
            totalDiscount /= params.discountRate
            val node = tree[state]
            when {
                node == null && nodesToExpand > 0 -> {
                    nodesToExpand--
                    tree[state] = TTNode(params, possibleActions)
                    // Add new node (with no visits as yet; that will be sorted during back-propagation)
                }
                node == null -> Unit // do nothing
                else -> node.update(action, possibleActions, finalScore * totalDiscount)
            }
        }
        nodesToExpand = nodesPerIteration
    }
}