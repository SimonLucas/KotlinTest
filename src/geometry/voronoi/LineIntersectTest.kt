package geometry.voronoi

import math.Vec2d

fun main() {
    val l2 = Line(Vec2d(10.0, 10.0), Vec2d(20.0, 30.0))
    val l1 = Line(Vec2d(10.0, 50.0), Vec2d(20.0, 71.0))

    val intercept = LineUtil().intersect(l1, l2)
    println("Intercept = $intercept")

    println(LineUtil().intersect(l2, l2))

}

