package games.simplegridgame

import games.gridgame.Grid
import games.gridgame.MyRule
import games.gridgame.UpdateRule
import games.gridgame.vectorExtractor
import utilities.StatSummary
import java.util.HashMap

class StatLearner() : UpdateRule {

    var diceRoll = false;
    var lutSizeLimit = 400

    override fun cellUpdate(grid: Grid, x: Int, y: Int): Int {
        val probOn = getProb(vectorExtractor(grid, x, y))
        if (diceRoll)
            return if (random.nextDouble() < probOn) 1 else 0
        else
            return if (0.5 < probOn) 1 else 0
    }

    val lut = HashMap<ArrayList<Int>, StatSummary>()

    fun add(pattern: ArrayList<Int>, value: Int) {
        if (lut.size >= lutSizeLimit) return
        var ss = lut.get(pattern)
        if (ss == null) {
            ss = StatSummary()
            lut.put(pattern, ss)

        }
        ss.add(value)
    }

    fun getProb(pattern: ArrayList<Int>): Double {
        // return the probability of it being one or zero
        // based on the observed stats
        val ss = lut.get(pattern)
        // assume an equal likelihood of being on or off if we've not observed anything yet
        if (ss == null) return 0.5;
        // otherwise, calculate the probability with an epsilon backoff to regularise small samples
        return ss.mean()
    }

    fun getStats(pattern: ArrayList<Int>): StatSummary? = lut.get(pattern)

    fun report() {
        for (p in lut.keys) {
            println("${p} \t ${getProb(p)}")
        }
    }

    // quick hack for debugging
    fun reportComparison() {
        val ss = StatSummary("Differences")
        val otherRule = MyRule()
        otherRule.next = ::generalSumUpdate
        for (p in lut.keys) {
            val centre = p.get(4)
            val sum = p.sum() - centre
            val otherPrediction = otherRule.next(centre, sum)
            println("${p} \t ${getProb(p)} \t ${SimpleGridGame().lifeRule(p)} \t ${otherPrediction}}")
            ss.add(getProb(p) - SimpleGridGame().lifeRule(p))
        }
        println(ss)
    }
}

