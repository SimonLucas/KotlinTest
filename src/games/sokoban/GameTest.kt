package games.sokoban

import agents.RandomAgent
import agents.SimpleEvoAgent
import ggi.SimplePlayerInterface
import utilities.JEasyFrame

object Constants {
    val player1 = 0
    val player2 = 1
}

fun main(args: Array<String>) {

    var game = Sokoban()
    game.print()

    val gv = SokobanView(game.board)
    val frame = JEasyFrame(gv, "Sokoban!")
    val actions = intArrayOf(0, 0)
    var agent: SimplePlayerInterface = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 5, nEvals = 40)
//    var agent = RandomAgent()

//    var decisionTree : DecisionTree? = null
//    val modelTrainer = ForwardModelTrainer(InputType.Simple)

    val nSteps = 2000
    for (i in 0 until nSteps) {
        actions[0] = agent.getAction(game.copy(), Constants.player1)
        game.next(actions)

        gv.grid = game.board
        gv.repaint()
        Thread.sleep(50)
        frame.title = "tick = ${game.nTicks}, score = ${game.score()}"
        // System.exit(0)
        //println("$i\t N distinct patterns learned = ${learner.lut.size}")

        //decisionTree = modelTrainer.trainModel(data) as DecisionTree
    }

}
