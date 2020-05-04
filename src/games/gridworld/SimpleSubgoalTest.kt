package games.gridworld

import agents.GridWorldSubgameAdapter
import agents.SubgoalActionFinder
import utilities.JEasyFrame

fun main() {
    var gridWorld = GridWorld()
    // level 1 has subgoals, level 0 does not
    // gridWorld.readFile("data/GridWorld/Levels/level-0.txt")
    gridWorld.readFile("data/GridWorld/Levels/level-1.txt")

    val gridView = GridWorldView(gridWorld.simpleGrid.w, gridWorld.simpleGrid.h)

    gridView.update(gridWorld).repaint()

    JEasyFrame( gridView.view, "Simple Macro Test")
    println(gridWorld.goal)

    var macroGame = GridWorldSubgameAdapter(gridWorld)

    println("Initial grid position")
    println( (macroGame.microGame as GridWorld).gridPosition )
    macroGame.next(intArrayOf(1))

    println("After taking a macro action")
    println( (macroGame.microGame as GridWorld).gridPosition )

    gridView.update(macroGame.microGame as GridWorld).repaint()

}
