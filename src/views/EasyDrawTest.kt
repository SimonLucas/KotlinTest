package views

import math.Vec2d
import utilities.JEasyFrame
import java.awt.Color
import kotlin.random.Random

fun main() {
    val w = 500.0
    val h = 250.0
    val np = 3
    val rand = Random
    fun randPoly() : ArrayList<Vec2d> {
        val poly = ArrayList<Vec2d>()
        for (i in 0 until np) poly.add(Vec2d(rand.nextDouble() * w, rand.nextDouble() * h))
        return poly
    }
    fun randColor() = Color.getHSBColor(rand.nextFloat(), 1f, 1f)

    val nPoly = 5

    val polys = ArrayList<PolyDraw>()
    for (i in 0 until nPoly) polys.add(PolyDraw(randPoly(), randColor(), null))

    val easyDraw = EasyDraw()
    easyDraw.drawable.addAll(polys)
    // easyDraw.drawable.add(GridLines())

    JEasyFrame(easyDraw, "EasyDraWTest")
}
