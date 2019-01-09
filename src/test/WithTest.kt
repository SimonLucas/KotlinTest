package test

import games.caveswing.CaveSwingParams

fun main(args: Array<String>) {
    with(StringBuilder()) {
        append("content: ")
        append(javaClass.canonicalName)
    }

    val params = CaveSwingParams()

    with (params) {
        println("Gravity: " + gravity)
    }

    println(StringBuilder().javaClass.canonicalName)

}

