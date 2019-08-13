package views

import games.gridworld.GridWorld
import math.Vec2d
import utilities.StatSummary
import java.awt.Color

// this uses EasyDraw for the plotting
// but still deciding how to make the interfaces as easy as possible for this

class EasyPlot {
    val view = EasyDraw()
    fun update(scores: ArrayList<DoubleArray>) {

        val ssx = StatSummary()
        val ssy = StatSummary()

        for (sa in scores) {
            ssx.add(sa.size)
            for (s in sa) ssy.add(s)
        }

        val drawList = ArrayList<Drawable>()

        val lineColor = Color(255, 0, 128, 10)
        val w = view.width
        val xInc = view.width / (ssx.max())
        val yScale = view.height / (ssy.max() - ssy.min())

        for (sa in scores) {
            val points = ArrayList<Vec2d>()

            var x = 0.0

            for (s in sa) {
                val y = view.height - (yScale * (s - ssy.min()))
                points.add(Vec2d(x, y))
                x += xInc
            }
            drawList.add(PolyDraw(points, null, lineColor, false))
        }

        view.drawable = drawList
        view.repaint()

    }
}
