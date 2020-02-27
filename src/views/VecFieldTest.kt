package views

import math.Vec2d
import utilities.JEasyFrame
import java.awt.Color
import kotlin.random.Random

fun main() {

    // randomly rendered vector field of a simple differential equation
    // dy/dx = -x/y

    val rand = Random
    val n = 10000
    val w = 600
    val h = 600
    val len = 10.0
    val draw = EasyDraw(w, h)
    draw.drawable.add(CellDraw(0.0, 0.0, w.toDouble(), h.toDouble(), fill=Color.black, stroke = null))

    for (i in 0 until n) {
        val a = Vec2d(w * rand.nextDouble(), h * rand.nextDouble())
        val grad = Vec2d(-a.y+h/2, a.x-w/2).normalized // , so y-step = -x, x-step = y
        val dv = grad*(len/2)
        val line = LineDraw(a-dv, a+dv, stroke = Color(255, 0, 0, 155), lineWidth = 2.0f)
        draw.drawable.add(line)
    }

    JEasyFrame(draw, "Vector Field")

}


