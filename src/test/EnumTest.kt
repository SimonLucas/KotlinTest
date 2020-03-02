package test

import java.util.*

enum class MyColor(val rgb: Int) {
    RED(0xFF0000),
    GREEN(0x00FF00),
    BLUE(0x0000FF)
}

enum class Names{Simon, Jo, Steph}

fun main() {
    println(Arrays.toString(MyColor.values()))
    println(MyColor.BLUE.rgb)

    for (p in MyColor.values()) {
        println(p.name)
    }

    // for (n in Names.values().)

}