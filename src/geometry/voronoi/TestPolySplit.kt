package geometry.voronoi

import math.Vec2d
import utilities.JEasyFrame
import views.Drawable
import views.EasyDraw
import views.PolyDraw
import java.awt.Color

fun main() {
    val w = 600.0
    val h = 350.0
    val bounds = arrayListOf<Vec2d>(
            Vec2d(0.0, 0.0),
            Vec2d(w, 0.0),
            Vec2d(w, h),
            Vec2d(0.0, h)
    )

    var split = Line(Vec2d(w/2, h/2), Vec2d(w/2, 0.0))

    println("Bounds: $bounds")
    println(PolyUtil().intersect(bounds, split))

    var drawList = getPolys(bounds, split)
    val easyDraw = EasyDraw(600, 300)
    easyDraw.drawable = drawList
    JEasyFrame(easyDraw, "Split Poly Test")

    var i = 0
    while (true) {
        easyDraw.drawable = getPolys(bounds, split)
        split = split.rotatedAroundMidPoint(Math.PI / 180)
        easyDraw.repaint()
        Thread.sleep(50)
        // println(i++)
    }
}

fun getPolys(bounds: ArrayList<Vec2d>, split: Line) : ArrayList<Drawable> {
    val polys = PolyUtil().split(bounds, split)
    val drawList = ArrayList<Drawable>()
    val cols = arrayListOf<Color>(
            Color.getHSBColor(0.2f, 1f, 1f),
            Color.getHSBColor(0.8f, 1f, 1f)
            )
    if (polys != null) {
        drawList.add(PolyDraw(polys[0].points, cols[0], null))
        drawList.add(PolyDraw(polys[1].points, cols[1], null))
//        println(polys[0])
//        println(polys[1])
//        println()
    } else {
        println("Null polys")
    }
    return drawList
}

