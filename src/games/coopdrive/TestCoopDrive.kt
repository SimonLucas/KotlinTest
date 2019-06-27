package games.coopdrive

import utilities.JEasyFrame

fun main() {
    println("Testing coop drive")

    var state = CoopDriveState()
    val view = CoopDriveView(state)
    val delay = 50

    val frame = JEasyFrame(view, "Coop Driving Sim")

    while (!state.isTerminal()) {
        view.repaint()
    }

}

