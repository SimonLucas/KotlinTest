package test

fun main() {
    // sum all the numbers in a range

    val intRange = 1 .. 10

    println( (1..10).sum() )

    // sum the squares
    println( (1 .. 10 ).map{ t -> t*t }.sum() )

    //
    fun sqr(x:Int) = x*x

    val f = ::sqr
    // sum the squares
    println( intRange.map { f(it) }.sum() )

    val colors = listOf("red", "brown", "grey")
    val animals = listOf("fox", "bear", "wolf")
    println(colors zip animals)

    val twoAnimals = listOf("fox", "bear")
    println(colors.zip(twoAnimals))
}
