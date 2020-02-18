package views

import games.gridworld.GridWorld
import math.Vec2d
import utilities.JEasyFrame
import utilities.StatSummary
import java.awt.Color
import kotlin.random.Random

// this uses EasyDraw for the plotting
// but still deciding how to make the interfaces as easy as possible for this

fun main() {
    val ep = EasyPlot()
    val n = 100
    val data = DoubleArray(n)
    val rand = Random(0)
    for (i in 0 until n) data[i] = (rand.nextDouble())
    val al = ArrayList<DoubleArray>()
    al.add(data)
    JEasyFrame(ep.view, "Easy Plot Test")
    ep.update(al)
    println(ep.view.drawable)
}

class EasyPlot {
    val view = EasyDraw()


    fun easy(data: ArrayList<StatSummary>): EasyPlot {
        val a = DoubleArray(data.size)
        for (i in 0 until data.size) a[i] = data[i].mean()
        JEasyFrame(view, "Data")
        val dat = ArrayList<DoubleArray>()
        dat.add(a)
        update(dat)
        return this
    }


    fun update(
            scores: ArrayList<DoubleArray>,
            lineColor: Color = Color(255, 0, 128, 50),
            ssy: StatSummary = StatSummary()
            // pass one initialised with a range to extend beyond the actual data
            ) {

        val ssx = StatSummary()

        for (sa in scores) {
            ssx.add(sa.size)
            for (s in sa) ssy.add(s)
        }

        println(ssy)

        val drawList = ArrayList<Drawable>()

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
            if (points.size > 1)
                drawList.add(PolyDraw(points, null, lineColor, false))
        }

        view.drawable = drawList
        view.repaint()

    }


}
