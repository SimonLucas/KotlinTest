package games.simplegridgame

import games.gridgame.gameOfLife

fun main() {
    var totErrors = 0

    for (i in 0 until 512) {
        val a = ArrayList<Int>()
        i.toString(2).forEach { a.add(it.toInt() - '0'.toInt()) }
        // now append enough leading zeros
        while (a.size < 9) a.add(0, 0)
        val centre = a[4]
        val sum = a.sum() - centre
        // val x = gameOfLife(centre, sum)
        val t = 4
        val x = if (a.sum() > t) 1 else 0
        println("${a}\t $x")


    }

}
