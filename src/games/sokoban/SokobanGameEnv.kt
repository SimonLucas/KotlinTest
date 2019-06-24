package games.sokoban

import evodef.EvolutionLogger
import evodef.NoisySolutionEvaluator
import evodef.SearchSpace
import games.simplegridgame.hyper.AgentFactorySpace
import ggi.SimplePlayerInterface
import utilities.ElapsedTimer
import utilities.StatSummary
import java.util.*

class SokobanGameEnv(_afs: AgentFactorySpace) : NoisySolutionEvaluator {

    private val agentFactorySpace: AgentFactorySpace = _afs
    private var counter:Int=0
    private val trainingLevels = 0..9
    private val testingLevels = 10..19
//    private val trainingLevels = -1..-1
//    private val testingLevels = -1..-1
    private val logger = EvolutionLogger()

    var trueFitnessSamples:Int=100
    var lastTrueFitnessValue:Double=0.0
    var lastTrueFitnessError:Double=0.0

    var span:Int=2
    var maxTicks:Int=100

    override fun evaluate(p0: IntArray?): Double {
        val agent=agentFactorySpace.agent(p0)
        counter++
        val timer=ElapsedTimer()
        val f=train(agent)
        println(Arrays.toString(p0)+" time "+timer.elapsed()+" fitness "+f)
        return f
    }

    fun train(agent: SimplePlayerInterface):Double{
        var accum=0.0
        for(level in trainingLevels){
            accum += play(level,agent)
        }
        return accum
    }

    fun test(agent: SimplePlayerInterface):Double{
        var accum=0.0
        for(level in testingLevels){
            accum += play(level,agent)
        }
        return accum
    }

    fun play(level: Int, agent:SimplePlayerInterface):Double{
        var game = Sokoban(level)
        var tick=0
        val actions = intArrayOf(0, 0)

        while (!game.isTerminal() && tick<maxTicks){
            actions[0] = agent.getAction(game.copy(), Constants.player1)
            game.next(actions)
            tick++
        }

        print(" $tick ")

        return game.score()
    }

    override fun trueFitness(p0: IntArray?): Double {
        val ss = StatSummary()
        val agent=agentFactorySpace.agent(p0)

        for(rep in 1..trueFitnessSamples){
            ss.add(test(agent))
        }

        lastTrueFitnessValue = ss.mean()
        lastTrueFitnessError = ss.stdErr()

        return ss.mean()
    }

    override fun optimalIfKnown(): Double {
        return 0.0
    }

    override fun logger(): EvolutionLogger {
        return logger
    }

    override fun isOptimal(p0: IntArray?): Boolean {
        return false
    }

    override fun searchSpace(): SearchSpace {
        return agentFactorySpace.searchSpace
    }

    override fun reset() {
        counter = 0
    }

    override fun nEvals(): Int {
        return counter
    }

    override fun optimalFound(): Boolean {
        return false
    }
}
