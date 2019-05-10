package games.sokoban

import agents.RandomAgent
import agents.SimpleEvoAgent
import ggi.SimplePlayerInterface
import utilities.ElapsedTimer
import utilities.StatSummary


fun main() {
    val nGames = 1
    val useLearnedModel = true
    val pretrainModel = true
    val span = 2
    val maxSteps = 100

    var elapsed : Long

    println("Test learning speed with arbitrary pattern")
    val timer = ElapsedTimer()

    val gridIterator = CrossGridIterator(2)
    useFastPrediction = false
    timer.reset()
    var learnedModel = DTModel(gridIterator)
    if (pretrainModel) ModelTrainer().trainModel(learnedModel)

    elapsed = timer.elapsed()
    println("learning: elapsed time fastPrediction=false: $elapsed")
    println()

    //define model and agent
    timer.reset()
    useFastPrediction = true
    learnedModel = DTModel(gridIterator)
    if (pretrainModel) ModelTrainer().trainModel(learnedModel)

    elapsed = timer.elapsed()
    println("learning: elapsed time fastPrediction=true: $elapsed")


    println("Test prediction speed with arbitrary pattern")
    val agent: SimplePlayerInterface = SimpleEvoAgent(
            useMutationTransducer = false, sequenceLength = 40, nEvals = 50,
//            discountFactor = 0.999,
            flipAtLeastOneValue = false,
            probMutation = 0.2)

    println("total analysed patter ${learnedModel.totalAnalysedPatterns}")
    //run agent test
    val tester = AgentTesterDT(maxSteps, useLearnedModel)

    useFastPrediction = true
    timer.reset()
    tester.runModelTests(nGames, agent, learnedModel)
    elapsed = timer.elapsed()
    println("prediction: elapsed time fastPrediction=true: $elapsed")

    useFastPrediction = false
    timer.reset()
    tester.runModelTests(nGames, agent, learnedModel)
    elapsed = timer.elapsed()
    println("prediction: elapsed time fastPrediction=false: $elapsed")

}
