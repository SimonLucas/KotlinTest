package games.gridworld

import agents.PolicyEvoAgent
import agents.SubgoalActionFinder
import ggi.SimplePlayerInterface
import utilities.JEasyFrame
import views.EasyPlot
import java.awt.BorderLayout
import java.util.*
import javax.swing.JComponent


fun main() {
    var gridWorld = GridWorld()
    // level 1 has subgoals, level 0 does not
    gridWorld.readFile("data/GridWorld/Levels/level-0.txt")
    // gridWorld.readFile("data/GridWorld/Levels/level-3.txt")
    gridWorld.simpleGrid.print()

    println(gridWorld.goal)

    val subAgent = SubgoalActionFinder()
    // subAgent.expandState(gridWorld)
//    println("Final macros: ")
//    subAgent.macros.forEach { t, u ->  println("$t ->  (${gridWorld.subgoals.contains(t)}) \t $u")}
//
    // now make an agent test

//    val agent = SimpleEvoAgent(useMutationTransducer = false, discountFactor = 0.9,
//            nEvals = 20, sequenceLength = 100, probMutation = 0.2, useShiftBuffer = true)

    var heuristic : SimplePlayerInterface? = null
    heuristic = MinDistancePolicy()

    val agent = PolicyEvoAgent(useMutationTransducer = false, discountFactor = 1.0, flipAtLeastOneValue = false,
            nEvals = 2, sequenceLength = 50, probMutation = 0.2, useShiftBuffer = true, policy = heuristic,
            initUsingPolicy = 1.0, mutateUsingPolicy = 1.0)

    val gridView = GridWorldView(gridWorld.simpleGrid.w, gridWorld.simpleGrid.h)
    val scoreView = EasyPlot()

    val both = PairView(gridView.view, scoreView.view)
    val frame = JEasyFrame(both, "Grid World Test")

    var step = 0
    while (!gridWorld.isTerminal()) {
        val action = agent.getAction(gridWorld.copy(), 0)
        gridWorld = gridWorld.next(intArrayOf(action)) as GridWorld
        // println("${step++} -> \t ${gridWorld.score()}")
        gridView.update(gridWorld)
        gridView.update(agent.solutions, gridWorld.copy() as GridWorld)
        gridView.repaint()

        scoreView.update(agent.scores)

//        for (seq in agent.solutions) println(Arrays.toString(seq))
//        println()
//
//        for (sa in agent.scores) println(Arrays.toString(sa))
//        println()

        frame.title = "Score: %.2f".format(gridWorld.score())
//        println(gridWorld.copy().score())
//        println(gridWorld.score())
        println(Arrays.toString(agent.scores.last()))
        Thread.sleep(1000)
    }

    println()
    println("Final score: ${gridWorld.score()}")
    println("Steps taken: ${gridWorld.nTicks}")


}

class PairView(grid: JComponent, scores: JComponent) : JComponent() {
    init {
        layout = BorderLayout()
        add(grid, BorderLayout.NORTH)
        add(scores, BorderLayout.SOUTH)
    }
}