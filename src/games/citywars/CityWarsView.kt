package games.citywars

import utilities.DrawUtil
import utilities.JEasyFrame
import java.awt.*
import java.lang.Math.abs
import javax.swing.JComponent


val showNumbers = true
val showDots = true
val showCityOutline = true

val playerOneColor = Color.getHSBColor(0.2f, 1f, 1f)
val playerTwoColor = Color.getHSBColor(0.8f, 1f, 1f)

fun main() {

    val game = CityWars()
    val view = CityWarsView(game)

    println("Score: " + game.score());
    println("isTerminal: " + game.isTerminal());

    JEasyFrame(view, "City Wars")
    println('[')
    game.report()
    println(']')
}


class CityWarsView
//    public GridView(GridGame gridGame) {
//        this.gridGame = gridGame;
//    }


(// GridGame gridGame;
        var game: CityWars) : JComponent() {

    internal var cellSize = 50
    internal var deadBlack = true

    val colorMap = hashMapOf<Int, Color>(
            0 to Color.white,
            1 to Color.getHSBColor(0.35f, 1f, 1f),
            2 to Color.black
    )

    var gridLines = true

    public override fun paintComponent(go: Graphics) {
        val g = go as Graphics2D
        with(game) {
            val grid = board
            val n = grid.w * grid.h
            for (i in 0 until n) {

                val cell = grid.getCell(i)
                g.color = colorMap[cell]
                val x = cellSize * (i % grid.w)
                val y = cellSize * (i / grid.w)

                val nTroops = game.troops.grid[i]

                if (nTroops > 0) g.setColor(playerOneColor)
                if (nTroops < 0) g.setColor(playerTwoColor)

                g.fillRect(x, y, cellSize, cellSize)

                if (showNumbers) {
                    val n = abs(nTroops)
                    DrawUtil().centreString(g, n.toString(), x.toDouble()+cellSize/2, y.toDouble()+cellSize/2, Color.black)
                }

                if (showDots) {

                }



            }
            // paint faint gridlines separately
            if (gridLines) {
                g.color = Color(128, 128, 128, 128)
                for (i in 0 until n) {
                    val x = cellSize * (i % grid.w)
                    val y = cellSize * (i / grid.w)
                    g.drawRect(x, y, cellSize, cellSize)
                    if (showCityOutline && grid.getCell(i) == game.city) {
                        val stroke = g.stroke
                        g.color = Color.gray
                        g.stroke = BasicStroke(cellSize.toFloat()/10)
                        g.drawRect(x, y, cellSize, cellSize)
                        g.stroke = stroke

                    }
                }
            }
        }
    }

    override fun getPreferredSize(): Dimension {
        val grid = game.board
        return Dimension(cellSize * grid.w, cellSize * grid.h)
    }
}


