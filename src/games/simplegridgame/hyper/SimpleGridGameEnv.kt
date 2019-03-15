package games.simplegridgame.hyper

import agents.DoNothingAgent
import agents.RandomAgent
import evodef.EvolutionLogger
import evodef.NoisySolutionEvaluator
import evodef.SearchSpace
import games.simplegridgame.*
import ggi.SimplePlayerInterface
import utilities.StatSummary

class SimpleGridGameEnv(_afs:AgentFactorySpace) : NoisySolutionEvaluator{

    private val agentFactorySpace: AgentFactorySpace = _afs

    private val logger = EvolutionLogger()
    private var counter:Int=0
    private var myLearner=StatLearner()
    private val testSteps = 100
    var w:Int=30
    var h:Int=30
    var lutSize:Int=512
    var diceRoll:Boolean=false
    var trueFitnessSamples:Int=100
    var learningAgent:SimplePlayerInterface= RandomAgent()

    var lastTrueFitnessValue:Double=0.0
    var lastTrueFitnessError:Double=0.0

    fun train():Int{
        var game = SimpleGridGame(w, h)
        game.rewardFactor = 1.0
        myLearner.lutSizeLimit = lutSize
        myLearner.diceRoll = diceRoll

        var agent1: SimplePlayerInterface = learningAgent
        var agent2: SimplePlayerInterface = DoNothingAgent(game.doNothingAction())

        // first of all train the learner

        harvestData = true
        learner=myLearner
        println("[Training]: START")
        while(myLearner.lut.size<lutSize){
            val actions = intArrayOf(0, 0)
            actions[0] = agent1.getAction(game.copy(), Constants.player1)
            actions[1] = agent2.getAction(game.copy(), Constants.player2)
            game.next(actions)
            println("$\t N distinct patterns learned = ${myLearner.lut.size}")
        }
        println("[Training]: OVER")
        harvestData = false
        return myLearner.lut.size
    }

    override fun trueFitness(p0: IntArray?): Double {
        val ss = StatSummary()
        val agent = agentFactorySpace.agent(p0)
        for(i in 1..trueFitnessSamples){
            ss.add(runGame(agent,myLearner))
        }
        println("val: ${ss.mean()} stderr:${ss.stdErr()}")
        lastTrueFitnessValue = ss.mean()
        lastTrueFitnessError = ss.stdErr()
        return ss.mean()
    }

    override fun optimalFound(): Boolean {
        return false
    }

    override fun optimalIfKnown(): Double {
        return 0.0
    }

    override fun logger(): EvolutionLogger {
        return logger
    }

    override fun evaluate(p0: IntArray?): Double {
        harvestData = false
        counter++
        println("eval: $counter")
        val agent1:SimplePlayerInterface = agentFactorySpace.agent(p0)
        val result = runGame(agent1, myLearner)
        println("result: $result")
        return result
    }

    private fun runGame(agent:SimplePlayerInterface, learnedRule:StatLearner):Double{
        val game = SimpleGridGame(w, h)
        game.rewardFactor = 1.0

        for (i in 0 until testSteps) {
            val actions = intArrayOf(0, 0)

            val agentCopy = game.copy() as SimpleGridGame
            if (learnedRule != null) {
                agentCopy.updateRule = learnedRule
            }

            actions[0] = agent.getAction(agentCopy, Constants.player1)
            actions[1] = DoNothingAgent().getAction(game.copy(), Constants.player2)

            game.next(actions)
        }
        return game.score()
    }

    override fun isOptimal(p0: IntArray?): Boolean {
        return false
    }

    override fun searchSpace(): SearchSpace {
        return agentFactorySpace.searchSpace
    }

    override fun reset() {
        counter=0
    }

    override fun nEvals(): Int {
        return counter
    }

}
