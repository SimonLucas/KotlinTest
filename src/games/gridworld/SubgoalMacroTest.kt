package games.gridworld

import agents.GridWorldSubgameAdapter
import agents.SimpleEvoAgent
import agents.SubgoalActionFinder
import utilities.JEasyFrame
import views.EasyPlot
import java.util.*

fun main() {
    var gridWorld = GridWorld()
    // level 1 has subgoals, level 0 does not
    // gridWorld.readFile("data/GridWorld/Levels/level-0.txt")
    gridWorld.readFile("data/GridWorld/Levels/level-1.txt")
    gridWorld.simpleGrid.print()

    println(gridWorld.goal)

    val subAgent = SubgoalActionFinder()

    subAgent.expandState(gridWorld)
    println("Final macros: ")
    subAgent.macros.forEach { t, u -> println("$t ->  (${gridWorld.subgoals.contains(t)}) \t $u") }

    // System.exit(0)

    // now make a new game

    var macroGame = GridWorldSubgameAdapter(gridWorld)

    val agent = SimpleEvoAgent(nEvals = 10, sequenceLength = 10)

    val gridView = GridWorldView(gridWorld.simpleGrid.w, gridWorld.simpleGrid.h)
    val scoreView = EasyPlot()

    val both = PairView(gridView.view, scoreView.view)
    val frame = JEasyFrame(both, "Grid World Test")

    var step = 0
    while (!macroGame.isTerminal()) {
        val action = agent.getAction(macroGame.copy(), 0)
        // System.exit(0)

        macroGame = macroGame.next(intArrayOf(action)) as GridWorldSubgameAdapter
        println("${step++} -> \t ${gridWorld.score()}")
        gridView.update(macroGame.microGame as GridWorld)
        gridView.update(agent.solutions, gridWorld.copy() as GridWorld)
        gridView.repaint()

        scoreView.update(agent.scores)

//        for (seq in agent.solutions) println(Arrays.toString(seq))
//        println()
//
//        for (sa in agent.scores) println(Arrays.toString(sa))
//        println()

        frame.title = "Score: %.2f".format(macroGame.score())
//        println(gridWorld.copy().score())
//        println(gridWorld.score())
        println(Arrays.toString(agent.scores.last()))
        Thread.sleep(100)
    }

    println()
    println("Final score: ${gridWorld.score()}")
    println("Steps taken: ${gridWorld.nTicks}")
    println("Total ticks: ${gridWorld.totalTicks()}")

}

