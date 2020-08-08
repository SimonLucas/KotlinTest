package games.gridworld

import agents.PolicyEvoAgent
import agents.SimpleEvoAgent
import ggi.SimplePlayerInterface
import utilities.ElapsedTimer
import utilities.JEasyFrame
import utilities.StatSummary
import views.GridView

fun main() {
    var gridWorld = GridWorld()
    // level 1 has subgoals, level 0 does not
    gridWorld.readFile("data/GridWorld/Levels/level-0.txt")
    gridWorld.readFile("data/GridWorld/Levels/level-1.txt")
    gridWorld.readFile("data/GridWorld/Levels/level-2.txt")
    // gridWorld.readFile("data/GridWorld/Levels/level-3.txt")

    val gridView = GridWorldView(gridWorld.simpleGrid.w, gridWorld.simpleGrid.h)
    gridView.update(gridWorld)
    gridView.repaint()

    JEasyFrame(gridView.view, "GridView")

    gridWorld.simpleGrid.print()

    val nExp = 10
//    val agent = SimpleEvoAgent(useMutationTransducer = true, discountFactor = 0.9,
//            nEvals = 20, sequenceLength = 100, probMutation = 0.2, useShiftBuffer = true)

    var heuristic : SimplePlayerInterface? = null
    // heuristic = MinDistancePolicy()

//    val agent = PolicyEvoAgent(useMutationTransducer = true, discountFactor = 0.9,
//            nEvals = 20, sequenceLength = 100, probMutation = 0.2,
//            useShiftBuffer = true, policy = heuristic)

//    val agent = PolicyEvoAgent(useMutationTransducer = false, discountFactor = 1.0, flipAtLeastOneValue = false,
//            nEvals = 10, sequenceLength = 50, probMutation = 0.5, useShiftBuffer = true, policy = null,
//            initUsingPolicy = 0.5,
//            appendUsingPolicy = 0.5,
//            mutateUsingPolicy = 0.5)

    val agent = MinDistancePolicy()

    GridWorldConstants.subgoalWeight = 0.0
    GridWorldConstants.distanceWeight = 0.0

    val ss = StatSummary()

    val t = ElapsedTimer()
    nExp.downTo(1).forEach {
        ss.add(runTest(gridWorld.copy() as GridWorld, agent, gridView))
    }

    println(ss)

    println(t)
    println("Total ticks: " + gridWorld.totalTicks())
}

fun runTest(gw: GridWorld, agent: SimplePlayerInterface, gridView: GridWorldView?) : Double {
    var gridWorld = gw
    val nSubgoals = 30
    gridWorld.addSubgoals(nSubgoals)

    if (gridView != null) {
        gridView.update(gw)
        gridView.repaint()
    }

    // println("Grid has ${gridWorld.subgoals.size} subgoals")

    agent.reset()
    while (!gridWorld.isTerminal()) {
        val action = agent.getAction(gridWorld.copy(), 0)
        gridWorld = gridWorld.next(intArrayOf(action)) as GridWorld
    }
    return gridWorld.nTicks.toDouble()
}

