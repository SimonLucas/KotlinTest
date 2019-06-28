package games.coopdrive

import agents.SimpleEvoAgent
import utilities.JEasyFrame

fun main() {
    println("Testing coop drive")

    val player = SimpleEvoAgent(
            useMutationTransducer = true,
            sequenceLength = 500,
            probMutation = 0.02,
            nEvals = 100,
            discountFactor = 0.99,
            repeatProb = 0.4,
            useShiftBuffer = true)
    val playerId = 0

    val params = CoopDriveParams(timePenalty = 1, parkingBonus = 50000)
    var state = CoopDriveState(params)
    val view = CoopDriveView(state)
    val delay = 20L

    val frame = JEasyFrame(view, "Coop Driving Sim")

    while (!state.isTerminal()) {
        val actions = intArrayOf(player.getAction(state.copy(), playerId))
//        println("Selected: ${actions[0]}")
//        println(state.state)
        state.next(actions)
        view.repaint()
        frame.title = state.messageString()
        Thread.sleep(delay)
    }
    println(state.state.vehicles)
    println(state.state.parkingBonus((state.state.vehicles[0])))
    println("Game over")

}

