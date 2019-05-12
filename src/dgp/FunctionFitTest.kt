package dgp

import utilities.ElapsedTimer
import utilities.StatSummary
import kotlin.random.Random

fun main() {


    // testRandomNets()

    testRandomGrowth()

//    val randNet = RandNetPKE()
//    randNet.addNodes(10)
//    testFunction(1e6.toInt(), pke)
//    testFunction(1e6.toInt(), randNet)
}

fun testRandomGrowth() {
    val tester = Tester(1000)

    var pke = TestPKE()
    println("True function: %.1f".format(tester.test(pke)))

    val t = ElapsedTimer()
    val nTrials = 1000
    val net = RandNetPKE()
    // net.setDimensionless()
    val ss = StatSummary("Ave err")
    for (i in 1..nTrials) {
        net.addNodes(1)
        val err = tester.test(net)
//        println("%d:  \trand net(%d):\t %.1f".format(i, net.nodes.size, err))
//        println(net.report())
        ss.add(err)
    }
    println(ss)
    println(t)

}

fun testRandomNets() {
    val tester = Tester(1000)

    var pke = TestPKE()
    println("True function: %.1f".format(tester.test(pke)))

    val t = ElapsedTimer()
    val nTrials = 100
    for (i in 1..nTrials) {
        val net = RandNetPKE()
        net.addNodes(10)
        println("%d:  \trand net(%d):\t %.1f".format(i, net.nodes.size, tester.test(net)))
    }
    println(t)

}

data class Pattern(val ip: ArrayList<Double>, val op: Double)

class Tester(val n: Int) {
    val patterns = ArrayList<Pattern>()
    val min = -10
    val max = 10

    init {
        for (i in 0 until n) {
            val ip = ArrayList<Double>()
            for (j in 0 until 3)
                ip.add((Random.nextInt(1 + (max - min)) + min).toDouble())
            val op = pke(ip[0], ip[1], ip[2])
            patterns.add(Pattern(ip, op))
        }
        println("Made patterns: ")
        println(patterns.size)
    }

    fun test(pke: PKE): Double {
        var tot = 0.0
        for (p in patterns) {
            val err = Math.abs(p.op - pke.f(p.ip[0], p.ip[1], p.ip[2]))
            tot += err
        }
        return tot / n
    }
}

// write a basic kinetic energy test to begin with

interface PKE {
    fun f(h: Double, m: Double, v: Double): Double
}

fun testFunction(n: Int, net: PKE): Double {

    // to begin with just start with some values

    val values = ArrayList<Int>()
    (-10..10).forEach { i -> values.add(i) }

    // pick values at random to try out

    val rand = Random


    val t = ElapsedTimer()

    var totErr = 0.0

    for (i in 0 until n) {

        val mass = values[rand.nextInt(values.size)].toDouble()
        val h = values[rand.nextInt(values.size)].toDouble()
        val v = values[rand.nextInt(values.size)].toDouble()
        val correct = pke(h, mass, v)
        // val estimate = pke(h, mass, v)
        val estimate = net.f(h, mass, v)
        // now see what our test function produces

        val err = Math.abs(correct - estimate)
        totErr += err

        // println("%d\t %.1f\t %.1f\t %.1f".format(i, correct, estimate, err))

    }
    println(totErr / n)
    val rate = n / (t.elapsed().toDouble() * 1000)

    println(t)
    println(" %.1f million net updates per second".format(rate))
    return totErr / n
}

class TestPKE : PKE {
    val m = SimpleInputNode(massType)
    val g = SimpleInputNode(accelType)
    val h = SimpleInputNode(distanceType)
    val v = SimpleInputNode(speedType)

    val nodes = ArrayList<SimpleNode>()
    val energy: SimpleNode

    init {
        // set g
        g.v = 10.0
        // make the calculation network
        val mg = SimpleTimes(m, g)
        val pe = SimpleTimes(mg, h)

        val vv = SimpleTimes(v, v)
        val mvv = SimpleTimes(m, vv)
        val half = SimpleInputNode(DynaType())
        half.v = 0.5
        val ke = SimpleTimes(half, mvv)

        energy = SimplePlus(pe, ke)

        val temp = arrayListOf<SimpleNode>(mg, pe, vv, mvv, ke, energy)
        nodes.addAll(temp)

    }

    override fun f(height: Double, mass: Double, velocity: Double): Double {
        m.v = mass; h.v = height; v.v = velocity;
        nodes.forEach { t -> t.update() }
        return energy.value()
    }
}

class RandNetPKE : PKE {
    // todo : Make this a randomly constructed but valid network ...
    val m = SimpleInputNode(massType)
    val g = SimpleInputNode(accelType)
    val h = SimpleInputNode(distanceType)
    val v = SimpleInputNode(speedType)



    val makers = arrayListOf<BinaryMaker>(::makePlus, ::makeMinus, ::makeTimes, ::makeDivide)


    // need to add the inout nodes in since now we're growing them...
    var nodes = ArrayList<SimpleNode>()

    init {
        // set g
        g.v = 10.0

        // todo: add the stuff in
        val half = SimpleInputNode(DynaType())
        half.v = 0.5

        nodes.addAll(arrayListOf(m, g, h, v, half))

    }

    fun setDimensionless() {
        for (node in nodes) node.t = DynaType()
    }

    fun addNodes(n: Int, limit: Int = 5, attemptRatio: Int = 10) {
        // add a number of random nodes
        // but use a limited number of attempts

        val initial = nodes.size
        for (i in 0 until n * attemptRatio) {
            val node = randomBinaryNode(nodes, makers)
            if (node != null) {
                // println("Node: " + node)
                if (absPowers(node.type()) <= limit)
                    nodes.add(node)
            } else {
                // println("Failed to make a node")
            }
            if (nodes.size >= (initial + n)) break
        }
    }

    fun absPowers(t: DynaType): Int {
        return t.values.sumBy { t -> Math.abs(t) }
    }

    override fun f(height: Double, mass: Double, velocity: Double): Double {
        m.v = mass; h.v = height; v.v = velocity;
        nodes.forEach { t -> t.update() }
        return nodes[nodes.size - 1].value()
    }

    fun report() : String {
        val op = nodes[nodes.size-1]
        return "abs power = ${absPowers(op.type())}\t : ${op.type()}"
    }
}


// class BinaryNodeMaker(val absPowersLimit: Int = 6)

fun randomBinaryNode(nodes: ArrayList<SimpleNode>, makers: ArrayList<BinaryMaker>): SimpleNode? {
    val x = nodes[Random.nextInt(nodes.size)]
    val y = nodes[Random.nextInt(nodes.size)]
    val maker = makers[Random.nextInt(makers.size)]
    val node = maker
    return maker(x, y)
}

