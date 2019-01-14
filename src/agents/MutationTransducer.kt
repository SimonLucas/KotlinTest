package agents

import java.util.*

fun main(args: Array<String>) {
    val range = 5
    val n = 20
    val mt = MutationTransducer()

    val input = mt.repSeq(n, 0)
    val output = mt.mutate(input, range)

    input.forEach { print(it) }
    println()
    output.forEach { print(it) }
    println()

}

data class MutationTransducer (var mutProb: Double = 0.2, var repeatProb: Double = 0.5){

    val random = Random()

    fun mutate(input: IntArray, range: Int) : IntArray {
        val output = IntArray(input.size)
        // now copy across the input

        for (i in 0 until input.size) {

            // todo actually implement these
            val p = random.nextDouble()

            if (p < mutProb) {
                // mutate

            } else if (p < mutProb + repeatProb)
            else {
                // faithful copy

            }

        }

        return output

    }

    fun randSeq(n: Int, range: Int) : IntArray {
        return IntArray(n, {x -> random.nextInt(range)})
    }
    fun repSeq(n: Int, v: Int) : IntArray {
        return IntArray(n, {x -> v})
    }
}