package geometry.voronoi

import math.Vector2d
import utilities.ElapsedTimer
import utilities.JEasyFrame
import utilities.Picker

import javax.swing.*
import java.awt.*
import java.util.ArrayList
import java.util.Random

class VoronoiGrid : JComponent() {

    internal var width = 800
    internal var height = 500
    internal var cellSize = 1
    internal var dimension = Dimension(width, height)

    internal var nPoints: Int = 0

    internal var points = ArrayList<Vector2d>()
    internal var colors = ArrayList<Color>()
    val defaultColout = Color.black

    public override fun paintComponent(go: Graphics) {
        val g = go as Graphics2D


        val d = size

        val timer = ElapsedTimer()
        var nCells = 0
        run {
            var y = 0
            while (y < d.getHeight()) {
                run {
                    var x = 0
                    while (x < d.getWidth()) {
                        nCells++
                        val closestIndex = getClosestPointIndex(Vector2d(x.toDouble(), y.toDouble()))
                        g.color = if (closestIndex == null) defaultColout else colors[closestIndex]
                        g.fillRect(x, y, cellSize, cellSize)
                        x += cellSize
                    }
                }
                y += cellSize
                // System.out.println(y);
            }
        }
        println("Painted n cells: $nCells")
        println(timer)

        // if (!dimension.equals(d) || )

    }

    fun setRandomPoints(n: Int): VoronoiGrid {
        this.nPoints = n
        colors = ArrayList()
        for (i in 0 until n) {
            val p1 = randomPoint()
            points.add(p1)
            points.add(reflectionXY(p1))
            val color = randColor()
            colors.add(color)
            colors.add(color)
        }
        return this
    }

    fun randomPoint(): Vector2d {
        return Vector2d(width * random.nextDouble(), height * random.nextDouble())
    }

    fun reflectionXY(p: Vector2d): Vector2d {

        return Vector2d(width - p.x, height - p.y)
    }

    fun getClosestPointIndex(probe: Vector2d): Int? {
        val picker = Picker<Int>(Picker.MIN_FIRST)
        for (i in 0 until nPoints) {
            picker.add(probe.dist(points[i]), i)
        }
        return picker.best
    }

    override fun getPreferredSize(): Dimension {
        return dimension
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            // System.out.println(new VoronoiGrid().s );

            val vg = VoronoiGrid().setRandomPoints(20)
            JEasyFrame(vg, "Voronoi GVGAISimpleTest")
        }

        internal var random = Random()

        fun randColor(): Color {
            // return Color.getHSBColor(random.nextFloat(), 0.5f + random.nextFloat()/2, 1);
            // return Color.getHSBColor(random.nextFloat(), random.nextFloat(), 1);
            return Color.getHSBColor(random.nextFloat(), 1f, 1f)
            // return Color.getHSBColor(random.nextFloat(), random.nextFloat(), 0.7f * random.nextFloat() + 0.3f);
        }
    }


}
