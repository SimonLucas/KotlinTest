package test

import java.util.*
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

fun main(args: Array<String>) {
    val init = 0;
    // fun init =
    val range = 6;
    val rand = Random()
    fun randInit (i:Int) = rand.nextInt(range)
    // println(::randInit)

    for (i in 0 until 10) println(randInit(i))

    // val a = arrayOf(Array)

    val a = Array(10, {x -> rand.nextInt(range)})
    println(Arrays.toString(a))

}
