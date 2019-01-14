package test

data class Complex(val re: Double, val im: Double)

val Double.j: Complex
    get() = Complex(0.0, this)

operator fun Double.plus(c: Complex): Complex {
    return Complex(this + c.re, c.im)
}

operator fun Complex.plus(c : Complex) : Complex {
    return Complex(this.re + c.re, this.im + c.im)
}

fun main(args: Array<String>) {
    val res = Complex(4.0, 5.0)
    val r2 = 3.0 + 5.0.j


    println(res.toString() + " : " + r2)

    val r3 = res + r2
    println(r3)

    // println(r3.javaClass.)

}

