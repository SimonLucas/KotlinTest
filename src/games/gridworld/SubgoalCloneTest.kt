package games.gridworld

fun main() {

    var gridWorld = GridWorld()
    // level 1 has subgoals, level 0 does not
    // gridWorld.readFile("data/GridWorld/Levels/level-0.txt")
    gridWorld.readFile("data/GridWorld/Levels/level-1.txt")
    gridWorld.simpleGrid.print()
    gridWorld.subgoals.forEach { t -> println(t) }

    val subs = gridWorld.subgoals.clone() as HashSet<GridPosition>

    val gp = GridPosition(18,5)
    gridWorld.subgoals.remove(gp)

    // should print true followed by false showing that cloning works
    println(subs.contains(gp))
    println(gridWorld.subgoals.contains(gp))



}

