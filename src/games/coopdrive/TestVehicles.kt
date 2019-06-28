package games.coopdrive

fun main() {
    val v1 = Vehicle()
    val v2 = v1.copy()
    v1.next(3)
    v1.next(1)

//    println(v1)
//    println(v2)



    val s1 = CoopDriveState()
    (1..100).forEach { s1.next(intArrayOf(1)) }

    val s2 = s1. copy() as CoopDriveState
    (1..100).forEach { s1.next(intArrayOf(1)) }

    println(s1.state.vehicles)
    println(s2.state.vehicles)

}
