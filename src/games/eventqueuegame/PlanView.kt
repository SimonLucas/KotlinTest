package games.eventqueuegame

import ggi.SimpleActionPlayerInterface
import javax.swing.*

class PlanView(val agent: SimpleActionPlayerInterface, val game: LandCombatGame, val playerId: Int) : JTextArea(10, 50) {

    var lastGenome = intArrayOf()
    var lastDifferentGenome = intArrayOf()

    fun refresh() {
        val actionList = agent.getPlan(game, playerId)
        when (agent) {
            is SimpleActionEvoAgentRollForward -> {
                text = lastDifferentGenome.joinToString("") + "\n" + agent.genome.joinToString("") + "\n"
                if (!lastGenome.contentEquals(agent.genome))
                    lastDifferentGenome = lastGenome.copyOf()
                lastGenome = agent.genome.copyOf()
            }
            is SimpleActionEvoAgent -> {
                text = lastDifferentGenome.joinToString("") + "\n" + agent.underlyingAgent.buffer?.joinToString("") + "\n"
                if (!lastGenome.contentEquals(agent.underlyingAgent.buffer ?: intArrayOf()))
                    lastDifferentGenome = lastGenome.copyOf()
                lastGenome = (agent.underlyingAgent.buffer ?: intArrayOf()).copyOf()
            }
        }
        append(actionList.joinToString("\n"))
    }
}