package ntbca.test

import evodef.EvalMaxM
import games.matrix.RPSCoevMixedStrategies
import games.matrix.RPSEvoMixedStrategy
import ntbea.NTupleBanditEA
import ntbea.NTupleSystem
import ntbea.NTupleSystemReport
import utilities.ElapsedTimer
import java.util.*

fun main(args: Array<String>) {

    // val problem = RPSEvoMixedStrategy()

    val problem = RPSCoevMixedStrategies()

    val kExplore = 2.0
    val epsilon = 0.5

    val banditEA = NTupleBanditEA().setKExplore(kExplore).setEpsilon(epsilon)

    // set a particlar NTuple System as the model
    // if this is not set, then it will use a default model

    val model = NTupleSystem()
    // set up a non-standard tuple pattern
    model.use1Tuple = true
    model.use2Tuple = true
    model.use3Tuple = false
    model.useNTuple = true
    banditEA.model = model
    val timer = ElapsedTimer()
    val nEvals = 100000
    val solution = banditEA.runTrial(problem, nEvals)
    println("Report: ")
    NTupleSystemReport().setModel(model).printDetailedReport()
    NTupleSystemReport().setModel(model).printSummaryReport()
    println("Model created: ")
    println(model)
    println("Model used: ")
    println(banditEA.model)
    println()
    println("Solution returned: " + Arrays.toString(solution))
    println(timer)
}
