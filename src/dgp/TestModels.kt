package dgp

import javax.crypto.MacSpi

// playing with some physics equations related to the Dimensional GP paper
// by Keijzer and Babovic from GECCO '99

fun main() {

    val gamma = 9810.0
    fun bernoulli(z: Double, p: Double, v: Double): Double {
        return z + (p / gamma) + ((v * v) / (2 * g))
    }


    val z = 0.0
    for (p in 0..200000 step 50000) {
        for (v in 0..10 step 2) {
            println("p=$p,\tv=$v,\t f = %.3f".format(bernoulli(z, p.toDouble(), v.toDouble())))
        }
    }

    val mass = 1.0

    // start with a height but zero velocity
    // accelerate it each time
    // check whether total energy remains constant

    var h = 100.0
    var v = 0.0

    println()
    println("Constant sum of kinetic and potential energy?")
    // approximate g as 10.0 ms^-2
    g = 10.0
    for (i in 0 until 10) {
        // accelerate our particle using Newton's equations
        // assume a time of t seconds between each update
        // s is the displacement each step
        val t = 1.0
        val s = v * t + 0.5 * -g * t * t
        h += s
        v += -g * t

        println("h = %.1f\t v = %.1f\t E = %.1f  \t k = %.1f".format(h, v, pke(h, v, mass), ke(v, mass)))

    }

}


