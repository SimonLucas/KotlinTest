package dgp

import kotlin.reflect.KFunction


typealias BinaryMaker = (SimpleNode, SimpleNode ) -> SimpleNode?
typealias UnaryMaker = (SimpleNode) -> SimpleNode?

fun makeTimes (x: SimpleNode, y: SimpleNode ) :SimpleNode? {
    return SimpleTimes(x,y)
}

fun makeDivide (x: SimpleNode, y: SimpleNode ) :SimpleNode? {
    return SimpleDivide(x,y)
}

fun makePlus(x: SimpleNode, y: SimpleNode ) :SimpleNode? {
    if (x.type().equals(y.type())) {
        return SimplePlus(x, y)
    } else return null
}

fun makeMinus (x: SimpleNode, y: SimpleNode ) :SimpleNode? {
    if (x.type().equals(y.type())) {
        return SimpleMinus(x, y)
    } else return null
}

fun main() {
    println(::makeTimes is BinaryMaker)
    val m = SimpleInputNode(massType)
    val g = SimpleInputNode(accelType)
    val h = SimpleInputNode(distanceType)

    var dimensionless = false
    if (dimensionless) {
        m.t = DynaType()
        g.t = DynaType()
        h.t = DynaType()
    }

    val nodes = arrayListOf<SimpleNode>(m, g, h)

    val makers = arrayListOf<BinaryMaker>(::makePlus, ::makeMinus, ::makeTimes, ::makeDivide)

    val l1 = produceNodes(nodes, makers)
    println(l1)
    println(l1.size)

    nodes.addAll(l1)

    val l2 = produceNodes(nodes, makers)
    println(l2.size)

    m.v = 2.0
    g.v = 10.0
    h.v = 5.0

    println(m.type())
    println(g.type())

    var mg = SimpleTimes(m, g)

    // may need the reflect package for this to work ...
    // val simpleTimesConsBroken : KFunction2<SimpleNode,SimpleNode,SimpleTimes> = ::SimpleTimes
    val simpleTimesCons = ::SimpleTimes
    mg = simpleTimesCons(g, h)

    mg.update()
    println(mg.type())
    println(mg.value())
}

fun produceNodes(nodes: ArrayList<SimpleNode>, makers: ArrayList<BinaryMaker>) : ArrayList<SimpleNode> {
    val productions = ArrayList<SimpleNode>()
    for (x in nodes) {
        for (y in nodes) {
            // now iterate over all the makers
            for (m in makers) {
                val trial = m(x, y)
                if (trial != null) productions += trial
            }
        }
    }
    return productions
}

open abstract class SimpleNode(var t: DynaType) {
    var v : Double = 0.0
    // var t = DynaType
    open abstract fun update()
    fun value() = v
    fun type() = t
}

open abstract class BinaryNode (t: DynaType) : SimpleNode(t) {
    open abstract fun check(x: DynaType, y: DynaType) : Boolean
}

class SimpleInputNode (t: DynaType): SimpleNode(t) {
    override fun update() {
        // do nothing
    }
}

class SimpleTimes(val x: SimpleNode, val y: SimpleNode) : BinaryNode(DynaType()) {
    init {
        t = inferProductType(x.type(), y.type())
    }
    override fun update() {
        v = x.value() * y.value()
    }
    override fun check(x: DynaType, y: DynaType)  = true
}

class SimpleDivide(val x: SimpleNode, val y: SimpleNode) : SimpleNode(DynaType()) {
    init {
        t = inferProductType(x.type(), y.type())
    }
    override fun update() {
        v = x.value() * y.value()
    }
}

class SimplePlus(val x: SimpleNode, val y: SimpleNode) : SimpleNode(DynaType()) {
    init {
        require(x.type().equals(y.type()))
        t = x.type()
    }
    override fun update() {
        v = x.value() + y.value()
    }
}

class SimpleMinus(val x: SimpleNode, val y: SimpleNode) : SimpleNode(DynaType()) {
    init {
        require(x.type().equals(y.type()))
        t = x.type()
    }
    override fun update() {
        v = x.value() - y.value()
    }
}

