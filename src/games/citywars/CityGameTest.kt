package games.citywars

import agents.DoNothingAgent
import agents.RandomAgent
import agents.SimpleEvoAgent
import utilities.JEasyFrame

fun main() {

    val game = CityWars()
    val view = CityWarsView(game)

    val frame = JEasyFrame(view, "City Wars")

    val nSteps = 2000

    val p1 = SimpleEvoAgent(useMutationTransducer = false, nEvals = 100)
    val p2 = SimpleEvoAgent()
//    val p2 = DoNothingAgent()
    // val p1 = RandomAgent()

    var tick = 0;
    while(!game.isTerminal() && tick++ < nSteps) {

        val actions = intArrayOf(p1.getAction(game.copy(), 0), p2.getAction(game.copy(), 1))
        game.next(actions)
        frame.title = "$tick : ${ game.score() }"
        view.repaint()
        Thread.sleep(50)

    }

}
