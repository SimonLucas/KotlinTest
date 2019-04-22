package dgp

import java.util.*

// idea is to develop a simple proof of concept of Dimenionsal Genetic Programming
// this would have a type system that is dynamically based on combinations of
// other types
// For example, dividing displacement-type by time-type produces a new type (displacement/time) which we could call velocity
// Hence, in addition to dynamic type creation, we should also allow aliasing for clarity

// To start with we'll define Types as SI units
// Called FUnit to avoid confusion with the 'Unit' type in Kotlin

//  Declare the fundamental units
enum class FUnit {metre, second, kilogram}



// each base type or dynamically inferred type will include a number of units
// each raised to a power
// create a convenient type alias for this called DynaType for Dynamic Type
// note that the empty map represents a dimenionsal constant or variable
typealias DynaType = HashMap<FUnit, Int>

fun baseType(t: FUnit) : DynaType = hashMapOf(t to  1)

val distanceType = baseType(FUnit.metre)
val timeType = baseType(FUnit.second)
val massType = baseType(FUnit.kilogram)
val speedType = inferDivisionType(distanceType, timeType)
val accelType = inferDivisionType(speedType, timeType)

// a dynamically typed variable consists of a value and a DynaType
// for now we'll have all values be of type Double
// but in future we'll need to expand these to include more types
data class DynaVar(var v: Double, val t: DynaType)

// we'll create a class to hold a node in a graph
// currently we only need a method to update these
interface Node {
    fun update()
    fun value() : DynaVar
}

data class InputNode (val x: DynaVar): Node {

    override fun update() {
        // do nothing
    }
    override fun value() = x
}


typealias BiFunc = (DynaVar, DynaVar) -> DynaVar
typealias UniFunc = (DynaVar) -> DynaVar

// next we need a way to create new function nodes
// a function node is responsible for applying a function to its inputs
// IF possible
interface BiDynaFun {
    // fun check(xt: DynaType, yt: DynaType) : Boolean
    fun opType(xt: DynaType, yt: DynaType) : DynaType?
    fun getFunction(x: DynaVar, y: DynaVar) : BiFunc?
    fun getNode(x: DynaVar, y: DynaVar) : Node?
    // fun op() : Double
}

class Times : BiDynaFun {

    override fun opType(xt: DynaType, yt: DynaType): DynaType? {
        return inferProductType(xt, yt)
    }

    override fun getFunction(x: DynaVar, y: DynaVar): BiFunc? {
        val t = opType(x.t, y.t)
        return if (t == null) null else
            ::times
    }

    override fun getNode(x: DynaVar, y: DynaVar): Node? {
        val f = getFunction(x, y)
        if (f == null) return null
        else
            return BiNode(x, y, f)
    }
    // override fun op() = return x.v * y.v

}

class Addition : BiDynaFun {
    override fun opType(xt: DynaType, yt: DynaType): DynaType? {
        return if (xt.equals(yt)) xt else null
    }
    override fun getFunction(x: DynaVar, y: DynaVar): BiFunc? {
        val t = opType(x.t, y.t)
        return if (t == null) null else
            ::add
    }

    // same code as for Times: a clear indication that something has
    // gone wrong ...
    override fun getNode(x: DynaVar, y: DynaVar): Node? {
        val f = getFunction(x, y)
        if (f == null) return null
        else
            return BiNode(x, y, f)
    }
    // override fun op() = return x.v * y.v

}



data class BiNode(val x: DynaVar,  val y: DynaVar, val f : BiFunc) : Node {
    val op: DynaVar
    // val t: DynaType

    init {
        // t
        op = DynaVar(0.0, DynaType())
        // need a way to check the legality of the function call
        // and the nature of the output

    }

    override fun update() {
        op.v = f(x,y).v
    }

    override fun value() = op

}


// object derivedNames

fun main() {
    val speed1 =  hashMapOf<FUnit,Int>(
            FUnit.metre to 1,
            FUnit.second to -1
    )

    speed1[FUnit.metre] = 2

    var speed2 =  hashMapOf<FUnit,Int>(
            FUnit.metre to 1,
            FUnit.second to -1
    )


    println("Type check: t1 = t2?  ${speed1.equals(speed2)}")



    val d1 = DynaType()
    d1[FUnit.metre] = 1
    val d2 = d1.clone() as DynaType

    val t1 = DynaType()
    t1[FUnit.second] = 1

    // val area = inferProductType(d1, d2)

    // println("Area type: " + area)

    val speed = inferDivisionType(d1, t1)
    println("Speed type: " + speed)

    val width = DynaVar(3.0, d1)
    val height = DynaVar(4.0, d1)
    val depth = DynaVar(5.0, d1)


    val area1 = times(width,  height)
    val area2 = times(width, depth)

    val vol = times(area1, depth)

    // should both work
    println(add(area1, area2))
    println(vol)

    println("Types: ${area1.t} : ${vol.t}")
    println(area1.t.equals(vol.t))

//     require(area1.t.equals(vol.t)) {"Failed this check!"}

    // should cause an assertion error
    // println(add(area1, vol))



}

// given a set of variables as input, this will find all possible functions
// that can be called on them
fun findPossibles() {



}

// val l = b?.length ?: -1

fun inferProductType(x: DynaType, y: DynaType) : DynaType {
    val result = DynaType()
    x.forEach { t, u -> result[t] = u }
    y.forEach { t, u ->
        val cur =result[t] ?: 0
        result[t] = cur + u
    }
    return result
}

fun inferDivisionType(x: DynaType, y: DynaType) : DynaType {
    val result = DynaType()
    x.forEach { t, u -> result[t] = u }
    y.forEach { t, u ->
        val cur =result[t] ?: 0
        result[t] = cur - u
    }
    return result
}

fun times (x: DynaVar, y: DynaVar) : DynaVar {
    return DynaVar(x.v * y.v, inferProductType(x.t, y.t))
}

fun divide (x: DynaVar, y: DynaVar) : DynaVar {
    return DynaVar(x.v / y.v, inferDivisionType(x.t, y.t))
}

fun add (x: DynaVar, y: DynaVar) : DynaVar {
    require(x.t.equals(y.t)) {"DynaTypes must be equal for addition but are not: ${x.t} != ${y.t}"}
    return DynaVar(x.v + y.v, x.t)
}

fun subtract (x: DynaVar, y: DynaVar) : DynaVar {
    require(x.t.equals(y.t)) {"DynaTypes must be equal for subtraction but are not: ${x.t} != ${y.t}"}
    return DynaVar(x.v - y.v, x.t)
}





