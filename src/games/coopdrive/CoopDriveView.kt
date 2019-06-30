package games.coopdrive

import math.Vec2d
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.geom.Ellipse2D
import java.awt.geom.Path2D
import javax.swing.JComponent


class CoopDriveView (var state: CoopDriveState = CoopDriveState()): JComponent() {
    var gridLines = true;
    val bg = Color.black
    val goalSought = Color.red
    val goalFound = Color.green

    override fun paintComponent(go: Graphics) {
        val g = go as Graphics2D

        g.color = bg
        g.fillRect(0, 0, width, height)

        drawGoals(g, state.state.vehicles)
        drawVehicles(g, state.state.vehicles)
        if (gridLines) drawGrid(g, 20, 600, 30)
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
        for (v in vehicles) {
            val tv = TransporterView().setState(v)
            tv.draw(g)
        }
    }

    fun drawGoals(g: Graphics2D, vehicles: ArrayList<Vehicle>) {
        for (v in vehicles) {
            val goal = state.state.getGoal(v.id)
            val rad = state.state.range()
            val circle = Ellipse2D.Double(goal.x-rad, goal.y - rad, rad*2, rad*2)
            // println(circle)
            g.color = if (state.atGoal(v)) goalFound else goalSought
            g.fill(circle)
        }
    }
}


class TransporterView {
    val vColor = Color.cyan

    internal var v = Vehicle()

    fun setState(v: Vehicle): TransporterView {
        this.v = v
        return this
    }

    override fun toString(): String {
        return v.toString()
    }

    fun draw(g: Graphics2D) {
        val at = g.transform
        g.translate(v.s.x, v.s.y)
        val rot = Math.atan2(v.d.y, v.d.x) + Math.PI / 2
        g.rotate(rot)
        g.scale(v.scale, v.scale)

        // draw the collision disc
        val rad = 1.0
        val circle = Ellipse2D.Double( -rad, -rad, rad*2, rad*2)
        g.color = Color.gray
        // println(circle)
        g.fill(circle)

        g.color = vColor
        g.fillPolygon(xp, yp, xp.size)

        g.transform = at
    }

    companion object {
        internal var xp = intArrayOf(-1, 0, 1, 0)
        internal var yp = intArrayOf(1, -1, 1, 0)
    }
}
