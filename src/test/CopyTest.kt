package test

import java.io.Serializable
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import java.io.ObjectOutputStream
import java.io.ObjectInputStream

fun <T : Serializable> deepCopy(obj: T): T {
    // if (obj == null) return null
    val baos = ByteArrayOutputStream()
    val oos  = ObjectOutputStream(baos)
    oos.writeObject(obj)
    oos.close()
    val bais = ByteArrayInputStream(baos.toByteArray())
    val ois  = ObjectInputStream(bais)
    @Suppress("unchecked_cast")
    return ois.readObject() as T
}



data class Player(var name: String, var age: Int) : Serializable
data class MatchPlayers(var p1: Player, var p2: Player) : Serializable



fun main(args: Array<String>) {
    var p1 = Player("Simon", 25)
    var p2 = Player("Anna", 22)
    var m1 = MatchPlayers(p1, p2)
    println(m1)

    var m2 = m1.copy()
    m2.p1.name="Mary"

    var m3 = deepCopy(m1)

    m3.p1.name = "Bob"


    println("m1: \t$m1")
    println("m2: \t$m2")
    println("m3: \t$m3")


}
