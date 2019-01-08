package games.caveswing

import math.Vector2d

fun main(args: Array<String>) {
    val p1 = CaveSwingParams()
    p1.maxTicks = 99
    // val p2 = p1.copy() // Params()
    val p2 = p1.deepCopy()
    p2.gravity.x = 55.0
    p2.hooke = 1.0
    // check that maxTicks (non-default value) is correctly copied in to p2,
    // and that the updated gravity value in p2 is not changed in p1
    println(p1)
    println(p2)
    println()

    println("Easy tests")
    data class User(var name: String = "Bob", val age: Int = 0, val s: Vector2d = Vector2d(0.0, 0.0)) {
        fun deepCopy() : User {
            return copy(s = s.copy())
        }
    }
    val jack = User(name = "Jack", age = 1, s = Vector2d(1.0,2.0))
    // jack.name = "Jill"
    val olderJack = jack.deepCopy()
    olderJack.s.x = 99.0

    println("Should be: User(name=Jack, age=1, s=1.0 : 2.0)")
    println(jack)


    println("Should be: User(name=Jack, age=1, s=99.0 : 2.0)")
    println(olderJack)

}
