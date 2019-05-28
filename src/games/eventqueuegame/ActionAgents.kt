package games.eventqueuegame

import agents.SimpleEvoAgent
import ggi.AbstractGameState
import ggi.SimpleActionPlayerInterface
import ggi.game.Action
import ggi.game.ActionAbstractGameState
import test.junit.game
import java.lang.AssertionError

class NoAction<T : ActionAbstractGameState> : Action<T> {
    override fun apply(state: T): T {
        // Do absolutely nothing
        return state
    }

    override fun visibleTo(player: Int, state: T) = true
}

class SimpleActionEvoAgent(val underlyingAgent: SimpleEvoAgent = SimpleEvoAgent(),
                           val opponentModel: SimpleActionPlayerInterface= SimpleActionDoNothing) : SimpleActionPlayerInterface {

    override fun reset(): SimpleActionPlayerInterface {
        underlyingAgent.reset()
        opponentModel.reset()
        return this
    }

    override fun getAgentType() = "SimpleActionEvoAgent: $underlyingAgent"

    override fun <T : ActionAbstractGameState> getAction(gameState: T, playerRef: Int): Action<T> {
        if (gameState is EventQueueGame) {
            val intPerAction = gameState.codonsPerAction()
            // the underlyingAgent does all the work on mutating the genome
            // we're just a wrapper for it
            if (opponentModel != null) {
                opponentModel.getAction(gameState, 1 - playerRef)
                // this is just to give the opponent model some thinking time
                underlyingAgent.opponentModel = opponentModel.getForwardModelInterface()
            }
            val gene = underlyingAgent.getActions(gameState, playerRef).sliceArray(0 until intPerAction)
            return gameState.translateGene(playerRef, gene) as Action<T>
        }
        throw AssertionError("Unexpected type of GameState $gameState")
    }

    override fun getForwardModelInterface(): SimpleActionPlayerInterface {
        return SimpleActionEvoAgentRollForward((underlyingAgent.buffer ?: intArrayOf()).copyOf())
    }

    override fun <T : ActionAbstractGameState> getPlan(gameState: T, playerRef: Int): List<Action<T>> {
        val genome = underlyingAgent.buffer
        return convertGenomeToActionList(genome, gameState, playerRef)
    }
    override fun backPropagate(finalScore: Double) {}
}

fun <T : ActionAbstractGameState> convertGenomeToActionList(genome: IntArray?, gameState: T, playerRef: Int): List<Action<T>> {
    val intPerAction = gameState.codonsPerAction()
    if (genome == null || genome.isEmpty()) return listOf()
    val retValue = (0 until (genome.size / intPerAction)).map { i ->
        val gene = genome.sliceArray(i * intPerAction until (i + 1) * intPerAction)
        gameState.translateGene(playerRef, gene)
    }
    return retValue as List<Action<T>>
    // TODO: This does not roll the gameState forward yet, which it should do, but assumes all actions are
    // given from the current state...which is fine while I get the visualisation working
}

/*
Will take actions using a specified genome...until the sequence runs out
 */
class SimpleActionEvoAgentRollForward(var genome: IntArray) : SimpleActionPlayerInterface {

    override fun <T : ActionAbstractGameState> getAction(gameState: T, playerId: Int): Action<T> {
        val intPerAction = gameState.codonsPerAction()
        if (genome.size >= intPerAction) {
            val gene = genome?.sliceArray(0 until intPerAction)
            genome = genome.sliceArray(intPerAction until genome.size)
            return gameState.translateGene(playerId, gene) as Action<T>
        } else {
            return NoAction()
        }
    }

    override fun <T : ActionAbstractGameState> getPlan(gameState: T, playerRef: Int): List<Action<T>> {
        return convertGenomeToActionList(genome, gameState, playerRef)
    }

    override fun reset() = this

    override fun getAgentType() = "SimpleActionEvoAgentRollForward"

    override fun getForwardModelInterface(): SimpleActionPlayerInterface {
        return SimpleActionEvoAgentRollForward(genome.copyOf())
    }
    override fun backPropagate(finalScore: Double) {}
}

object SimpleActionDoNothing : SimpleActionPlayerInterface {
    override fun <T : ActionAbstractGameState> getAction(gameState: T, playerId: Int) = NoAction<T>()
    override fun <T : ActionAbstractGameState> getPlan(gameState: T, playerId: Int) = emptyList<Action<T>>()
    override fun reset() = this
    override fun getAgentType() = "SimpleActionDoNothing"
    override fun getForwardModelInterface() = this
    override fun backPropagate(finalScore: Double) {}
}