package math

fun main() {
    val A = m(1.0, 2.0, 3.0 ,4.0)

    val B = A+A
    println(A)
    println(B)

    val inv = A.inverse()

    if (inv!= null) {

        println(inv)

        println(A * inv)
        println(A * 2.0)
    } else {
        "No inverse"
    }
}


