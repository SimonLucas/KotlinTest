package games.sokoban

import agents.RandomAgent
import agents.SimpleEvoAgent
import ggi.SimplePlayerInterface
import utilities.ElapsedTimer
import utilities.StatSummary

fun main() {

    // val lfm = GPModel()
//    val lfm = DummyForwardModel()

    val span = 10

    //
    // val gatherer = GatherData(span)
    val gridIterator = CrossGridIterator(span)
    val gatherer = Gatherer(gridIterator = gridIterator)
    ModelTrainer(0..9).trainModel(gatherer)

    // gatherer.report()
    println("Hashmap has ${gatherer.tileData.size} entries")

    val lfm  = LocalForwardModel(gatherer.tileData, gatherer.rewardData, gridIterator, false)

    val seed = 99L

    val tester = ModelTester()
    val timer = ElapsedTimer()
    println(tester.testOneStepAccuracy(lfm))
    println(timer)

}


class ModelTester(private val testLevels : IntRange = 10..19) {

    var EvoAgent: SimplePlayerInterface = SimpleEvoAgent(
            useMutationTransducer = false, sequenceLength = 40, nEvals = 50,
//            discountFactor = 0.999,
            flipAtLeastOneValue = false,
            probMutation = 0.2)

    fun testOneStepAccuracy(learnedModel: ForwardGridModel,
                            agent: SimplePlayerInterface = RandomAgent(99),
                            nStartsPerLevel:Int = 100,
                            nStepsPerLevel:Int = 100): StatSummary {
        val a = ArrayList<ForwardGridModel>()
        a.add(learnedModel)
        return testOneStepAccuracy(a, agent, nStartsPerLevel, nStepsPerLevel)[0]
    }

    fun testOneStepAccuracy(learnedModels: ArrayList<ForwardGridModel>,
                            agent: SimplePlayerInterface = RandomAgent(99),
                            nStartsPerLevel:Int = 100,
                            nStepsPerLevel:Int = 100): ArrayList<StatSummary> {

        val ss = ArrayList<StatSummary>()
        learnedModels.forEach {
            ss.add(StatSummary("One Step Prediction Stats: $it"))
        }
        println("Testing learnedModels using ${agent.getAgentType()} for a total of " +
                "${testLevels.count() * nStartsPerLevel} games each")

        val actions = intArrayOf(0,0)

        var nr_of_games = 0
        for (i in testLevels) {
            for (j in 0 until nStartsPerLevel) {
                nr_of_games++
                val game = Sokoban(i)
                for (k in 0 until nStepsPerLevel) {
                    val action = agent.getAction(game, Constants.player1)
                    actions[0] = action


                    //set grid and advance according to action
                    learnedModels.forEach{
                        it.setGrid(game.board.getSimpleGrid())
                        it.next(actions)
                    }


                    //advance the game using true model
                    game.next(actions)

                    // remember that in the game the Avatar is not stored in the grid
                    // to avoid overwriting the underlying tiles
                    // so we need to set it on the grid instead
                    // getSimpleGrid() does this already
                    for (x in 0 until learnedModels.size){
                        ss[x].add(accuracy(game.board.getSimpleGrid().grid, learnedModels[x].getGrid().grid))
                    }
                }
            }
        }
        return ss
    }

    fun testPlayingPerformance(learnedModel: ForwardGridModel,
                               agent: SimplePlayerInterface = EvoAgent,
                               nStartsPerLevel:Int = 100,
                               nStepsPerLevel:Int = 100,
                               addTrueModel: Boolean = true): StatSummary {
        val a = ArrayList<ForwardGridModel?>()
        a.add(learnedModel)
        return testPlayingPerformance(a, agent, nStartsPerLevel, nStepsPerLevel)[0]
    }

    fun testPlayingPerformance(learnedModels: ArrayList<ForwardGridModel?>,
                               agent: SimplePlayerInterface = EvoAgent,
                               nStartsPerLevel:Int = 100,
                               nStepsPerLevel:Int = 100,
                               addTrueModel: Boolean = true): ArrayList<StatSummary> {

        val ss = ArrayList<StatSummary>()
        learnedModels.forEach {
            ss.add(StatSummary("GamePlaying Performance Stats: $it"))
        }
        if (addTrueModel){
            learnedModels.add(null)
            ss.add(StatSummary("GamePlaying Performance Stats: TrueForwardModel"))
        }

        println("Testing learned models using ${agent.getAgentType()} for a " +
                "total of ${testLevels.count() * nStartsPerLevel} games each")
        var nrOfGames = 0
        for (levelid in testLevels) {
            for (j in 0 until nStartsPerLevel) {
                nrOfGames++
                println("Simulating game number: $nrOfGames out of ${testLevels.count()*nStartsPerLevel}")
                for (x in 0 until learnedModels.size){
                    ss[x].add(runModelGame(agent, learnedModels[x], levelid))
                }
            }
        }
        //println("$nrOfGames simulated games per agent")
        return ss
    }

    private fun runModelGame(agent: SimplePlayerInterface,
                             lfm: ForwardGridModel?,
                             levelid: Int,
                             nStepsPerLevel:Int = 100): Double {
        val game = Sokoban(levelid)
        val actions = intArrayOf(0, 0)
        var i = 0
        var gameOver = false

        while (i < nStepsPerLevel && !gameOver) {
            //Take and execute actions
            if (lfm != null) {
                lfm.setGrid(game.board.getSimpleGrid())
                actions[0] = agent.getAction(lfm, Constants.player1)
            } else {
                actions[0] = agent.getAction(game.copy(), Constants.player1)
            }

            game.next(actions)
            gameOver = game.isTerminal()
            i++
        }
        return game.score()
    }

    private fun accuracy(x: CharArray, y: CharArray) : Double {
        // better to use require than assert in this context
        require(x.size == y.size)
        return (x zip y).count{(a,b) -> a == b} / x.size.toDouble()
    }
}
