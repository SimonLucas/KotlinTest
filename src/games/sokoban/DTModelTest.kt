package games.sokoban

import agents.SimpleEvoAgent
import ggi.SimplePlayerInterface
import utilities.ElapsedTimer
import utilities.StatSummary


fun main() {

    val nGames = 100
    val useLearnedModel = true
    val dummySpeedTest = false
    val span = 2
    val maxSteps = 100
    val gatherer = GatherData(span)

    var lfm: ForwardGridModel = LocalForwardModel(gatherer.tileData, gatherer.rewardData, CrossGridIterator(2), dummySpeedTest)
    // lfm = GPModel()
    val t = ElapsedTimer()
    val agent: SimplePlayerInterface = SimpleEvoAgent(
            useMutationTransducer = false, sequenceLength = 40, nEvals = 50,
//            discountFactor = 0.999,
            flipAtLeastOneValue = false,
            probMutation = 0.2)
    // agent = RandomAgent()

    // learn a forward model

    val tester = AgentTester(maxSteps, useLearnedModel)
    tester.runModelTests(nGames, agent, lfm)
    val elapsed = t.elapsed()

    println(t)
    println("total game ticks     = %e".format( Sokoban().totalTicks().toDouble()))
    println("learned model ticks  = %e".format( lfm.totalTicks().toDouble()))
    println("mTicks/s for game    = %.2f".format( Sokoban().totalTicks().toDouble() * 1e-3 / elapsed))
    println("mTicks/s for model   = %.2f".format( lfm.totalTicks().toDouble() * 1e-3 / elapsed))

}
