package games.gridworld

import agents.SimpleEvoAgent
import ggi.SimplePlayerInterface
import utilities.ElapsedTimer
import utilities.StatSummary

fun main() {
    var gridWorld = GridWorld()
    // level 1 has subgoals, level 0 does not
    gridWorld.readFile("data/GridWorld/Levels/level-0.txt")
    // gridWorld.readFile("data/GridWorld/Levels/level-1.txt")
    // gridWorld.readFile("data/GridWorld/Levels/level-2.txt")
    gridWorld.simpleGrid.print()

    val nExp = 1000
    val agent = SimpleEvoAgent(useMutationTransducer = false, discountFactor = 0.9,
            nEvals = 20, sequenceLength = 100, probMutation = 0.2, useShiftBuffer = true)

    GridWorldConstants.subgoalWeight = 0.01

    val ss = StatSummary()

    val t = ElapsedTimer()
    nExp.downTo(1).forEach {
        ss.add(runTest(gridWorld.copy() as GridWorld, agent))
    }

    println(ss)

    println(t)
    println("Total ticks: " + gridWorld.totalTicks())
}

fun runTest(gw: GridWorld, agent: SimplePlayerInterface) : Double {
    var gridWorld = gw
    while (!gridWorld.isTerminal()) {
        val action = agent.getAction(gridWorld.copy(), 0)
        gridWorld = gridWorld.next(intArrayOf(action)) as GridWorld
    }
    return gridWorld.nTicks.toDouble()
}