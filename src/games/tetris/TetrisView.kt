package games.tetris

import utilities.JEasyFrame
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.JComponent
import kotlin.random.Random


/**
 * The View class is responsible for rendering the Tetris model
 */

fun main() {
    // test the view component

    val nCols = 10
    val nRows = 20

    val tv = TetrisView(nCols, nRows)
    val a = Array(nCols) { IntArray(nRows) { TetrisView.BG } };
    for (i in 0 until nCols)
        for (j in nRows/2 until nRows)
            a[i][j] = Random.nextInt(TetrisView.colors.size)
    val shape = TetronSprite(nCols/2, 3, 2, 1, 2)
    val ghost = TetronSprite(nCols/2, 7, 2, 3, 2)
    tv.setData(a, shape, ghost)
    JEasyFrame(tv, "Tetris View Random Test")
}

class TetrisView(val nCols: Int, val nRows: Int) : JComponent() {

    var topVisibleRow = 0
    var shape: TetronSprite? = null
    var ghostShape: TetronSprite? = null
    var a: Array<IntArray>? = null

    fun setData(a: Array<IntArray>?, shape: TetronSprite?, ghostShape: TetronSprite?) {
        this.a = a
        this.shape = shape
        this.ghostShape = ghostShape
    }

    fun draw(g: Graphics, a: Array<IntArray>?) {
        if (a == null) return
        for (i in 0 until nCols) {
            for (j in topVisibleRow until nRows) {
                // println(a[i][j])
                g.color = colors[a[i][j]]
                g.fill3DRect(i * cellSize, j * cellSize, cellSize, cellSize, true)
                if (a[i][j] == BG) {
                    g.color = frame
                    g.drawRect(i * cellSize, j * cellSize, cellSize, cellSize)
                }
            }
        }
    }

    fun drawShape(g: Graphics, ts: TetronSprite?) {
        if (ts == null) return
        g.color = colors[ts.color]
        for (cell in ts.getCells()) {
            g.fill3DRect(cell.x * cellSize, cell.y * cellSize, cellSize, cellSize, true)
        }
    }

    fun drawGhostShape(g: Graphics, ts: TetronSprite?) {
        if (ts == null) return
        g.color = Color.darkGray
        for (cell in ts.getCells()) {
            g.color = Color.darkGray
            g.fill3DRect(cell.x * cellSize, cell.y * cellSize, cellSize, cellSize, true)
            g.color = Color.white
            g.drawRect(cell.x * cellSize, cell.y * cellSize, cellSize, cellSize)
        }
    }

    @Synchronized
    public override fun paintComponent(g: Graphics) {
        draw(g, a)
        drawGhostShape(g, ghostShape)
        drawShape(g, shape)
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(nCols * cellSize, nRows * cellSize)
    }

    companion object {
        var colors = arrayOf(Color.green, Color.blue, Color.red,
                Color.yellow, Color.magenta, Color.pink, Color.cyan, Color.black, Color.gray)

        // size of each block in pixels
        var cellSize = 20

        // var BG = 0
        var frame = Color.blue

        // code for the background colour
        val BG = 7

    }
}

