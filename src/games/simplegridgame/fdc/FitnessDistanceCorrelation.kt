package games.simplegridgame.fdc

import agents.DoNothingAgent
import games.gridgame.UpdateRule
import games.gridgame.gameOfLife
import games.simplegridgame.*
import utilities.ElapsedTimer
import utilities.StatSummary
import java.util.*
import kotlin.random.Random

val predictionSteps = 5

// set this true to force a random rule
val forceRandom = true

fun main() {

    var rule: SimpleUpdateRule = LifeFun()
    rule = CaveFun()

    val reference = TruthTableRule().setRule(rule)

    val t = ElapsedTimer()
    val nReps = 30

    val predictions = TreeMap<Int,StatSummary>()
    val scores = TreeMap<Int,StatSummary>()
    for (i in 0 .. 0 step 10 ) {

        val pair = runTrial(reference, i, nReps)
        scores[i] = pair.first
        predictions[i] = pair.second

    }

    println("Scores (mean, s.e.)")
    report(scores)
    println("Prediction Errors (mean, s.e.)")
    report(predictions)

    println(t)

}

fun report(results: TreeMap<Int,StatSummary>) {
    results.forEach{key, value -> println("$key\t %.1f\t %.1f".format(value.mean(), value.stdErr()))}
}

fun runTrial(reference: TruthTableRule, diff: Int, nReps: Int) : Pair<StatSummary,StatSummary> {
    val scoreSS = StatSummary("Scores (dist = ${diff})")
    val predSS = StatSummary("Prediction Errors (dist = ${diff})")
    for (i in 0 until nReps) {
        // make a new one for each step of the way..
        val fdc = FitnessDistanceCorrelation()
        val drifter = TruthTableRule().setRule(reference)
        drifter.randomWalkFromStart(diff)

        if (forceRandom) drifter.randomise()

        val dist = reference.distance(drifter)
        println("Check: distance = " + dist)
        predSS.add(fdc.predictionTest(reference.updateRule(), drifter.updateRule(), predictionSteps))
        scoreSS.add(FalseModelPlayTest().playTests(reference.updateRule(), drifter.updateRule(), nGames = 1))

    }
    println(scoreSS)

    return Pair(scoreSS, predSS)

}

fun randomHammingTest(size: Int, nTrials: Int) : StatSummary {
    // use this to sanity check that average hamming distances are equal to size / 2
    val ss = StatSummary("Random hamming test (nDims = $size)")
    val rand = Random
    for (i in 0 until nTrials) {
        val x = IntArray(size, {rand.nextInt(2) } )
        val y = IntArray(size, {rand.nextInt(2) } )
        // println(x.size)
        var tot = 0
        for (ix in 0 until size) tot += Math.abs(x[ix] - y[ix])
        ss.add(tot)
    }
    return ss
}

fun simpleTest() {
    val ttr1 = TruthTableRule().setRule(LifeFun())
    val ttr2 = TruthTableRule().setRule(CaveFun())

    println(ttr1.distance(ttr2))
    println(ttr1.distance(ttr1))

    val t = ElapsedTimer()
    val fdc = FitnessDistanceCorrelation()
    val nTrials = 100
    for (i in 0 until nTrials) {
        fdc.predictionTest(ttr1.updateRule(), ttr1.updateRule())
    }
    println(fdc.ss)

    println(t)
}

interface SimpleUpdateRule { fun f(p: ArrayList<Int>) : Int }

class LifeFun : SimpleUpdateRule {
    override fun f(p: ArrayList<Int>) : Int {
        assert(p.size == 9)
        // find total excluding the centre
        var tot = p.sum() - p.get(4)
        if (p.get(4) == 0) {
            return if (tot == 3) 1 else 0
        } else {
            return if (tot == 2 || tot == 3) 1 else 0
        }
    }
}

class CaveFun : SimpleUpdateRule {
    override fun f(p: ArrayList<Int>) : Int {
        assert(p.size == 9)
        return if (p.sum() > 4) 1 else 0
    }
}



class TruthTableRule : SimpleUpdateRule {
    val lut = HashMap<ArrayList<Int>, Int>()
    // val cards = ArrayList<Int>()
    val stack = Stack<Int>()

    fun shuffleWalk() {
        (0 until lut.size).forEach{ t -> stack.add(t) }
        stack.shuffle()
        // println(stack)
    }

    fun randomWalkFromStart(nSteps: Int) {
        shuffleWalk()
        (0 until nSteps).forEach { walkAway() }
    }

    fun walkAway() : SimpleUpdateRule {
        val ix = if (!stack.empty()) stack.pop() else null
        if (ix != null) {
            val p = getPattern(ix)
            lut[p] = 1 - lut[p]!!
        }
        return this
    }

    fun getPattern(i: Int) : ArrayList<Int> {
        val a = ArrayList<Int>()
        i.toString(2).forEach { a.add(it.toInt() - '0'.toInt()) }
        // now append enough leading zeros
        while (a.size < 9) a.add(0, 0)
        return a
    }

    override fun f(p: ArrayList<Int>) : Int { return lut[p]!!}

    fun setRule(rule: SimpleUpdateRule) : TruthTableRule {
        for (i in 0 until 512) {
            val p = getPattern(i)
            lut[p] = rule.f(p)
        }
        // println("nPatterns = ${lut.size}")
        return this
    }

    fun setRule(statLearner: StatLearner) : TruthTableRule {
        for (i in 0 until 512) {
            val p = getPattern(i)
            lut[p] = statLearner.getProb(p).toInt()
        }
        println("nPatterns = ${lut.size}")
        return this
    }




    fun distance(ttr: TruthTableRule) : Int {
        var tot = 0
        lut.forEach { t, u ->
            val v = ttr.lut[t]
            tot += if (v!=null) Math.abs(u-v) else 1
        }
        return tot
    }

    fun updateRule() : UpdateRule {
        return LutRule(this)
    }

    fun randomise() {
        lut.forEach { t, u -> lut[t] = Random.nextInt(2) }

    }

}

class FitnessDistanceCorrelation {

    val ss = StatSummary("Prediction errors")

    fun predictionTest(r1: UpdateRule, r2: UpdateRule, predictionSteps: Int = 1) : StatSummary{
        // test and checking the differences a number of times
        val g1 = SimpleGridGame(w, h)
        val g2 = g1.copy() as SimpleGridGame

        g1.updateRule = r1
        g2.updateRule = r2

        val agent = DoNothingAgent()

        // bit of a clumsy way to set doNothing actions
        val action = agent.getAction(g1.copy(), 0)
        val actions = intArrayOf(action, action)

        for (i in 0 until predictionSteps) {
            g1.next(actions)
            g2.next(actions)
            // println("Test ${i}, \t diff = ${g1.grid.difference(g2.grid)}")
        }
        val diff = g1.grid.difference(g2.grid)
        // println("Test ${i}, \t diff = ${diff}")
        ss.add(diff)
        // println(ss)
        return ss
    }
}

