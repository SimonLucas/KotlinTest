package agents.MCTS

import agents.DoNothingAgent
import games.eventqueuegame.NoAction
import games.eventqueuegame.SimpleActionDoNothing
import ggi.SimpleActionPlayerInterface
import ggi.game.*
import test.junit.game
import java.lang.AssertionError

class MCTSTranspositionTableAgentMaster(val params: MCTSParameters,
                                        val stateFunction: StateSummarizer,
                                        val opponentModel: SimpleActionPlayerInterface = SimpleActionDoNothing
) : SimpleActionPlayerInterface {

    private val tree: MutableMap<String, TTNode> = mutableMapOf()

    override fun getAgentType(): String {
        return "MCTSTranspositionTableAgentMaster"
    }

    override fun <T : ActionAbstractGameState> getAction(gameState: T, playerId: Int): Action<T> {

        val startTime = System.currentTimeMillis()
        var iteration = 0

        resetTree(gameState, playerId)
        do {
            val clonedState = gameState.copy(playerId) as T
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

        } while (iteration < params.maxPlayouts && System.currentTimeMillis() < startTime + params.timeLimit)

        return getBestAction(gameState) as Action<T>? ?: NoAction()
    }

    fun getBestAction(state: ActionAbstractGameState): Action<*>? {
        val key = stateFunction(state)
        val actionMap: Map<Action<*>, MCStatistics> = tree[key]?.actionMap ?: mapOf()
        val chosenAction = actionMap.maxBy {
            when (params.selectionMethod) {
                MCTSSelectionMethod.SIMPLE -> it.value.mean
                MCTSSelectionMethod.ROBUST -> it.value.visitCount.toDouble()
            }
        }
                ?.key
        return chosenAction
    }

    fun resetTree(root: ActionAbstractGameState, playerId: Int) {
        // may be overridden to prune tree
        tree.clear()
        val key = stateFunction(root)
        tree[key] = TTNode(params, root.possibleActions(playerId))
    }

    override fun <T : ActionAbstractGameState> getPlan(gameState: T, playerId: Int): List<Action<T>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun reset(): SimpleActionPlayerInterface {
        return MCTSTranspositionTableAgentMaster(params, stateFunction, opponentModel)
    }

    override fun getForwardModelInterface(): SimpleActionPlayerInterface {
        return MCTSTranspositionTableAgentChild()
    }

    override fun backPropagate(finalScore: Double) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}


class MCTSTranspositionTableAgentChild : SimpleActionPlayerInterface {
    override fun getAgentType(): String {
        return "MCTSTranspositionTableAgentChild"
    }

    override fun <T : ActionAbstractGameState> getAction(gameState: T, playerId: Int): Action<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : ActionAbstractGameState> getPlan(gameState: T, playerId: Int): List<Action<T>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun reset(): SimpleActionPlayerInterface {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getForwardModelInterface(): SimpleActionPlayerInterface {
        return this
    }

    override fun backPropagate(finalScore: Double) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}