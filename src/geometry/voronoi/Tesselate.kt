package geometry.voronoi

import math.Vec2d
import utilities.ElapsedTimer
import utilities.Picker
import utilities.StatSummary
import kotlin.random.Random


fun main() {

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
        val nPoints = 1000
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


data class VoronoiPoint (val point: Vec2d, val vn : List<Vec2d>)

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
