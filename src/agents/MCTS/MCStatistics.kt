package agents.MCTS

import games.eventqueuegame.NoAction
import ggi.game.Action
import ggi.game.ActionAbstractGameState

data class MCTSParameters(
        val C: Double = 1.0,
        val selectionMethod: MCTSSelectionMethod = MCTSSelectionMethod.SIMPLE,
        val maxPlayouts: Int = 100,
        val timeLimit: Int = 1000,
        val horizon: Int = 2000,     // limit of actions taken across both tree and rollout policies
        val discountRate: Double = 1.0
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
) {
    fun UCTScore(): Double = mean + params.C * Math.sqrt(Math.log(validVisitCount.toDouble()))
}

class TTNode(
        val params: MCTSParameters = MCTSParameters(),
        val actions: List<Action>
) {
    val actionMap: Map<Action, MCStatistics> = actions.map { it to MCStatistics() }.toMap()

    fun hasUnexploredActions(): Boolean {
        return actionMap.values.any { it.visitCount == 0 }
    }

    fun getRandomUnexploredAction(validOptions: List<Action>): Action {
        if ((validOptions - actions).size > 0) TODO("Need to cater for previously unknown options")
        return validOptions.filter { actionMap[it]!!.visitCount == 0 }.random()
    }

    fun getUCTAction(validOptions: List<Action>): Action {
        val withScores = validOptions.map { a -> a to (actionMap[a]?.UCTScore() ?: 0.0) }
        return withScores.maxBy { (_, score) -> score }!!.first
    }

    fun update(action: Action, possibleActions: List<Action>, reward: Double) {
        actionMap.filter { (k, v) -> possibleActions.contains(k) }
                .mapNotNull { (k, v) -> v }
                .forEach { it.validVisitCount++ }
        val stats = actionMap[action]
        if (stats != null) {
            stats.visitCount++
            stats.mean += reward
            stats.variance += reward * reward
            if (stats.max < reward) stats.max = reward
            if (stats.min > reward) stats.min = reward
        }
    }

}

class OLNode(
        val params: MCTSParameters = MCTSParameters(),
        val key: Long,
        val actionMap: Map<Action, MCStatistics> = mutableMapOf(),
        val treeMap: Map<Action, OLNode> = mutableMapOf()
)