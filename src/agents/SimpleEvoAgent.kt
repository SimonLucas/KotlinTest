package agents

import ggi.AbstractGameState
import ggi.SimplePlayerInterface
import ggi.game.ActionAbstractGameState


import java.util.Random

fun shiftLeftAndRandomAppend(startingArray: IntArray, nShift: Int, nActions: Int): IntArray {
    val p = IntArray(startingArray.size)
    for (i in 0 until p.size)
        p[i] = when {
            i >= p.size - nShift -> random.nextInt(nActions)
            else -> startingArray[i + nShift]
        }
    return p
}

data class SimpleEvoAgent(
        var flipAtLeastOneValue: Boolean = true,
        // var expectedMutations: Double = 10.0,
        var probMutation: Double = 0.2,
        var sequenceLength: Int = 200,
        var nEvals: Int = 20,
        var useShiftBuffer: Boolean = true,
        var useMutationTransducer: Boolean = true,
        var repeatProb: Double = 0.5,  // only used with mutation transducer
        var discountFactor: Double? = null,
        var opponentModel: SimplePlayerInterface = DoNothingAgent()
) : SimplePlayerInterface {
    override fun getAgentType(): String {
        return "SimpleEvoAgent"
    }

    internal var random = Random()

    // these are all the parameters that control the agend
    internal var buffer: IntArray? = null // randomPoint(sequenceLength)

    // SimplePlayerInterface opponentModel = new RandomAgent();
    override fun reset(): SimplePlayerInterface {
        // buffer = null
        return this
    }

    val solutions = ArrayList<IntArray>()

    var x: Int? = 1


    fun getActions(gameState: AbstractGameState, playerId: Int): IntArray {
        var solution = buffer ?: randomPoint(gameState.nActions())
        if (useShiftBuffer) {
            if (solution == null)
                solution = randomPoint(gameState.nActions())
            else {
                val numberToShiftLeft = if (gameState is ActionAbstractGameState) gameState.codonsPerAction() else 1
                solution = shiftLeftAndRandomAppend(solution, numberToShiftLeft, gameState.nActions())
            }
        } else {
            // System.out.println("New random solution with nActions = " + gameState.nActions())
            solution = randomPoint(gameState.nActions())
        }
        solutions.clear()
        solutions.add(solution)
        for (i in 0 until nEvals) {
            // evaluate the current one
            val mut = mutate(solution, probMutation, gameState.nActions())
            val curScore = evalSeq(gameState.copy(), solution, playerId)
            val mutScore = evalSeq(gameState.copy(), mut, playerId)
            if (mutScore >= curScore) {
                solution = mut
            }
            solutions.add(solution)
        }
        buffer = solution
        return solution
    }

    private fun mutate(v: IntArray, mutProb: Double, nActions: Int): IntArray {

        if (useMutationTransducer) {
            // build it dynamically in case any of the params have changed
            val mt = MutationTransducer(mutProb, repeatProb)
            return mt.mutate(v, nActions)
        }

        val n = v.size
        val x = IntArray(n)
        // pointwise probability of additional mutations
        // choose element of vector to mutate
        var ix = random.nextInt(n)
        if (!flipAtLeastOneValue) {
            // setting this to -1 means it will never match the first clause in the if statement in the loop
            // leaving it at the randomly chosen value ensures that at least one bit (or more generally value) is always flipped
            ix = -1
        }
        // copy all the values faithfully apart from the chosen one
        for (i in 0 until n) {
            if (i == ix || random.nextDouble() < mutProb) {
                x[i] = mutateValue(v[i], nActions)
            } else {
                x[i] = v[i]
            }
        }
        return x
    }

    private fun mutateValue(cur: Int, nPossible: Int): Int {
        // the range is nPossible-1, since we
        // selecting the current value is not allowed
        // therefore we add 1 if the randomly chosen
        // value is greater than or equal to the current value
        if (nPossible <= 1) return cur
        val rx = random.nextInt(nPossible - 1)
        return if (rx >= cur) rx + 1 else rx
    }

    private fun randomPoint(nValues: Int): IntArray {
        val p = IntArray(sequenceLength)
        for (i in p.indices) {
            p[i] = random.nextInt(nValues)
        }
        return p
    }


    private fun evalSeq(gameState: AbstractGameState, seq: IntArray, playerId: Int): Double {
        return if (discountFactor == null) {
            evalSeqNoDiscount(gameState, seq, playerId)
        } else {
            evalSeqDiscounted(gameState, seq, playerId, discountFactor!!)
        }
    }

    private fun evalSeqNoDiscount(gameState: AbstractGameState, seq: IntArray, playerId: Int): Double {
        var gameState = gameState
        val current = gameState.score()
        val actions = IntArray(2)
        for (action in seq) {
            actions[playerId] = action
            actions[1 - playerId] = opponentModel.getAction(gameState, 1 - playerId)
            gameState = gameState.next(actions)
        }
        val delta = gameState.score() - current
        return if (playerId == 0)
            delta
        else
            -delta
    }

    private fun evalSeqDiscounted(gameState: AbstractGameState, seq: IntArray, playerId: Int, discountFactor: Double): Double {
        var gameState = gameState
        var currentScore = gameState.score()
        var delta = 0.0
        var discount = 1.0
        val actions = IntArray(2)
        for (action in seq) {
            actions[playerId] = action
            actions[1 - playerId] = opponentModel.getAction(gameState, 1 - playerId)
            gameState = gameState.next(actions)
            val nextScore = gameState.score()
            val tickDelta = nextScore - currentScore
            currentScore = nextScore
            delta += tickDelta * discount
            discount *= discountFactor
        }
        return if (playerId == 0)
            delta
        else
            -delta
    }

    override fun toString(): String {
        return "SEA: $nEvals : $sequenceLength : $opponentModel"
    }

    override fun getAction(gameState: AbstractGameState, playerId: Int): Int {
        return getActions(gameState, playerId)[0]
    }

    fun getSolutionsCopy(): ArrayList<IntArray> {

        val x = ArrayList<IntArray>()
        x.addAll(solutions)
        return x
    }
}
