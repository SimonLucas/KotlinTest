package test

import utilities.StatSummary
import java.util.*
import kotlin.collections.ArrayList


fun main() {

    val diff = StatSummary()
    val nTrials = 10000
    for (i in 0 until nTrials) diff.add(trial())

    val sDev = 10
    println("Inflation given sDev of $sDev = ${"%.1f".format(diff.mean() * sDev)}")

}

val myRand = Random()

fun trial() : Double {

    val nSamples = 8
    val nUsed = 6

    val values = ArrayList<Double>()
    for (i in 0 until nSamples) values.add(myRand.nextGaussian())

    val sorted = values.sortedDescending()

    // println(sorted)
    val best = sorted.subList(0, nUsed)

    // println(best.size)

    assert(nUsed == best.size, { "Best size (${best.size}) not equal to nUsed ($nUsed)" })

    val diff = StatSummary().add(best).mean() - StatSummary().add(sorted).mean()

    return diff

}

