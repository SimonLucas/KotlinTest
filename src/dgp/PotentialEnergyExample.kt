package dgp

fun main() {

    // just go through the basics anyold way for now

    val pet = PotentialEnergyTest()

    val fLuts = arrayListOf<BiDynaFun>(Times(), Addition())

    typeChecks(pet.nodes, fLuts)

}

var g = 10.0

fun ke(v: Double, mass:Double) :Double {
    return 0.5 * mass * v * v
}

fun pe(h: Double, mass: Double): Double {
    // the idea is that these should be conserved
    return mass * g * h
}

fun pke(h: Double,  mass: Double, v: Double): Double {
    // the idea is that these should be conserved
    return pe(h, mass) + ke(v, mass)
}



class PotentialEnergyTest {

    // val acc

    val m = InputNode(DynaVar(0.0, baseType(FUnit.kilogram)))
    val h = InputNode(DynaVar(0.0, baseType(FUnit.metre)))
    val g = InputNode(DynaVar(10.0, accelType))
    val nodes : ArrayList<Node> = arrayListOf(m, g, h)

    init {
        // create the other nodes also
        // val mh = Times().
    }

    fun op(im: Double, ih: Double) {
        // set up the inputs
        m.x.v = im
        h.x.v = ih

        // update all the nodes
        nodes.forEach { el -> el.update() }

    }


}

fun typeChecks(nodes: ArrayList<Node>, fLuts: ArrayList<BiDynaFun>) {
    val biFuncs = ArrayList<BiFunc>()
    for (x in nodes) {
        for (y in nodes) {
            println("Type checking $x paired with $y")
            for (fl in fLuts) {
                val fn = fl.getFunction(x.value(), y.value())
                if (fn != null) {
                    println("$fl -> $fn")
                    biFuncs.add(fn)
                }
            }
            println()
        }
    }
    println("Found ${biFuncs.size} valid binary functions")
}



fun potentialEnergyNet() : ArrayList<Node> {
    val m = InputNode(DynaVar(0.0, baseType(FUnit.kilogram)))
    val h = InputNode(DynaVar(0.0, baseType(FUnit.metre)))
    val g = InputNode(DynaVar(10.0, DynaType()))
    val nodes : ArrayList<Node> = arrayListOf(m, g, h)
    // in addition we also need a way to call it
    return nodes
}
