package views

import java.awt.Graphics2D

interface CellPainter {
    fun paintCell(g: Graphics2D, x:Int, y:Int, scale: Int)
}

// val wall : CellPainter = {}


val SokoPaint: HashMap<Char,CellPainter> = hashMapOf(

)