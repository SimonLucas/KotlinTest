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
            // Vec2d(0.0, 1.0),
            // Vec2d(1.0, 1.0),
            Vec2d(10.0, 10.0),
            // Vec2d(1.0, 0.40),
            Vec2d(2.0, 8.0)
    )

    val useRandomPoints = false

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

    fun intersect(l1: Line, l2: Line) : Vec2d? {
        // form simulataneous equations in Matrix vector form then solve
        val g1 = l1.gradientVector()
        val g2 = l2.gradientVector()

        val mat = Mat2d(g1.x, -g2.x, g1.y, -g2.y)
        val inv = mat.inverse() ?: return null

        val diffVec = Vec2d(l2.a.x-l1.a.x, l2.a.y - l1.a.y)
        val ab = inv * diffVec
        return l1.a + g1 * ab.x
    }

    fun boundedIntersect(l1: Line, l2: Line) : Vec2d? {
        // form simulataneous equations in Matrix vector form then solve
        val ip = intersect(l1, l2)
        if (ip == null) return null

        // now check whether the intersection point is between both line segments

        if (ip.distanceTo(l1.a) < l1.length() && ip.distanceTo(l1.b) < l1.length() &&
                ip.distanceTo(l2.a) < l2.length() && ip.distanceTo(l2.b) < l2.length())
            return ip
        else
            return null
    }
}

data class Poly (val points: ArrayList<Vec2d>)

class PolyUtil {



    fun split(poly: ArrayList<Vec2d>, split: Line) : ArrayList<Poly>? {
        // find the closest intersecting line with the midpoint of the intersec
        // similar to finding the closest intersection points of a line with a polygon
        // but this does the split into two smaller polygons

        val lines = ArrayList<Line>()

        for (i in 0 until poly.size) {
            lines.add(Line(poly[i], poly[(1+i) % poly.size]))
        }

        val mid = (split.a + split.b) * 0.5
        val dir = (split.a - split.b).normalized

        // use this to keep track of the points at which the poly should be split
        data class SplitPoint(val p: Vec2d, val ix: Int)

        val towardsA = Picker<SplitPoint>(Picker.MIN_FIRST)
        val towardsB = Picker<SplitPoint>(Picker.MIN_FIRST)


        // find all the intersection point distances
        for (i in 0 until lines.size) {
            val line = lines[i]
            val x = LineUtil().intersect(line, split)
            if (x == null) continue
            // otherwise add the intersection point, and then check the scalar product
            // along the direction of the line

            val sp = dir.sp(x-mid)

            // now what next?
            if (sp > 0) {
                towardsA.add(sp, SplitPoint(x, i))
            } else {
                // negative scalar product so negate it before passing to the picker
                towardsB.add(-sp, SplitPoint(x, i))
            }
        }
        // the two intersection points are the first in each list

        val a  = towardsA.best
        val b = towardsB.best
        if (a != null && b != null) {
            val sp1 = if (a.ix < b.ix)  a else b
            val sp2 = if (a.ix < b.ix)  b else a
            // to split the poly, make two new ones, then add the points

            val poly1 = ArrayList<Vec2d>()
            val poly2 = ArrayList<Vec2d>()

            for (i in 0 .. sp1.ix) {
                poly1.add(poly[i])
            }
            poly1.add(sp1.p)
            poly1.add(sp2.p)
            for (i in sp2.ix until  poly.size) {
                poly1.add(poly[i % poly.size])
            }

            poly2.add(sp1.p)
            for (i in sp1.ix +1 .. sp2.ix  ) {
                poly2.add(poly[i % poly.size])
            }
            poly2.add(sp2.p)

            return arrayListOf<Poly>(Poly(poly1), (Poly(poly2)))

        } else {
            return null
        }
    }
    fun intersect(poly: ArrayList<Vec2d>, split: Line) : Line? {
        // find the closest intersecting line with the midpoint of the intersec
        val lines = ArrayList<Line>()

        for (i in 0 until poly.size -1 ) {
            lines.add(Line(poly[i], poly[1+i]))
        }
        val mid = (split.a + split.b) * 0.5
        val dir = (split.a - split.b).normalized

        val towardsA = Picker<Vec2d>(Picker.MIN_FIRST)
        val towardsB = Picker<Vec2d>(Picker.MIN_FIRST)

        // find all the intersection point distances
        for (line in lines) {
            val x = LineUtil().intersect(line, split)
            if (x == null) continue
            // otherwise add the intersection point, and then check the scalar product
            // along the direction of the line

            val sp = dir.sp(x-mid)

            // now what next?
            if (sp > 0) {
                towardsA.add(sp, x)
            } else {
                // negative scalar product so negate it before passing to the picker
                towardsB.add(-sp, x)
            }

        }
        // the two intersection points are the first in each list

        val a  = towardsA.best
        val b = towardsB.best
        if (a != null && b != null) {
            return Line(a, b)
        } else
            return null
    }
}

data class BLine(val line:Line, val theta: Double, val midPoint: Vec2d = Vec2d())

data class VoronoiPoint (val point: Vec2d, val vn : List<Vec2d>) {
    val bLines = ArrayList<BLine>()
    val poly = ArrayList<Vec2d>()

    // this builds a "Voronoi Polygon" around this Voronoi point
    // a Voronoi point is defined by a central point and it's set of
    // Voronoi Neighbour points

    // The algorithm also considers a set of bounding lines (e.g. that could
    // form a bounding rectangle)

    // It iterates over the set of neighbours to find the set of bisecting lines
    // Each bisecting line has a centre point, where it intersects the line
    // joining the VN centre with the VN point

    fun calcPoly(bounds: ArrayList<Line>) {
        bLines.clear()

        for (p in vn) {
            val bis = LineUtil().bisector(point, p)
            val theta = Math.atan2(p.y - point.y, p.x - point.x)
            bLines.add( BLine(bis, theta, (point + p) * 0.5) )
        }
        poly.clear()
        // now find the bisection points for each line, in each direction

        for (bl in bLines) {

        }

        if (bLines.size < 4) {
            println("Returning as too few lines:  ${bLines.size}")
            return
        }

        for (i in 0 until bLines.size) {

            val p = LineUtil().intersect(bLines[i].line, bLines[(i+1)%bLines.size].line)
            // if the lines are parallel there will be no intersection...
            if (p == null) continue
            poly.add(p)

        }
    }





    fun calcPolyBuggy() {
        // logic was wrong in this, and also it had
        // no way for lines to intersect with a bounding set of lines
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
            if (p == null) return
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
    fun rotatedAroundMidPoint(theta: Double) : Line {
        val mid = (a + b) * 0.5
        val ar = (a - mid).rotatedBy(theta)
        val br = (b - mid).rotatedBy(theta)
        return Line(ar+mid, br+ mid)
    }
    fun angle() = Math.atan2(gradientVector().y, gradientVector().x)
    fun length() = a.distanceTo(b)
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
                if (d != p) {

                    // otherwise sort in order of smallest scalar product first
                    val dir = d - p

                    // find the closest point in each direction
                    val picker = Picker<Vec2d>(Picker.MIN_FIRST)
                    for (x in points) {
                        // calculate scalar product
                        // only consider of greater than zero
                        if (true || x!=p && x!=d) {
                            val sp = dir.sp(x - p)
                            if (sp > 0) {
                                picker.add(sp, x)
                            }
                        }
                        innerLoopCount++
                    }
                    val best = picker.best
                    if (best != null) neighbours.add(best)
                }
            }
            vps.add(VoronoiPoint(p, neighbours.toList()))
        }
        return vps
    }
}
