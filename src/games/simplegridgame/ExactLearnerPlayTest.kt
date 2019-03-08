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
        val startLut:Int,
        val endLut:Int,
        val stepLut:Int,
        val outFileName:String
){

    fun run() {

        val t = ElapsedTimer()

        val lutSizes = startLut .. endLut step stepLut
        println(lutSizes)
        val results = TreeMap<Int,StatSummary>()
        for (lut in lutSizes) {
            println("Getting results for lut size: $lut")
            val ss = StatSummary()
            for (i in 0 until nReps)
                ss.add(trainAndPlay(lut))
            results.put(lut,ss)
            println(ss)
            println()
        }

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



    fun trainAndPlay(lutSizeLimit: Int) : StatSummary {

        var game = SimpleGridGame(w, h)
        // (game.updateRule as MyRule).next = ::generalSumUpdate
        //
        //
        // game.updateRule = LifeUpdateRule()

        // game.updateRule = CaveUpdateRule()
        // game.updateRule =
        game.rewardFactor = 1.0;
        learner.lutSizeLimit = lutSizeLimit
        learner.diceRoll = diceRoll

        var agent1: SimplePlayerInterface = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 5, nEvals = 20)
        var agent2: SimplePlayerInterface = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 5, nEvals = 10)
        agent1 = RandomAgent()
        // agent1 = DoNothingAgent(game.doNothingAction())
        agent2 = DoNothingAgent(game.doNothingAction())

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

        // System.exit(0)

        println("Testing")
        val ss = runGames(agent1, learner, visual)
        println(ss)
        // learner.reportComparison()
        println(t)

        return ss

    }




    fun runGames(agent: SimplePlayerInterface, learnedRule: UpdateRule, visual: Boolean): StatSummary {
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




