package geometry.voronoi

import math.Mat2d
import math.Vec2d
import utilities.ElapsedTimer
import utilities.Picker
import utilities.StatSummary
import kotlin.random.Random




fun main() {

    val l2 = Line(Vec2d(10.0, 10.0), Vec2d(20.0, 30.0))
    val l1 = Line(Vec2d(10.0, 50.0), Vec2d(20.0, 71.0))

    val intercept = LineUtil().intersect(l1, l2)
    println("Intercept = $intercept")

    println(LineUtil().intersect(l2, l1))

    // System.exit(0)

    val points = arrayListOf<Vec2d>(
            Vec2d(0.0, 0.0),
            Vec2d(0.0, 1.0),
            Vec2d(1.0, 1.0),
            Vec2d(10.0, 10.0),
            Vec2d(1.0, 0.0),
            Vec2d(0.3, 0.4)
    )

    val useRandomPoints = true

    val scale = 1000.0
    if (useRandomPoints) {
        val nPoints = 20
        points.clear()
        for (i in 0 until nPoints) {
            points.add(Vec2d(scale * Random.nextDouble(), scale * Random.nextDouble()))
        }
    }

    val t = ElapsedTimer()
    val tess = Tesselate()
    val vps = tess.findNeighbours(points)

    val el = t.elapsed()


    val ss = StatSummary("Voronoi Neighbour Stats")

    for (vp in vps) {
        println(vp)

        println()
        ss.add(vp.vn.size)
    }
    println("Made ${tess.innerLoopCount} executions of inner loop")
    println("Tesselation of ${points.size} points")
    println("$el ms elapsed")
    println(ss)
}

class LineUtil {

    fun bisector(a: Vec2d, b: Vec2d) : Line {
        val mid = a + (b-a)*0.5
        // println(mid)
        val perp = (b-a).rotatedBy(Math.PI/2)*0.25
        return Line(mid-perp, mid+perp)
    }

    fun intersect(l1: Line, l2: Line) : Vec2d {
        // form simulataneous equations in Matrix vector form then solve
        val g1 = l1.gradientVector()
        val g2 = l2.gradientVector()

        val mat = Mat2d(g1.x, -g2.x, g1.y, -g2.y)
        val inv = mat.inverse()

        val diffVec = Vec2d(l2.a.x-l1.a.x, l2.a.y - l1.a.y)

        val ab = inv * diffVec
        return l1.a + g1 * ab.x
    }
}

data class BLine(val line:Line, val theta: Double)

data class VoronoiPoint (val point: Vec2d, val vn : List<Vec2d>) {
    val bLines = ArrayList<BLine>()
    val poly = ArrayList<Vec2d>()

    fun calcPoly() {
        bLines.clear()


        for (p in vn) {
            val bis = LineUtil().bisector(point, p)
            val theta = Math.atan2(p.y - point.y, p.x - point.x)
            bLines.add(BLine(bis, theta))
            bLines.sortBy { t -> t.theta }
        }

        // now for each line, find the intersection with the next one
        // easy, surely ...

        poly.clear()
        if (bLines.size < 4) {
            println("Returning as too few lines:  ${bLines.size}")
            return
        }
        for (i in 0 until bLines.size) {

            val p = LineUtil().intersect(bLines[i].line, bLines[(i+1)%bLines.size].line)
            poly.add(p)

        }
    }
}

// could also rewrite this using vals and return
// new versions each time
data class Line (val a: Vec2d, val b: Vec2d) {
    fun gradientVector() = (b-a).normalized
    fun translatedBy(origin: Vec2d) : Line {
        return Line(a-origin, b-origin)
    }
    fun rotatedBy(theta: Double) : Line {
        return Line(a.rotatedBy(theta), b.rotatedBy(theta))
    }
    fun angle() = Math.atan2(gradientVector().y, gradientVector().x)
}

class Tesselate {

    var innerLoopCount = 0
    // naive Voronoi Tesselation Algorithm
    // runs O(n^3)
    fun findNeighbours(points: ArrayList<Vec2d>) : ArrayList<VoronoiPoint> {

        // for each point, find its closest neighbour in each direction
        val vps = ArrayList<VoronoiPoint>()

        for (p in points) {
            //
            val neighbours = HashSet<Vec2d>()
            for (d in points) {
                if (d == p) continue

                // otherwise sort in order of smallest scalar product first
                val dir = d-p

                // find the closest point in each direction
                val picker = Picker<Vec2d>(Picker.MIN_FIRST)
                for (x in points) {
                    // calculate scalar product
                    // only consider of greater than zero
                    val sp = dir.sp(x-p)
                    if (sp > 0) {
                        picker.add( sp, x )
                    }
                    innerLoopCount++
                }
                val best = picker.best
                if (best != null) neighbours.add(best)
            }
            vps.add(VoronoiPoint(p, neighbours.toList()))
        }
        return vps

    }

}
