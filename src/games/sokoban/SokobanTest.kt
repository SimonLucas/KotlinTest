package games.sokoban

import games.sokogame.SokobanGame

fun main() {
    val sokoban = SokobanGame()
    val view = SokobanView(sokoban.board)
}