package test

import kotlin.random.Random

data class Card (val suit: Int, val faceValue: Int)

data class Action(val cardPut1: Card, val cardPut2: Card)

fun main() {

    val rand = Random

    val actionValues = HashMap<Action,Double>()

    val c1 = Card(0, 0)
    val c2 = Card(0, 1)
    val c3 = Card(1, 4)

    val a1 = Action(c1, c2)
    val a2 = Action(c1, c2)
    val a3 = Action(c1, c3)

    actionValues.put(a1, 1.0)
    actionValues.put(a2, 2.0)
    actionValues.put(a3, 3.0)


    println(actionValues)





}