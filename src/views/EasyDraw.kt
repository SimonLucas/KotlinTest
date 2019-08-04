package views

import math.Vec2d
import java.awt.*
import java.awt.geom.Path2D
import javax.swing.JComponent

interface Drawable {
    fun draw(g: Graphics2D)
}

class PolyDraw (val poly: ArrayList<Vec2d>, val fill: Color?, val stroke: Color?) : Drawable {
    override fun draw(g: Graphics2D) {
        if (fill != null) {
            g.color = fill
            g.fill(polygon)
        }
        if (stroke != null) {
            g.color = stroke
            g.draw(polygon)
        }
    }

    val polygon: Path2D.Double

    init {
        polygon = Path2D.Double()
        polygon.moveTo(poly[0].x, poly[0].y)
        for (i in 0 until poly.size) polygon.lineTo(poly[i].x, poly[i].y)
        polygon.closePath()
    }
}

class EasyDraw(val dw:Int = 600, val dh:Int = 350) : JComponent() {

    var drawable = ArrayList<Drawable>()

    override fun getPreferredSize(): Dimension {
        return Dimension(dw, dh)
    }

    override fun paintComponent(go: Graphics?) {
        // super.paintComponent(go)
        if (go != null) {
            val g = go as Graphics2D
            for (d in drawable) {
                // println("Drawing: " + d)
                d.draw(g)
            }
        }
    }
}