package games.coopdrive

import agents.RandomAgent
import agents.RandomAgentMulti
import agents.SimpleEvoAgent
import agents.SimpleEvoAgentMulti
import ggi.SimplePlayerInterface
import ggi.SimplePlayerInterfaceMulti
import utilities.JEasyFrame
import java.util.*

fun main() {
    println("Testing coop drive")

    val player1 = SimpleEvoAgentMulti(
            useMutationTransducer = true,
            sequenceLength = 500,
            probMutation = 0.02,
            nEvals = 100,
            discountFactor = 0.99,
            repeatProb = 0.4,
            opponentModel = RandomAgentMulti(),
            useShiftBuffer = true)
    var player2: SimplePlayerInterfaceMulti = SimpleEvoAgentMulti()
    // player2 = player1.copy()
    val player1Id = 0
    val player2Id = 1

    // player2 = RandomAgent()

    val params = CoopDriveParams(timePenalty = 1, parkingBonus = 50000)
    var state = CoopDriveState(params)
    val view = CoopDriveView(state)
    val delay = 20L

    val frame = JEasyFrame(view, "Coop Driving Sim")

    while (!state.isTerminal(0)) {
        val actions = intArrayOf(
                player1.getAction(state.copy(), player1Id),
                player2.getAction(state.copy(), player2Id))
//        println("Selected: ${actions[0]}")
//        println(state.state)
        state.next(actions)
        // println(Arrays.toString(actions))
        view.repaint()
        frame.title = state.messageString()
        Thread.sleep(delay)
    }
    println(state.state.vehicles)
    println(state.state.parkingBonus((state.state.vehicles[0])))
    println("Game over")

}

