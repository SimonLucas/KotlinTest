package test

import games.citywars.showCityOutline


fun main(args: Array<String>) {
    var tot = 0
    val list = ArrayList<Int>()
    for (i in 1 .. 10) {
        // println(i)
        tot += i
        list.add(i)
    }
    println(tot)
    println(list)
    println(list.sum())



}

