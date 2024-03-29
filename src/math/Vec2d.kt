package math


import kotlin.math.*

// Vec2d from trykotlin.org (Creatures.kt example)

fun v(x: Double, y: Double) = Vec2d(x, y)

data class Vec2d(val x: Double = 0.0, val y: Double = 0.0) {
    operator fun plus(v: Vec2d) = v(x + v.x, y + v.y)
    operator fun unaryMinus() = v(-x, -y)
    operator fun minus(v: Vec2d) = v(x - v.x, y - v.y)
    operator fun times(koef: Double) = v(x * koef, y * koef)
    infix fun distanceTo(v: Vec2d) = sqrt((this - v).sqr)
    infix fun gridDistanceTo(v: Vec2d) = abs(x-v.x) + abs(y - v.y)
    fun rotatedBy(theta: Double): Vec2d {
        val sin = sin(theta)
        val cos = cos(theta)
        return v(x * cos - y * sin, x * sin + y * cos)
    }

    fun isInRect(topLeft: Vec2d, size: Vec2d) = (x >= topLeft.x) && (x <= topLeft.x + size.x) &&
            (y >= topLeft.y) && (y <= topLeft.y + size.y)

    val sqr: Double
        get() = x * x + y * y
    val normalized: Vec2d
        get() = this * (1.0 / sqrt(sqr))
    // scalar product
    fun sp(a: Vec2d, b: Vec2d) : Double = a.x * b.x + a.y * b.y
    fun sp(a: Vec2d) : Double = x * a.x + y * a.y
}

