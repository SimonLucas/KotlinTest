package games.eventqueuegame

import agents.SimpleEvoAgent
import agents.shiftLeftAndRandomAppend
import ggi.SimpleActionPlayerInterface
import ggi.game.Action
import ggi.game.ActionAbstractGameState
import test.junit.game

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

    override fun getPlan(gameState: ActionAbstractGameState, playerRef: Int): List<Action> {
        val genome = underlyingAgent.buffer
        return convertGenomeToActionList(genome, gameState, playerRef)
    }
}

fun convertGenomeToActionList(genome: IntArray?, gameState: ActionAbstractGameState, playerRef: Int): List<Action> {
    val intPerAction = gameState.codonsPerAction()
    if (genome == null || genome.isEmpty()) return listOf()
    val retValue = (0 until (genome.size / intPerAction)).map { i ->
        val gene = genome.sliceArray(i * intPerAction until (i+1) * intPerAction)
        gameState.translateGene(playerRef, gene)
    }
    return retValue
    // TODO: This does not roll the gameState forward yet, which it should do, but assumes all actions are
    // given from the current state...which is fine while I get the visualisation working
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

    override fun getPlan(gameState: ActionAbstractGameState, playerRef: Int): List<Action> {
        return convertGenomeToActionList(genome, gameState, playerRef)
    }

    override fun reset() = this

    override fun getAgentType() = "SimpleActionEvoAgentRollForward"

    override fun getForwardModelInterface(): SimpleActionPlayerInterface {
        return SimpleActionEvoAgentRollForward(genome.copyOf())
    }

}

object SimpleActionDoNothing : SimpleActionPlayerInterface {
    override fun getAction(gameState: ActionAbstractGameState, playerId: Int) = NoAction
    override fun getPlan(gameState: ActionAbstractGameState, playerId: Int) = emptyList<Action>()
    override fun reset() = this
    override fun getAgentType() = "SimpleActionDoNothing"
    override fun getForwardModelInterface() = this
}