package geometry.voronoi

import javafx.scene.shape.Polygon
import math.Vector2d
import utilities.ElapsedTimer
import utilities.JEasyFrame
import utilities.Picker

import javax.swing.*
import java.awt.*
import java.awt.geom.Ellipse2D
import java.awt.geom.Line2D
import java.awt.geom.Path2D
import java.util.ArrayList
import java.util.Random

class VoronoiGrid : JComponent() {

    var w = 800
    var h = 500
    var r = 5.0
    var vps = ArrayList<VoronoiPoint>()

    val defaultColour = Color.black

    public override fun paintComponent(go: Graphics) {
        val g = go as Graphics2D

        g.stroke = BasicStroke(2f)

        val d = size

        for (vp in vps) {

            for (p in vp.vn) {
                val l1 = Line2D.Double(vp.point.x, vp.point.y, p.x, p.y)
                g.draw(l1)
                val bis = LineUtil().bisector(vp.point, p)
                val l2 = Line2D.Double(bis.a.x, bis.a.y, bis.b.x, bis.b.y)
                g.draw(l2)
                // val poly =
            }
            if (vp.poly.size > 0) {
                val poly = Path2D.Double()
                // poly.moveTo(vp.point.x, vp.point.y)
                poly.moveTo(vp.poly[vp.poly.size - 1].x,
                        vp.poly[vp.poly
                                .size - 1].y)
                for (p in vp.poly) {
                    poly.lineTo(p.x, p.y)
                }
                poly.closePath()
                g.color = randColor()
                g.fill(poly)
            }


        }
        g.color = Color.BLACK

        for (vp in vps) {
            val dot = Ellipse2D.Double(vp.point.x - r, vp.point.y - r, 2 * r, 2 * r)
            g.fill(dot)
        }
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(w, h)
    }

    fun randColor(): Color {
        // return Color.getHSBColor(random.nextFloat(), 0.5f + random.nextFloat()/2, 1);
        // return Color.getHSBColor(random.nextFloat(), random.nextFloat(), 1);
        return Color.getHSBColor(Random().nextFloat(), 1f, 1f)
        // return Color.getHSBColor(random.nextFloat(), random.nextFloat(), 0.7f * random.nextFloat() + 0.3f);
    }
}
