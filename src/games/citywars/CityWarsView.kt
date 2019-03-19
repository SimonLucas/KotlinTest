package games.citywars

import utilities.JEasyFrame
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.JComponent


fun main() {

    val game = CityWars()
    val view = CityWarsView(game)

    JEasyFrame(view, "City Wars")

}


class CityWarsView
//    public GridView(GridGame gridGame) {
//        this.gridGame = gridGame;
//    }


(// GridGame gridGame;
        var game: CityWars) : JComponent() {

    internal var cellSize = 50
    internal var deadBlack = true

    val colorMap = hashMapOf<Int,Color>(
            0 to Color.white,
            1 to Color.getHSBColor(0.35f, 1f, 1f),
            2 to Color.black
    )

    var gridLines = true

    public override fun paintComponent(g: Graphics) {
        with (game) {
            val grid = board
            val n = grid.w * grid.h
            for (i in 0 until n) {

                val cell = grid.getCell(i)
                g.color = colorMap[cell]
                val x = cellSize * (i % grid.w)
                val y = cellSize * (i / grid.w)

                g.fillRect(x, y, cellSize, cellSize)
                val nTroops = game.troops.grid[i]



            }
            // paint faint gridlines separately
            if (gridLines) {
                g.color = Color(128, 128, 128, 128)
                for (i in 0 until n) {
                    val x = cellSize * (i % grid.w)
                    val y = cellSize * (i / grid.w)
                    g.drawRect(x, y, cellSize, cellSize)
                }
            }
        }
    }

    override fun getPreferredSize(): Dimension {
        val grid = game.board
        return Dimension(cellSize * grid.w, cellSize * grid.h)
    }
}


