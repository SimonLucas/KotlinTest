package tune

import evodef.*
import ntbea.params.BooleanParam
import ntbea.params.DoubleParam
import ntbea.params.IntegerParam
import ntbea.params.Param
import utilities.ElapsedTimer

class TuneEvoPolicyAgent : AnnotatedFitnessSpace {
    // todo: allow switching between Do Nothing and Random Opponent models
    override fun getParams(): Array<Param> {
        return arrayOf<Param>(
                DoubleParam().setArray(pointMutationRate).setName("Point Mutation Rate"),
                BooleanParam().setArray(flipAtLeastOneBit).setName("Flip at least one bit?"),
                BooleanParam().setArray(useShiftBuffer).setName("Use shift Buffer?"),
                IntegerParam().setArray(nResamples).setName("nResamples"),
                IntegerParam().setArray(seqLength).setName("sequence length"))
    }

    // todo: Use a different (longer?) range of sequence lengths
    var pointMutationRate = doubleArrayOf(0.0, 1.0, 2.0, 3.0, 5.0, 10.0)
    var flipAtLeastOneBit = booleanArrayOf(false, true)
    var useShiftBuffer = booleanArrayOf(false, true)
    var nResamples = intArrayOf(1, 2, 3)
    // int[] seqLength = {5, 10, 15, 20, 50};
    var seqLength = intArrayOf(2, 5, 10, 20, 40, 65, 100, 150, 200)
    var nValues = intArrayOf(pointMutationRate.size, flipAtLeastOneBit.size,
            useShiftBuffer.size, nResamples.size, seqLength.size)
    var nDims = nValues.size
    // NoisySolutionEvaluator problemEvaluator;
    var nGames = 1
    var maxSteps = 1000
    // log the solutions found
    var logger: EvolutionLogger

    fun report(solution: IntArray): String {
        val sb = StringBuilder()
        sb.append(String.format("pointMutationRate:     %.2f\n", pointMutationRate[solution[pointMutationRateIndex]]))
        sb.append(String.format("flipAtLeastOneBit:     %s\n", flipAtLeastOneBit[solution[flipAtLeastOneBitIndex]]))
        sb.append(String.format("useShiftBuffer:        %s\n", useShiftBuffer[solution[useShiftBufferIndex]]))
        sb.append(String.format("nResamples:            %d\n", nResamples[solution[nResamplesIndex]]))
        sb.append(String.format("seqLength:             %d\n", seqLength[solution[seqLengthIndex]]))
        sb.append(String.format("nEvals:                %d\n", getNEvals(solution)))
        return sb.toString()
    }

    fun getNEvals(solution: IntArray): Int {
        return tickBudget / seqLength[solution[seqLengthIndex]]
    }

    //    public EvoAgentSearchSpacePlanetWars setEvaluator(NoisySolutionEvaluator problemEvaluator) {
//        this.problemEvaluator = problemEvaluator;
//        return this;
//    }
    override fun nDims(): Int {
        return nDims
    }

    override fun nValues(i: Int): Int {
        return nValues[i]
    }

    // int innerEvals = 2000;
// int nEvals = 0;
    override fun reset() { // nEvals = 0;
        logger.reset()
    }

    var verbose = false
    override fun evaluate(x: IntArray): Double {

        // create a problem to evaluate this one on ...
// this should really be set externally, but just doing it this way for now
// search space will need to be set before use
        // todo now run a game and return the result
// use the evaluation code from yesterdays lab to evaluate the agent we already made
        if (verbose) {
        }
        // double value = Math.random();
//        logger.log(value, x, false)
//        return value

        // todo; do something sensible here
        return 0.0
    }

    override fun optimalFound(): Boolean {
        return false
    }

    override fun searchSpace(): SearchSpace {
        return this
    }

    override fun nEvals(): Int {
        return logger.nEvals()
    }

    override fun logger(): EvolutionLogger {
        return logger
    }

    override fun optimalIfKnown(): Double {
        return 0.0
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val searchSpace = TuneEvoPolicyAgent()
            var point = SearchSpaceUtil.randomPoint(searchSpace)
            point = intArrayOf(0, 0, 0, 0, 0)
            println(searchSpace.report(point))
            println()
            println("Size: " + SearchSpaceUtil.size(searchSpace))
            val timer = ElapsedTimer()
            println("Value: " + searchSpace.evaluate(point))
            println(timer)
        }

        var tickBudget = 400
        var pointMutationRateIndex = 0
        var flipAtLeastOneBitIndex = 1
        var useShiftBufferIndex = 2
        var nResamplesIndex = 3
        var seqLengthIndex = 4
    }

    init {
        logger = EvolutionLogger()
    }
}
