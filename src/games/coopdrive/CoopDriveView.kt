package games.coopdrive

import math.Vec2d
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JComponent


class CoopDriveView (var state: CoopDriveState = CoopDriveState()): JComponent() {
    var gridLines = true;
    val vColor = Color.blue

    override fun paintComponent(go: Graphics) {
        val g = go as Graphics2D


        drawVehicles(g, state.state.vehicles)
        drawGrid(g, 20, 200, 30)

    }

    override fun getPreferredSize(): Dimension {
        return Dimension(state.state.w, state.state.h)
    }

    fun drawGrid(g: Graphics2D, cellSize: Int, n:Int, w:Int) {

        g.color = Color(128, 128, 128, 128)
        for (i in 0 until n) {
            val x = cellSize * (i % w)
            val y = cellSize * (i / w)
            g.drawRect(x, y, cellSize, cellSize)
        }

    }

    fun drawVehicles(g: Graphics2D, vehicles: ArrayList<Vehicle>) {


        g.color = vColor
        for (v in vehicles) {
            val tv = TransporterView().setState(v.s, v.v, 10.0)
        }

    }

}


class TransporterView {

    internal var s = Vec2d()
    internal var v = Vec2d()
    internal var scale: Double = 0.toDouble()

    fun setState(s: Vec2d, v: Vec2d, scale: Double): TransporterView {
        this.s = s
        this.v = v
        // this.d = v.copy();
        this.scale = scale
        // d.normalise();
        return this
    }

    override fun toString(): String {
        return "$s\t $v"
    }

    fun draw(g: Graphics2D) {
        val at = g.transform
        g.translate(s.x, s.y)
        val rot = Math.atan2(v.y, v.x) + Math.PI / 2
        g.rotate(rot)
        g.scale(scale, scale)
        g.fillPolygon(xp, yp, xp.size)
        g.transform = at
    }

    companion object {
        internal var xp = intArrayOf(-2, 0, 2, 0)
        internal var yp = intArrayOf(2, -2, 2, 0)
    }
}
