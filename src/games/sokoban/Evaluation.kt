package games.sokoban

import agents.RandomAgent
import agents.SimpleEvoAgent
import ggi.SimplePlayerInterface


fun main(){
    val spansToTest = 1..3

    val trainLevels = 0..9
    val testLevels = 10..19

    val trainer = ModelTrainer(trainLevels, 100, 100)

    val models = ArrayList<GridModel>()
    var gridIterator : GridIterator
    for (i in spansToTest) {
        gridIterator = SquareGridIterator(i)
        models.add(Gatherer(gridIterator))
    }

    for (i in spansToTest) {
        gridIterator = CrossGridIterator(i)
        models.add(Gatherer(gridIterator))
    }

    for (i in spansToTest) {
        gridIterator = CrossGridIterator(i)
        models.add(DTModel(gridIterator))
    }

    for (i in spansToTest) {
        gridIterator = SquareGridIterator(i)
        models.add(DTModel(gridIterator))
    }

    println("Train Models")
    trainer.trainModel(models)

    models.add(Gatherer(CrossGridIterator(0)))
    models.forEach{ println(it.toString()) }
    println()

    testModelAccuracy(testLevels, models, nStartsPerLevel = 100, nStepsPerLevel = 100)
    testPlayingPerformance(testLevels, models, nStartsPerLevel = 100, nStepsPerLevel = 100)
}


fun testModelAccuracy(testLevels:IntRange,
                      models: ArrayList<GridModel>,
                      testAgent:SimplePlayerInterface = RandomAgent(99L),
                      nStartsPerLevel:Int = 100,
                      nStepsPerLevel:Int = 100){

    val forwardModels = ArrayList<ForwardGridModel>()
    models.forEach{
        if (it.toString().startsWith("HashSetModel"))
            forwardModels.add(LocalForwardModel((it as Gatherer).tileData, it.rewardData, it.gridIterator, false))
        else
            forwardModels.add((it as DTModel))
    }


    println("Test Model Accuracy")
    val tester = ModelTester(testLevels)
    val ss = tester.testOneStepAccuracy(forwardModels, testAgent, 100, 100)

    ss.forEach{
        println("${it.name}; " + "accuracy = " + "%.4f".format(it.mean()))
    }
    println()
}


fun testPlayingPerformance(testLevels:IntRange,
                           models: ArrayList<GridModel>,
                           testAgent: SimplePlayerInterface = SimpleEvoAgent(useMutationTransducer = false,
                                    sequenceLength = 20,
                                    nEvals = 50,
                                    //  discountFactor = 0.999,
                                    flipAtLeastOneValue = false,
                                    probMutation = 0.2),
                           nStartsPerLevel:Int = 100,
                           nStepsPerLevel:Int = 100){

    val forwardModels = ArrayList<ForwardGridModel?>()
    models.forEach{
        if (it.toString().startsWith("HashSetModel"))
            forwardModels.add(LocalForwardModel((it as Gatherer).tileData, it.rewardData, it.gridIterator, false))
        else
            forwardModels.add((it as DTModel))
    }

    println("Test Model Playing Performance")
    val tester = ModelTester(testLevels)
    val ss = tester.testPlayingPerformance(forwardModels, testAgent, nStartsPerLevel, nStepsPerLevel)

    ss.forEach{
        println("${it.name}" +
                ";\t avg score = " + "%.4f".format(it.mean()) +
                ";\t total score = " + "%.4f".format(it.sum()))
    }
    println()
}