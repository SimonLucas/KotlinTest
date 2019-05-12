package test

import java.util.*

fun main(args: Array<String>) {

    fun length(s: String) = s.length

    // println(::length)

    val p1 = ::isEven
    val p2 = ::isOdd

    val rand = Random()

    var p = if (rand.nextBoolean()) p1 else p2

    println(p)

    val oddLength = compose(p, ::length)
    val strings = listOf("a", "ab", "abc")
    println(strings.filter(oddLength))

}

fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C {
    return { x -> f(g(x)) }
}

fun <A, B, C> combine(f: (B) -> C, g: (A) -> B): (A) -> C {
    return { x -> f(g(x)) }
}

fun isOdd(x: Int) = x % 2 != 0
fun isEven(x: Int) = !isOdd(x)
// fun isOdd(s: String) = s == "brillig" || s == "slithy" || s == "tove"

