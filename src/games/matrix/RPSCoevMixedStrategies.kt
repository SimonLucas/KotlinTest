package games.matrix

import evodef.EvolutionLogger
import evodef.FitnessSpace
import evodef.SearchSpace

class RPSCoevMixedStrategies : FitnessSpace {

    override fun evaluate(x: IntArray): Double {
        // note that this must be made to work just for point p, even if it really makes no sense for the RPS example
        // for now, pick a random strategy to play against for one game

        // convert the int array to probability weights (they need normalising to convert to probabilities
        val p = weights.p

        // players will be a and b, and we'll get the weights for each and make two players
        val wa = doubleArrayOf( p[x[0]], p[x[1]], p[x[2]] )
        val wb = doubleArrayOf( p[x[3]], p[x[4]], p[x[5]] )

        // create a mixed strategy with these weights
        // setProbs does the normalisation
        val playerA = MixedStrategy(wa.size).setProbs(wa)
        val playerB = MixedStrategy(wa.size).setProbs(wb)

        val game = RPS()

        val outcome = game.payOff(playerA.getAction(), playerB.getAction())

        // just return this player's payoff for now
        val fitness = outcome[0].toDouble()
        logger.log(fitness, x, false)
        return fitness
    }

    var logger: EvolutionLogger

    init {
        logger = EvolutionLogger()
    }

    override fun nDims(): Int {
        return 6
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

    override fun nValues(p0: Int): Int {
        return weights.p.size
    }

    override fun searchSpace(): SearchSpace {
        return this
    }

    override fun reset() {
        logger = EvolutionLogger()
    }

    override fun nEvals(): Int {
        return logger.nEvals()
    }
}