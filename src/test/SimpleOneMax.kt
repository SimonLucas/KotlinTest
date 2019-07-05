package test

import java.util.*
import kotlin.random.Random

fun main() {
    val oneMax = SimpleOneMax(mutationRate = 0.2, m = 21, n = 100)
    oneMax.run(40000)
}

class SimpleOneMax(val mutationRate: Double = 0.05, val n: Int = 20, val m:Int = 2) {
    var current = IntArray(n) { i -> Random.nextInt(m) }
    fun run(nEvals: Int) {
        for (i in 0 until nEvals) {
            val mut = mutate(current)
            // note: would normally test ">="
            if (mut.sum() > current.sum()) {
                current = mut
                println("$i\t ${current.sum()}\t ${Arrays.toString(current)}")
                if (mut.sum() == n) {
                    println("Optimum after $i iterations")
                    return
                }
            }
        }
    }

    fun mutate(x: IntArray): IntArray {
        val mut = IntArray(n)
        for (i in 0 until n) mut[i] =
                if (Random.nextDouble() < mutationRate) Random.nextInt(m) else x[i]
        return mut
    }
}

