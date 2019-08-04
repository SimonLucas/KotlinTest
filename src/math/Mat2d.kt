package math


fun m(a: Double, b: Double, c: Double, d: Double) = Mat2d(a,b,c,d)

data class Mat2d(val a: Double = 0.0, val b: Double = 0.0, val c: Double = 0.0, val d: Double = 0.0) {
    operator fun plus(m: Mat2d) = m(a + m.a, b + m.b, c+m.c, d + m.d)
    operator fun unaryMinus() = m(-a, -b, -c, -d)
    operator fun minus(m: Mat2d) = m(a-m.a, b-m.b, c-m.c, d-m.d)
    operator fun times(m: Mat2d) = m(
            a*m.a + b *m.c,
            a*m.b + b*m.d,
            c* m.a + d*m.c,
            c* m.b + d*m.d
    )
    operator fun times(coef: Double) = m(
            a*coef, b*coef, c*coef, d*coef
    )

    operator fun times(v: Vec2d) = Vec2d(
            a*v.x + b *v.y, c*v.x + d*v.y
    )

    fun inverse() : Mat2d? {
        val denom = (a*d - b*c)
        if (denom == 0.0) return null
        val det = 1 / denom
        return m(det*d, -det*b, -det*c, det*a)
    }
}

