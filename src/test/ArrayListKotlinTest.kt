package test

import kotlin.random.Random

fun main() {
    val list = ArrayList<Int>()
    repeat(10) {
        list.add(it)
    }
    val random = Random(2)
    list.shuffle(random)
    println(list)

}
