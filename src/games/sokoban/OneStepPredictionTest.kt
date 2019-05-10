package games.sokoban

import agents.RandomAgent
import ggi.SimplePlayerInterface
import utilities.ElapsedTimer
import utilities.StatSummary

fun main() {

    // val lfm = GPModel()
//    val lfm = DummyForwardModel()

    val span = 2

    //
    // val gatherer = GatherData(span)

    val gatherer = MultiLevelGatherer(trainLevels = 0..9).gatherData()
    // gatherer.report()
    println("Hashmap has ${gatherer.tileData.size} entries")

    var lfm  = LocalForwardModel(gatherer.tileData, gatherer.rewardData, span, false)


    val seed = 99L

    val tester = OneStepTester(lfm, RandomAgent(seed))
    val timer = ElapsedTimer()
    println(tester.runTests())
    println(timer)

}


class OneStepTester(val learnedModel: ForwardGridModel, val agent: SimplePlayerInterface = RandomAgent(99)) {

    var nStartsPerLevel = 100
    var nStepsPerLevel = 100

    val testLevels = 10..19
    var debug = false

    val ssTitle="One Step Prediction Stats: ${learnedModel}"

    fun runTests(): StatSummary {

        val ss = StatSummary(ssTitle)
        val actions = intArrayOf(0,0)

        for (i in testLevels) {
            for (j in 0 until nStartsPerLevel) {
                val game = Sokoban(i)
                for (k in 0 until nStepsPerLevel) {
                    val action = agent.getAction(game, Constants.player1)
                    actions[0] = action
                    learnedModel.setGrid(game.board.getSimpleGrid())

                    // advance both the true game and the learned model
                    game.next(actions)
                    learnedModel.next(actions)

                    // remember that in the game the Avatar is not stored in the grid
                    // to avoid overwriting the underlying tiles
                    // so we need to set it on the grid instead
                    val accuracy = accuracy(game.board.getSimpleGrid().grid, learnedModel.getGrid().grid)

                    ss.add(accuracy)
                }
            }
        }
        return ss
    }

    fun accuracy(x: CharArray, y: CharArray) : Double {
        // better to use require than assert in this context
        require(x.size == y.size)
        return (x zip y).count{(a,b) -> a == b} / x.size.toDouble()
    }
}