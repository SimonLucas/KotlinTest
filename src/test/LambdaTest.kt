package test

fun main(args: Array<String>) {
    val fruits = listOf("banana", "avocado", "apple", "kiwifruit")
    // println(fruits.javaClass)
    // println(fruits[0].javaClass)

    fruits
            .filter { it.startsWith("a") }
            .sortedBy { it }
            // .map { it.toUpperCase() }
            .forEach { println(it) }

}
