package games.citywars

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.JComponent


fun main() {

    val game = CityWars()
    val view = CityWarsView(game.board)





}


class CityWarsView
//    public GridView(GridGame gridGame) {
//        this.gridGame = gridGame;
//    }


(// GridGame gridGame;
        var grid: Grid) : JComponent() {

    internal var cellSize = 20
    internal var deadBlack = true

    var gridLines = true

    public override fun paintComponent(g: Graphics) {
        val n = grid.w * grid.h
        for (i in 0 until n) {
            val h = if (grid.getCell(i) == 0) 0.35f else 0.89f
            g.color = Color.getHSBColor(h, 1f, 1f)
            if (deadBlack && grid.getCell(i) == 0) g.color = Color.black
            val x = cellSize * (i % grid.w)
            val y = cellSize * (i / grid.w)
            g.fillRect(x, y, cellSize, cellSize)
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

    override fun getPreferredSize(): Dimension {
        return Dimension(cellSize * grid.w, cellSize * grid.h)
    }
}


