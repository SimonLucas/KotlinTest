package games.simplegridgame

import agents.DoNothingAgent
import agents.RandomAgent
import agents.SimpleEvoAgent
import games.gridgame.MyRule
import games.gridgame.UpdateRule
import ggi.SimplePlayerInterface
import utilities.ElapsedTimer
import utilities.JEasyFrame
import utilities.StatSummary
import views.GridView
import java.util.*
import java.io.File;

// this test is to see how well we play when a learned model
// is used as a substitute for the true forward model

// step 1: train the agent for a number of steps
// step 2: run a number of games using its forward model

// learnSteps is the number of simulation ticks (state transitions)
// to run the model for to collect the training data
// however, there is a bug in that SimpleEvoAgent will run
// many simulations and the data harvester by default will
// collect data from all of them!

data class Experiment(
        val updateRule: Int,
        val agent: Int,
        val trueModel: Boolean,
        val learnSteps: Int,
        val testSteps: Int,
        val gamesPerEval: Int,
        val nPredictionTests: Int,
        val w: Int,
        val h: Int,
        val visual:Boolean,
        val lutSizeLimit: Int,
        val diceRoll:Boolean,
        val nReps:Int,
        val lutSize:Int,
        val outFileName:String
){

    fun run() {

        val t = ElapsedTimer()

        var game = setUpGame()

        val ss = StatSummary()
        //default agent is Simple EvoAgent
        for (i in 0 until nReps) {
            if (agent == 1) {
                ss.add(runGames(DoNothingAgent(game.doNothingAction()), game.updateRule, visual))
            } else if (agent == 2) {
                ss.add(runGames(RandomAgent(), game.updateRule, visual))
            } else {
                var agent1: SimplePlayerInterface = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 5, nEvals = 20)
                if(!trueModel){
                    var learner = train(lutSize, game, agent1)
                    ss.add(runGames(agent1, learner, visual))
                }else{
                    ss.add(runGames(agent1, game.updateRule, visual))
                }
            }
        }
        val results = TreeMap<Int,StatSummary>()
        results.put(lutSize, ss)

        //output result
        // now format the results
        var outFile =  File(outFileName)
        val isNewFileCreated: Boolean = outFile.createNewFile()
        if(!isNewFileCreated){
            println("Error in creating outFile, not writing to file")
            results.forEach{key, value -> println("$key\t %.1f\t %.1f".format(value.mean(), value.stdErr())) }

        }else {
            outFile.writeText("patterns seen\t average AI performance\t sd AI performance\n")
            results.forEach { key, value -> outFile.appendText("$key\t %.1f\t %.1f\n".format(value.mean(), value.stdErr())) }
        }
        println("Total time: " + t)
    }

    fun setUpGame() : SimpleGridGame{
        var game = SimpleGridGame(w, h)
        // (game.updateRule as MyRule).next = ::generalSumUpdate
        //default is default rule (whatever that is? TODO)
        if(updateRule==1){
            game.updateRule = CaveUpdateRule()
        }else{
            game.updateRule = LifeUpdateRule()
        }
        game.rewardFactor = 1.0;
        return game
    }


    fun train(lutSizeLimit: Int, game : SimpleGridGame, agent1 : SimplePlayerInterface) : StatLearner {

        learner.lutSizeLimit = lutSizeLimit
        learner.diceRoll = diceRoll

        var agent2 = DoNothingAgent(game.doNothingAction())

        // first of all train the learner

        harvestData = true

        println("Training")
        val t = ElapsedTimer()
        for (i in 0 until learnSteps) {
            val actions = intArrayOf(0, 0)
            actions[0] = agent1.getAction(game.copy(), Constants.player1)
            actions[1] = agent2.getAction(game.copy(), Constants.player2)
            game.next(actions)
            println("$i\t N distinct patterns learned = ${learner.lut.size}")
        }

        // learner.reportComparison()
        println(t)

        // todo: fix the error in the way the learner learns or is applied
        // even when trained with DoNothing agents and it sees ALL the patterns,
        harvestData = false
        predictionTest(learner)
        return learner
    }




    fun runGames(agent: SimplePlayerInterface, learnedRule: UpdateRule, visual: Boolean): StatSummary {
        println("Testing")
        val ss = StatSummary()
        for (i in 0 until gamesPerEval) {
            val game = SimpleGridGame(w, h)
            game.rewardFactor = 1.0;

            // game.updateRule = CaveUpdateRule()
            // game.updateRule = LifeUpdateRule()

            // game.updateRule = learnedRule

            val gridView = GridView(game.grid)
            if (visual) {
                JEasyFrame(gridView, "Grid Game")
            }
            for (i in 0 until testSteps) {
                val actions = intArrayOf(0, 0)

                val agentCopy = game.copy() as SimpleGridGame
                if (learnedRule != null) {
                    agentCopy.updateRule = learnedRule
                    // game.updateRule = learnedRule
                }

                // need to know how good this is
                actions[0] = agent.getAction(agentCopy, Constants.player1)

                // actions[0] = RandomAgent().getAction(agentCopy, Constants.player1)

                // println("Selected action: ${actions[0]}")

                // play against a DoNothing opponent for now...
                actions[1] = DoNothingAgent().getAction(game.copy(), Constants.player2)
                game.next(actions)
                // println(game.score())
                if (visual) {
                    gridView.grid = game.grid
                    gridView.repaint()
                    Thread.sleep(100)
                }

            }
            println("Game: ${i+1}, score = ${game.score()}")
            ss.add(game.score())
        }
        return ss
    }


    fun predictionTest(learnedRule: UpdateRule) {
        // test and checking the differences a number of times
        val ss = StatSummary("Prediction errors")
        for (i in 0 until nPredictionTests) {
            val game = SimpleGridGame(w, h)
            val other = game.copy() as SimpleGridGame
            other.updateRule = learnedRule
            // game.updateRule = learnedRule
            game.updateRule = LifeUpdateRule()

            val agent = DoNothingAgent()

            // see if the two grids end in the same state
            val action = agent.getAction(game.copy(), 0)
            val actions = intArrayOf(action, action)

            // now see what goes next... one with learned rule, one with not

            game.next(actions)
            other.next(actions)
            val diff = game.grid.difference(other.grid)
            println("Test ${i}, \t diff = ${diff}")
            ss.add(diff)

        }
        println(ss)
    }
}


// made an update with




