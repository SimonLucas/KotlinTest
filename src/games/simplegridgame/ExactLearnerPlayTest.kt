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
import java.awt.Color
import java.awt.FlowLayout
import java.util.*
import java.io.File;
import javax.swing.JComponent

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
        val testSteps: Int,
        val gamesPerEval: Int,
        val w: Int,
        val h: Int,
        val visual:Boolean,
        val diceRoll:Boolean,
        val nReps:Int,
        val lutSize:Int,
        val outFileName:String
){

    fun run() {

        val t = ElapsedTimer()

        var game = setUpGame()

        val obss = StatSummary()
        val predResults = TreeMap<Int,StatSummary>()
        val scoreResults = TreeMap<Int,StatSummary>()
        //default agent is Simple EvoAgent
        for (i in 0 until nReps) {
            if (agent == 1) {
                runGames(DoNothingAgent(game.doNothingAction()), visual, predResults, scoreResults)
            } else if (agent == 2) {
                runGames(RandomAgent(), visual, predResults, scoreResults)
            } else {
                var agent1: SimplePlayerInterface = SimpleEvoAgent(flipAtLeastOneValue = true, probMutation = 0.3, sequenceLength = 20, nEvals = 25, useShiftBuffer = true, useMutationTransducer = false, repeatProb = 0.2, discountFactor = 0.8)
                if(!trueModel){
                    var learner = train(lutSize, RandomAgent())
                    obss.add(learner.lut.size)
                    //predictionTest(agent1, learner)
                    runGames(agent1, visual, predResults, scoreResults, learner)
                }else{
                    runGames(agent1, visual, predResults, scoreResults)
                }
            }
        }
        //output result
        val outFile =  File(outFileName)
        val isNewFileCreated: Boolean = outFile.createNewFile()
        log("max lut size\t average AI performance\t sd AI performance\t pred error mean\t pred error sd\t patterns observed mean\t patterns observed sd\n",
                outFile, isNewFileCreated)
        log("%d\t %.1f\t %.1f\t %.1f\t %.1f\t %.1f\t %.1f\n".format(lutSize, scoreResults[testSteps-1]?.mean(), scoreResults[testSteps-1]?.stdErr(),
                predResults[testSteps-1]?.mean(), predResults[testSteps-1]?.stdErr(),
                obss.mean(), obss.stdErr()),
                outFile, isNewFileCreated)
        log("time step\t average AI performance\t sd AI performance\t pred error mean\t pred error sd\n",
                outFile, isNewFileCreated)
        for(i in 0 until testSteps){
            log("%d\t %.1f\t %.1f\t %.1f\t %.1f\n".format(i, scoreResults[i]?.mean(), scoreResults[i]?.stdErr(),
                    predResults[i]?.mean(), predResults[i]?.stdErr()),
                    outFile, isNewFileCreated)
        }
        println("Total time: " + t)
    }

    fun log(text: String, outFile: File, toFile: Boolean){
        if(!toFile){
            print(text)
        }else{
            outFile.appendText(text)
        }
    }

    fun setUpGame() : SimpleGridGame{
        var game = SimpleGridGame(w, h)
        if(updateRule==1){
            game.updateRule = CaveUpdateRule()
        }else{
            game.updateRule = LifeUpdateRule()
        }
        game.rewardFactor = 1.0
        return game
    }


    fun train(lutSizeLimit: Int, agent1 : SimplePlayerInterface) : StatLearner {

        var game = setUpGame()

        learner = StatLearner()
        learner.lutSizeLimit = lutSizeLimit
        learner.diceRoll = diceRoll

        var agent2 = DoNothingAgent(game.doNothingAction())

        // first of all train the learner

        harvestData = true

        println("Training")
        val t = ElapsedTimer()
        loop@ for (i in 0 until testSteps) {
            val actions = intArrayOf(0, 0)
            actions[0] = agent1.getAction(game.copy(), Constants.player1)
            actions[1] = agent2.getAction(game.copy(), Constants.player2)
            game.next(actions)
            println("$i\t N distinct patterns learned = ${learner.lut.size}")
            if(learner.lut.size>=lutSizeLimit) break@loop
        }

        // learner.reportComparison()
        println(t)

        // todo: fix the error in the way the learner learns or is applied
        // even when trained with DoNothing agents and it sees ALL the patterns,
        harvestData = false
        return learner
    }


    fun runGames(agent: SimplePlayerInterface, visual: Boolean, predResults: TreeMap<Int,StatSummary>,
                 scoreResults: TreeMap<Int,StatSummary>, learner: StatLearner? = null) {
        println("Testing")
        for (i in 0 until gamesPerEval) {
            var game = setUpGame()
            val gridView = GridView(game.grid)
            if (visual) {
                JEasyFrame(gridView, "Grid Game")
            }
            for (j in 0 until testSteps) {
                val actions = intArrayOf(0, 0)

                val agentCopy = game.copy() as SimpleGridGame
                if(learner!=null){
                    agentCopy.updateRule = learner
                }

                // need to know how good this is
                actions[0] = agent.getAction(agentCopy.copy(), Constants.player1)
                actions[1] = DoNothingAgent().getAction(game.copy(), Constants.player2)
                game.next(actions)
                agentCopy.next(actions)

                val diff = game.grid.difference(agentCopy.grid)
                //println("Test ${j}, \t diff = ${diff}")
                //println("Game: ${j}, score = ${game.score()}")
                if(!predResults.containsKey(j)) {
                    predResults[j] = StatSummary()
                }
                predResults[j]?.add(diff)
                if(!scoreResults.containsKey(j)) {
                    scoreResults[j] = StatSummary()
                }
                scoreResults[j]?.add(game.score())
                if (visual) {
                    gridView.grid = game.grid
                    gridView.repaint()
                    Thread.sleep(100)
                }
            }
            println("Game: ${i+1}, \t score = ${game.score()}")
        }
        return
    }


   /* fun predictionTest(agent: SimplePlayerInterface, learnedRule: UpdateRule): StatSummary {
        // test and checking the differences a number of times
        val ss = StatSummary("Prediction errors")
        for (i in 0 until nPredictionTests) {
            val game = setUpGame()
            val other = game.copy() as SimpleGridGame
            other.updateRule = learnedRule

            // see if the two grids end in the same state
            val actions = intArrayOf(0, 0)
            actions[0] = agent.getAction(other.copy(), Constants.player1)
            actions[1] = DoNothingAgent().getAction(game.copy(), Constants.player2)

            // now see what goes next... one with learned rule, one with not

            game.next(actions)
            other.next(actions)
            val diff = game.grid.difference(other.grid)
            println("Test ${i}, \t diff = ${diff}")
            println("Game: ${i}, score = ${game.score()}")
            ss.add(diff)

        }
        return ss
    }*/
}


// made an update with




