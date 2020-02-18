package test

fun main() {
    val (name, age) = test1()
    println(name + age)
    println(test2())
}

// recommended Kotlin way is to use a named data class
// but could also use Pair or Triple classes
data class PersonAge (val name: String, val age: Int)
fun test1(): Person {
    return Person("Edward", 18)
}

// this would also work
fun test2(): Pair<String,Int> {
    return Pair("Edward", 18)
}


