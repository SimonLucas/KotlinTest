package agents.MCTS

import ggi.game.Action
import ggi.game.ActionAbstractGameState

data class MCTSParameters(
        val C: Double = 1.0,
        val selectionMethod: MCTSSelectionMethod = MCTSSelectionMethod.SIMPLE,
        val maxPlayouts: Int = 100,
        val timeLimit: Int = 1000,
        val horizon: Int = 2000     // limit of actions taken across both tree and rollout policies
)

enum class MCTSSelectionMethod { ROBUST, SIMPLE }

class MCStatistics(
        val params: MCTSParameters = MCTSParameters(),
        var visitCount: Int = 0,
        var validVisitCount: Int = 0,
        var mean: Double = 0.0,
        var max: Double = Double.NEGATIVE_INFINITY,
        var min: Double = Double.POSITIVE_INFINITY,
        var variance: Double = 0.0
)

class TTNode(
        val params: MCTSParameters = MCTSParameters(),
        val actions: List<Action<*>>
) {
    val actionMap: Map<Action<*>, MCStatistics> = actions.map { it to MCStatistics() }.toMap()
}

class OLNode<T : ActionAbstractGameState>(
        val params: MCTSParameters = MCTSParameters(),
        val key: Long,
        val actionMap: Map<Action<T>, MCStatistics> = mutableMapOf(),
        val treeMap: Map<Action<T>, OLNode<T>> = mutableMapOf()
)