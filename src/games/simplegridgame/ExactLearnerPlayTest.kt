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
        val diceRoll:Boolean,
        val nReps:Int,
        val lutSize:Int,
        val outFileName:String
){

    fun run() {

        val t = ElapsedTimer()

        var game = setUpGame()

        val ss = StatSummary()
        val predss = StatSummary()
        val obss = StatSummary()
        //default agent is Simple EvoAgent
        for (i in 0 until nReps) {
            if (agent == 1) {
                ss.add(runGames(DoNothingAgent(game.doNothingAction()), visual, false))
            } else if (agent == 2) {
                ss.add(runGames(RandomAgent(), visual, false))
            } else {
                var agent1: SimplePlayerInterface = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 5, nEvals = 20)
                if(!trueModel){
                    ss.add(runGames(agent1, visual, true, lutSize))
                }else{
                    ss.add(runGames(agent1, visual, false))
                }
            }
        }
        //output result
        // now format the results
        var outFile =  File(outFileName)
        val isNewFileCreated: Boolean = outFile.createNewFile()
        if(!isNewFileCreated){
            println("Error in creating outFile, not writing to file")
            println("max lut size\t average AI performance\t sd AI performance\t pred error mean\t pred error sd\t patterns observed mean\t patterns observed sd\n")
            print("%d\t %.1f\t %.1f\t".format(lutSize, ss.mean(), ss.stdErr()))
            if(predss.n()>0){
                print("%.1f\t %.1f\t".format(predss.mean(), predss.stdErr()))
                print("%.1f\t %.1f\n".format(obss.mean(), obss.stdErr()))
            }else{
                print("%s\t %s\t".format("-", "-"))
                print("%s\t %s\n".format("-", "-"))
            }

        }else {
            outFile.writeText("max lut size\t average AI performance\t sd AI performance\t pred error mean\t pred error sd\t patterns observed mean\t patterns observed sd\n")
            outFile.appendText("%d\t %.1f\t %.1f\t".format(lutSize, ss.mean(), ss.stdErr()))
            if(predss.n()>0){
                outFile.appendText("%.1f\t %.1f\t".format(predss.mean(), predss.stdErr()))
                outFile.appendText("%.1f\t %.1f\n".format(obss.mean(), obss.stdErr()))

            }else{
                outFile.appendText("%s\t %s\t".format("-", "-"))
                outFile.appendText("%s\t %s\n".format("-", "-"))
            }
        }
        println("Total time: " + t)
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
        return learner
    }


    fun runGames(agent: SimplePlayerInterface, visual: Boolean, learn: Boolean, lutSizeLimit: Int=0): StatSummary {
        println("Testing")
        val ss = StatSummary()
        val predResults = TreeMap<Int,StatSummary>()
        for (i in 0 until gamesPerEval) {
            val game = setUpGame()

            var learner = StatLearner()
            learner.lutSizeLimit = lutSizeLimit
            learner.diceRoll = diceRoll

            if(learn){
                harvestData = true
            }

            val gridView = GridView(game.grid)
            if (visual) {
                JEasyFrame(gridView, "Grid Game")
            }
            for (j in 0 until testSteps) {
                val actions = intArrayOf(0, 0)

                val agentCopy = game.copy() as SimpleGridGame
                if(learn){
                    agentCopy.updateRule = learner
                }

                // need to know how good this is
                actions[0] = agent.getAction(agentCopy.copy(), Constants.player1)
                actions[1] = DoNothingAgent().getAction(game.copy(), Constants.player2)
                game.next(actions)
                agentCopy.next(actions)

                val diff = game.grid.difference(agentCopy.grid)
                println("Test ${j}, \t diff = ${diff}")
                println("Game: ${j}, score = ${game.score()}")
                /*if(predResults.containsKey(i)){
                    var predss = predResults.get(i)
                    if(predss!= null){ //kotlin complains otherwise
                        predss.add(diff)
                        predResults.put(i, predss)
                    }
                }else{
                    var predss = StatSummary()
                    predss.add(diff)
                    predResults.put(i, predss)
                }*/
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


    fun predictionTest(agent: SimplePlayerInterface, learnedRule: UpdateRule): StatSummary {
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
            ss.add(diff)

        }
        return ss
    }
}


// made an update with




