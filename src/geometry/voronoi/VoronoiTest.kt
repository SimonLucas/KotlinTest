package geometry.voronoi

import math.Vec2d
import utilities.JEasyFrame
import kotlin.random.Random

fun main() {
    val points = ArrayList<Vec2d>()


    val nPoints = 0
    val scale = 500.0

    // add in some distant points

    val s2 = scale/2


    // points.add(Vec2d(s2, 0.0))
    points.add(Vec2d(s2, scale))
    // points.add(Vec2d(0.0, s2))
    points.add(Vec2d(scale, s2))

    points.add(Vec2d(s2*0.5, s2*0.8))
    points.add(Vec2d(s2*1.1, s2*1.2))



    for (i in 0 until nPoints) {
        points.add(Vec2d(scale * Random.nextDouble(), scale * Random.nextDouble()))
    }

    val vps = Tesselate().findNeighbours(points)
//    vps.forEach { t -> t.calcPoly() }
//    vps.forEach { t -> println(t.poly) }

    val view = VoronoiGrid()
    view.showBisector = true
    view.fillPoly = true
    view.vps = vps

    JEasyFrame(view, "Voronoi View")

}

