package games.gridworld

import agents.PolicyEvoAgent
import agents.SubgoalActionFinder
import ggi.SimplePlayerInterface
import utilities.JEasyFrame
import views.EasyPlot
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.util.*
import javax.swing.JComponent

// todo: Add in a Score Prediction Consistency Test (a bit like the StickToPlanRate, but for values not actions

// todo: Add in a novelty value, and a Novelty Policy

// should the Novelty policy be updates like this also?

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

    var heuristic: SimplePlayerInterface? = null
    var policy = heuristic

    policy = null
    // heuristic = MinDistancePolicy()
    // heuristic = null

    // policy = MinDistancePolicy()

    var vf: MinDistancePolicy? = MinDistancePolicy()
    vf = null

    var agent: SimplePlayerInterface = PolicyEvoAgent(useMutationTransducer = false, discountFactor = 0.99, flipAtLeastOneValue = false,
            nEvals = 20, sequenceLength = 50, probMutation = 0.1, useShiftBuffer = true, policy = policy,
            initUsingPolicy = 0.5, mutateUsingPolicy = 0.5, appendUsingPolicy = 0.5,
            valueFunction = vf,
            analysePlans = true)

    // agent = MinDistancePolicy()

    val gridView = GridWorldView(gridWorld.simpleGrid.w, gridWorld.simpleGrid.h)
    val scoreView = EasyPlot()

    val both = PairView(gridView.view, scoreView.view)
    val frame = JEasyFrame(both, "Grid World Test")

    var step = 0
    val maxSteps = 100
    while (!gridWorld.isTerminal() && step++ < maxSteps) {
        val action = agent.getAction(gridWorld.copy(), 0)
        gridWorld = gridWorld.next(intArrayOf(action)) as GridWorld
        // println("${step++} -> \t ${gridWorld.score()}")
        gridView.update(gridWorld)
        if (agent is PolicyEvoAgent)
            gridView.update(agent.solutions, gridWorld.copy() as GridWorld)
        gridView.repaint()

        if (agent is PolicyEvoAgent)
            scoreView.update(agent.scores)

//        for (seq in agent.solutions) println(Arrays.toString(seq))
//        println()
//
//        for (sa in agent.scores) println(Arrays.toString(sa))
//        println()

        frame.title = "Score: %.2f".format(gridWorld.score())
//        println(gridWorld.copy().score())
//        println(gridWorld.score())

        if (agent is PolicyEvoAgent)
            println(Arrays.toString(agent.scores.last()))
        Thread.sleep(100)
    }

    println()
    println("Final score: ${gridWorld.score()}")
    println("Steps taken: ${gridWorld.nTicks}")

    if (agent is PolicyEvoAgent)
        agent.planAnalyser?.report(gridWorld.nActions())


}

class PairView(grid: JComponent, scores: JComponent) : JComponent() {
    init {
        layout = FlowLayout()
        add(grid, BorderLayout.NORTH)
        add(scores, BorderLayout.SOUTH)
        doLayout()
    }
}
