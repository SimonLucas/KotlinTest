package tune

import agents.PolicyEvoAgent
import evodef.EvolutionLogger
import evodef.SearchSpace
import evodef.SolutionEvaluator
import games.gridworld.GridWorld
import games.gridworld.MinDistancePolicy
import ntbea.NTupleBanditEA
import ntbea.NTupleSystem
import ntbea.NTupleSystemReport
import utilities.ElapsedTimer
import utilities.StatSummary
import java.util.*
import kotlin.reflect.KMutableProperty


// data class KParam

fun main() {

    val agent = PolicyEvoAgent()

    val pap = PolicyAgentParams()


    val tuner = TuneEvoPolicyAgent()


    val ntbea = NTupleBanditEA()

    val kExplore = 2.0
    val epsilon = 1.0
    val banditEA = NTupleBanditEA().setKExplore(kExplore).setEpsilon(epsilon)
    // set a particlar NTuple System as the model
    // if this is not set, then it will use a default model
    // set a particlar NTuple System as the model
// if this is not set, then it will use a default model
    val model = NTupleSystem()
    // set up a non-standard tuple pattern
    // set up a non-standard tuple pattern
    model.use1Tuple = true
    model.use2Tuple = true
    model.use3Tuple = false
    model.useNTuple = false
    banditEA.model = model

    val timer = ElapsedTimer()
    val nEvals = 1000
    val solution = banditEA.runTrial(tuner, nEvals)

    println("Report: ")
    NTupleSystemReport().setModel(model).printDetailedReport()
    NTupleSystemReport().setModel(model).printSummaryReport()

    pap.report(solution)

    pap.inject(solution, agent)

    val stats = tuner.evalN(solution, 100)

    println()
    println("Checking: agent sequence length = " + agent.sequenceLength)

    println(stats)
    println()
    println(timer)

}

class PolicyAgentParams : SearchSpace {

    val p1 = PolicyEvoAgent::sequenceLength

    val booleans = arrayOf(false, true)
    // int[] seqLength = {5, 10, 15, 20, 50};
    val seqLength = arrayOf(2, 5, 10, 20, 40, 65, 100, 150, 200)
    val discounts = arrayOf(1.0, 0.99, 0.95, 0.9)
    val probs = arrayOf(0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6)
    val heuristics = arrayOf<Any>(MinDistancePolicy())


    // val c = PolicyEvoAgent::
    val paramMap = mapOf(
            PolicyEvoAgent::nEvals to arrayOf(10, 20, 30, 40, 50),
            PolicyEvoAgent::sequenceLength to seqLength,
            PolicyEvoAgent::flipAtLeastOneValue to booleans,
            PolicyEvoAgent::useShiftBuffer to booleans,
            PolicyEvoAgent::discountFactor to discounts,
            PolicyEvoAgent::useMutationTransducer to booleans,
            PolicyEvoAgent::repeatProb to probs,
            PolicyEvoAgent::probMutation to probs,
//            PolicyEvoAgent::policy to heuristics,
//            PolicyEvoAgent::valueFunction to heuristics,
            PolicyEvoAgent::initUsingPolicy to probs,
            PolicyEvoAgent::appendUsingPolicy to probs,
            PolicyEvoAgent::mutateUsingPolicy to probs
    )

    val indexMap = TreeMap<Int, KMutableProperty<out Any>>()

    // many of the following operations could be placed in a utility class
    init {
        var i = 0
        for (p in paramMap.keys) {
            println(i)
            indexMap[i++] = p
        }
    }

    override fun nDims(): Int {
        return paramMap.size
    }

    override fun nValues(ix: Int): Int {
        return paramMap[indexMap[ix]]!!.size
    }

    fun report(solution: IntArray) {
        for (i in indexMap.keys) {
            val p = indexMap[i]
            val valIndex = solution[i]
            val v = paramMap[p]!![valIndex]
            println("${p!!.name}\t $v")
        }
    }

    fun inject(solution: IntArray, agent: PolicyEvoAgent) {
        for (i in indexMap.keys) {
            val p = indexMap[i]!!
            val valIndex = solution[i]
            val v = paramMap[p]!![valIndex]
            // now have to set it
            p.setter.call(agent, v)
            // println("Injected:  ${p!!.name}\t $v")
        }
    }
}


class TuneEvoPolicyAgent : SearchSpace, SolutionEvaluator {
    val params = PolicyAgentParams()
    var nGames = 1
    var maxSteps = 1000
    // log the solutions found
    var logger: EvolutionLogger

    val masterGrid = GridWorld()

    init {
        masterGrid.readFile("data/GridWorld/Levels/level-3.txt")
    }

    fun report(solution: IntArray) = params.report(solution)

    override fun nDims() = params.nDims()

    override fun nValues(i: Int) = params.nValues(i)

    // int innerEvals = 2000;
// int nEvals = 0;
    override fun reset() { // nEvals = 0;
        logger.reset()
    }

    var verbose = false

    override fun evaluate(x: IntArray): Double {
        val score = evalOnce(x)
        if (verbose) {
            println(score)
        }
        logger.log(score, x, false)
        return score
    }

    fun evalN(x: IntArray, nEvals: Int) : StatSummary {
        val stats = StatSummary("Test results")
        for (i in 0 until nEvals) {
            stats.add(evalOnce(x))
        }
        return stats
    }

    fun evalOnce(x: IntArray) : Double{
        var heuristic: MinDistancePolicy? = MinDistancePolicy()
        // heuristic = null
        val agent = PolicyEvoAgent(valueFunction = heuristic, policy = heuristic)

        params.inject(x, agent)

        var gridWorld = masterGrid.copy()

        var step = 0
        val maxSteps = 100

        while (!gridWorld.isTerminal() && step++ < maxSteps) {
            val action = agent.getAction(gridWorld.copy(), 0)
            gridWorld = gridWorld.next(intArrayOf(action)) as GridWorld
            // println("${step++} -> \t ${gridWorld.score()}")
        }
        return gridWorld.score()
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

    init {
        logger = EvolutionLogger()
    }
}
