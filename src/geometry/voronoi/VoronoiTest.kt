package geometry.voronoi

import math.Vec2d
import utilities.JEasyFrame
import kotlin.random.Random

fun main() {
    val points = ArrayList<Vec2d>()
    val nPoints = 10
    val scale = 500.0

    for (i in 0 until nPoints) {
        points.add(Vec2d(scale * Random.nextDouble(), scale * Random.nextDouble()))
    }

    val vps = Tesselate().findNeighbours(points)
    vps.forEach { t -> t.calcPoly() }
    vps.forEach { t -> println(t.poly) }

    val view = VoronoiGrid()
    view.vps = vps

    JEasyFrame(view, "Voronoi View")

}

