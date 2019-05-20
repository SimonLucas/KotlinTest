package games.eventqueuegame

import math.Vec2d

fun main() {
    // test city creation and copying

    val c1 = City(Vec2d(0.0, 0.0))
    val c2 = City(Vec2d(10.0, 10.0))



    val cities = ArrayList<City>()
    cities.add(c1)
    val citiesCopy = ArrayList<City>()
    cities.forEach { el -> citiesCopy.add(el.copy()) }

    c1.pop = 50.0
    println("Original: ${cities}")
    println("Copy:     ${citiesCopy}")

    val w1 = World(cities)

    val w2 = w1.copy()

    println(w1.equals(w2))

    c1.pop = 10.0

    println(w1.equals(w2))

    println("W2")
    println(w2)

    println("Hashcode 1: " + w1.hashCode())

    c1.pop = 100.0
    println("Hashcode 2: " + w1.hashCode())


    // cities = City(Vec2d(30.0, 30.0))


}