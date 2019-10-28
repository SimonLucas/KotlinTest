package views

import games.caveswing.CaveGameState
import math.Vec2d
import java.awt.*
import java.awt.geom.Ellipse2D
import java.awt.geom.Path2D
import java.awt.geom.Rectangle2D
import javax.swing.JComponent

interface Drawable {
    fun draw(g: Graphics2D)
    fun bounds() : Rectangle? = null
}

class LineDraw {
    // may be not needed ...

//    private fun drawShipPlayout(g: Graphics2D, gameState: CaveGameState, seq: IntArray) {
//
//        val path = Path2D.Double()
//        var pos = gameState.avatar.s
//        path.moveTo(pos.x, pos.y)
//        for (a in seq) {
//            gameState.next(intArrayOf(a))
//            pos = gameState.avatar.s
//            path.lineTo(pos.x, pos.y)
//        }
//        g.draw(path)
//
//    }


}

class PolyDraw (val poly: ArrayList<Vec2d>, val fill: Color?, val stroke: Color?, val closed: Boolean = true) : Drawable {
    var s = Vec2d()
    override fun draw(g: Graphics2D) {
        val at = g.transform
        g.translate(s.x, s.y)
        if (fill != null) {
            g.color = fill
            g.fill(polygon)
        }
        if (stroke != null) {
            g.color = stroke
            g.draw(polygon)
        }
        g.transform = at
    }

    val polygon: Path2D.Double

    init {
        polygon = Path2D.Double()
        polygon.moveTo(poly[0].x, poly[0].y)
        for (i in 0 until poly.size) polygon.lineTo(poly[i].x, poly[i].y)
        if (closed) polygon.closePath()
    }
}

class GridLines(val w: Int, val h: Int, val sx: Int, val sy: Int,
                var color: Color = Color(128, 128, 128, 128) ) : Drawable {
    override fun draw(g: Graphics2D) {
        g.color = color
        for (i in 0 until w*h) {
            val x = sx * (i % w)
            val y = sy * (i / w)
            g.drawRect(x, y, sx, sy)
        }
    }
}

class CellDraw(val x:Double, val y: Double, val w:Double = 10.0, val h:Double = 10.0, val fill: Color?, val stroke: Color?) : Drawable {
    val rect: Rectangle2D.Double
    init {
        rect = Rectangle2D.Double(x, y, w, h)
    }
    override fun draw(g: Graphics2D) {
        if (fill != null) {
            g.color = fill
            g.fill(rect)
        }
        if (stroke != null) {
            g.color = stroke
            g.draw(rect)
        }
    }
}

class Ellipse(val x:Double, val y: Double, val w:Double = 10.0, val h:Double = 10.0, val fill: Color?, val stroke: Color?) : Drawable {
    val rect: Ellipse2D.Double
    init {
        rect = Ellipse2D.Double(x, y, w, h)
    }
    override fun draw(g: Graphics2D) {
        if (fill != null) {
            g.color = fill
            g.fill(rect)
        }
        if (stroke != null) {
            g.color = stroke
            g.draw(rect)
        }
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