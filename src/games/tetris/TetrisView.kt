package games.tetris

import utilities.JEasyFrame
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.JComponent


/**
 * The View class is responsible for rendering the Tetris model
 */

fun main() {
    // test the view component
    val tm = TetrisModel()
    System.out.println(tm.newShape())
    val tv = TetrisView(tm, null)
    JEasyFrame(tv, "Tetris")
}

class TetrisView : JComponent {




    var tm: TetrisModel
    var tc: Controller?

    var topVisibleRow = 0

    constructor(tm: TetrisModel, tc: Controller?) {
        this.tm = tm
        this.tc = tc
    }

    fun draw(g: Graphics, a: Array<IntArray>) {
        // a[6][10] = 3;
        for (i in 0 until tm.nCols) {
            for (j in topVisibleRow until tm.nRows) {
                if (a[i][j] != 100 + TetrisConstants.BG) {
                    g.color = colors[a[i][j]]
                    g.fill3DRect(i * cellSize, j * cellSize, cellSize, cellSize, true)
                }
                if (a[i][j] == TetrisConstants.BG) {
                    g.color = frame
                    val nntc = tc
                    if (nntc != null && nntc.fastMode()) {
                        g.color = Color.red
                    }
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
        draw(g, tm.a)
        if (tm.tetronSprite != null) {

            drawGhostShape(g, tm.getGhost())
            drawShape(g, tm.tetronSprite)
            // Shape ghost = tm.shape.copy();
        }
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(tm.nCols * cellSize, tm.nRows * cellSize)
    }

    companion object {
        var colors = arrayOf(Color.green, Color.blue, Color.red,
                Color.yellow, Color.magenta, Color.pink, Color.cyan, Color.black, Color.gray)

        // size of each block in pixels
        var cellSize = 20
        // var BG = 0
        var frame = Color.blue
    }
}

