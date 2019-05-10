package games.sokoban

import agents.RandomAgent

// val lfm = GPModel()
//    val lfm = DummyForwardModel()

fun main(){

    val maxspan = 10
    //
    // val gatherer = GatherData(span)
    var gridIterator : GridIterator
    var gatherer : Gatherer

    for (i in 1..maxspan) {
        gridIterator = CrossGridIterator(i)
        gatherer = MultiLevelGatherer(trainLevels = 0..9, gridIterator = gridIterator).gatherData()
        // gatherer.report()
        val seed = 99L
        val lfm  = LocalForwardModel(gatherer.tileData, gatherer.rewardData, gridIterator, false)
        val tester = OneStepTester(lfm, RandomAgent(seed))
        val ss  = tester.runTests()

        println("CrossGridIterator, span=$i, Hashmap has ${gatherer.tileData.size} entries, avg=${ss.mean()}")
    }

    for (i in 1..maxspan) {
        gridIterator = SquareGridIterator(i)
        gatherer = MultiLevelGatherer(trainLevels = 0..9, gridIterator = gridIterator).gatherData()
        // gatherer.report()

        val seed = 99L
        val lfm  = LocalForwardModel(gatherer.tileData, gatherer.rewardData, gridIterator, false)
        val tester = OneStepTester(lfm, RandomAgent(seed))
        val ss  = tester.runTests()

        println("SquareGridIterator, span=$i, Hashmap has ${gatherer.tileData.size} entries, avg=${ss.mean()}")
    }



}
