package test

import java.util.*

data class Sides(var attack: Int, var defence: Int)

val rand = Random();
fun dice() : Int = 1 + rand.nextInt(6)

fun doBattle(input: Sides) : Boolean {
    if(input.attack <2 || input.defence<1) return false;

    val nAttack = Math.min(3, input.attack-1)
    val nDefend = Math.min(2, input.defence)

    var attackRoll = Array(nAttack, {x -> dice()}).sortedBy{-it}
    var defenceRoll = Array(nDefend, {x -> dice()}).sortedBy{-it}

    println("Rolling:")
    println("Attack: \t " + attackRoll)
    println("Defence:\t " + defenceRoll)
    println()

    for (i in 0 until Math.min(defenceRoll.size, attackRoll.size)) {
        if (attackRoll[i] > defenceRoll[i])
            input.defence--
        else
            input.attack--
    }

    return true
}

fun war(input: Sides) : Sides {
    while (doBattle(input)) {
        println(input)
    }
    return input
}

fun main(args: Array<String>) {

//    print("Attack? ")
//    val def = readLine().toInt()
//    print("Defence? ")

    val match = Sides(5, 3)
    war(match)
    println("Result is: " + match)


}