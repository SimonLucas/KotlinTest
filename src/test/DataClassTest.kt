package test

data class Person(val name: String, var age: Int)


fun getPeople(): List<Person> {
    return listOf(Person("Alice", 29), Person("Bob", 31))
}

fun main(args: Array<String>) {
    val people = getPeople();
    println(people)
    // here's how we increase the age of each one
    // this is rreally powerful, I love it
    people.map{ it.age++ }
    println(people)
}
