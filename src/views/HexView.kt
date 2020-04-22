package views

import math.Vec2d
import utilities.JEasyFrame
import utilities.Picker
import java.awt.Color
import java.awt.Graphics2D
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.lang.Math.PI
import kotlin.random.Random

fun main() {
    val hv = HexView(borderWidth = 0, h = 7, w=7)

    JEasyFrame(hv.view, "Hex View")
}

class HexView(val w: Int = 6, val h: Int = 6, val cellWidth: Int = 50, val borderWidth: Int = 50) : Drawable {

    val toEdge = cellWidth * Math.cos(PI / 6)
    val board = Array(w) { IntArray(h) { 0 } }; // Array<IntArray>
    val cellValues = 3;

    val view = EasyDraw(getWidth(), getHeight())


    init {
        println(getWidth())
        println(getHeight())

        view.addMouseListener(
                object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent?) {
                        super.mouseClicked(e)
                        if (e != null) process(e.x, e.y)
                    }
                }
        )
    }

    fun process(x: Int, y: Int) {
        println("($x, $y)")
        println(pixelToGrid(x, y))
        val selection = pixelToGrid(x, y)
        if (selection != null) {
            val (i, j) = selection
            board[i][j] = next(board[i][j])
            repaint()
        }
    }

    fun next(v: Int) : Int {
        return (v + 1) % 3
    }


    fun getWidth() = (borderWidth * 2 + toEdge * 2 * w + toEdge * (h - 1)).toInt()
    fun getHeight() = (borderWidth * 2 + cellWidth * (2 + 1.5 * (h - 1))).toInt()

    init {
        randBoard()
        view.drawable.add(this)
    }

    fun randBoard() {
        for (i in 0 until w)
            for (j in 0 until h)
                board[i][j] = Random.nextInt(cellValues)
    }

    val toDraw = ArrayList<Drawable>()

    fun makeDraw() {

    }

    fun makePolygon(n: Int = 6, rad: Double = 10.0, startAngle: Double = 0.0): ArrayList<Vec2d> {
        val verts = ArrayList<Vec2d>()

        val step = (2 * PI) / n
        for (i in 0 until n) {
            val angle = startAngle + i * step
            val x = rad * Math.sin(angle)
            val y = rad * Math.cos(angle)
            verts.add(Vec2d(x, y))
        }
        return verts

    }

    override fun draw(g: Graphics2D) {

        // create aa polygon to draw
        val lineColor = Color.gray
        val fills = arrayOf(Color.red, Color.lightGray, Color.blue)

        val poly = PolyDraw(makePolygon(rad = cellWidth.toDouble()), stroke = lineColor)

        for (i in 0 until w) {
            for (j in 0 until h) {
                val trans = g.transform
                g.translate(xc(i, j), yc(i, j))
                poly.fill = fills[board[i][j]]
                poly.draw(g)
                g.transform = trans
            }
        }
    }

    fun xc(i: Int, j: Int) =
            cellWidth * 2 * Math.cos(PI / 6) * (0.5 + i + j * Math.sin(PI / 6))

    fun yc(i: Int, j: Int) = cellWidth * (j * 1.5 + 1)

    fun pixelToGrid(x:Int, y:Int) : Pair<Int,Int>? {
        // check whether they are in bounds
        // find the closest one within bounds
        // could easily make it faster with a direct mapping,
        // but this way is so easy
        val picker = Picker<Pair<Int,Int>>()
        val p = Vec2d(x.toDouble(),y.toDouble())
        for (i in 0 until w) {
            for (j in 0 until h) {
                val s = p.distanceTo(Vec2d(xc(i,j), yc(i,j)))
                if (s < toEdge) picker.add(s, Pair(i,j))
            }
        }
        return picker.best
    }

//    fun pixelToGrid(x:Int, y:Int) : Pair<Int,Int>? {
//        // check whether they are in bounds
//        val gridY = (y / (cellWidth * 1.5)).toInt()
//        val gridX = (x / cellWidth).toInt()
//        return Pair(gridX, gridY)
//    }

    fun repaint() = view.repaint()
}

// have a sepaarate class for the board
class HexBoard {



}
