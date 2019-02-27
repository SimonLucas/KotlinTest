package games.gridgame

import utilities.StatSummary
import java.util.HashMap

class StatLearner () {
    val lut = HashMap<ArrayList<Int>, StatSummary>()

    fun add(pattern: ArrayList<Int>, value: Int) {
        var ss = lut.get(pattern)
        if (ss == null) {
            ss = StatSummary()
            lut.put(pattern, ss)
        }
        ss.add(value)
    }

    // not using epsilon yet
    val epsilon = 0.1;

    fun getProb(pattern: ArrayList<Int>) : Double {
        // return the probability of it being one or zero
        // based on the observed stats
        val ss = lut.get(pattern)
        // assume an equal likelihood of being on or off if we've not observed anything yet
        if (ss == null) return 0.5;
        // otherwise, calculate the probability with an epsilon backoff to regularise small samples
        return ss.mean()
    }

    fun getStats(pattern: ArrayList<Int>) : StatSummary? = lut.get(pattern)

    fun report() {
        for (p in lut.keys) {
            println("${p} \t ${getProb(p)}" )
        }
    }
}

