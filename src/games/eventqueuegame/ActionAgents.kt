package games.eventqueuegame

import agents.SimpleEvoAgent
import ggi.SimpleActionPlayerInterface
import ggi.game.Action
import ggi.game.ActionAbstractGameState

object NoAction : Action {
    override fun apply(state: ActionAbstractGameState): ActionAbstractGameState {
        // Do absolutely nothing
        return state
    }
}

class SimpleActionEvoAgent(val underlyingAgent: SimpleEvoAgent = SimpleEvoAgent()) : SimpleActionPlayerInterface {
    override fun reset() = this

    override fun getAgentType() = "SimpleActionEvoAgent: $underlyingAgent"

    override fun getAction(gameState: ActionAbstractGameState, playerRef: Int): Action {
        val intPerAction = gameState.codonsPerAction()
        // the underlyingAgent does all the work on mutating the genome
        // we're just a wrapper for it
        val gene = underlyingAgent.getActions(gameState, playerRef).sliceArray(0 until intPerAction)
        return gameState.translateGene(playerRef, gene)
    }

    override fun getForwardModelInterface(): SimpleActionPlayerInterface {
        return SimpleActionEvoAgentRollForward((underlyingAgent.buffer ?: intArrayOf()).copyOf())
    }
}

/*
Will take actions using a specified genome...until the sequence runs out
 */
class SimpleActionEvoAgentRollForward(var genome: IntArray) : SimpleActionPlayerInterface {

    override fun getAction(gameState: ActionAbstractGameState, playerId: Int): Action {
        val intPerAction = gameState.codonsPerAction()
        if (genome.size >= intPerAction) {
            val gene = genome?.sliceArray(0 until intPerAction)
            genome = genome.sliceArray(intPerAction until genome.size)
            return gameState.translateGene(playerId, gene)
        } else {
            return NoAction
        }
    }

    override fun reset() = this

    override fun getAgentType() = "SimpleActionEvoAgentRollForward"

    override fun getForwardModelInterface(): SimpleActionPlayerInterface {
        return SimpleActionEvoAgentRollForward(genome.copyOf())
    }

}