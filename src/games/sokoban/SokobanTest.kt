package games.sokoban

import utilities.JEasyFrame

fun main() {
    val sokoban = Sokoban()
    val view = SokobanView(sokoban.board)
    JEasyFrame(view, "Sokoban")
}