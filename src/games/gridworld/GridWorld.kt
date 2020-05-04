package games.gridworld

import games.gridworld.GridWorldConstants.avatarChar
import games.gridworld.GridWorldConstants.distanceWeight
import games.gridworld.GridWorldConstants.goalChar
import games.gridworld.GridWorldConstants.maxTicks
import games.gridworld.GridWorldConstants.navChar
import games.gridworld.GridWorldConstants.removeSubgoalsWhenVisited
import games.gridworld.GridWorldConstants.subgoalChar
import games.gridworld.GridWorldConstants.subgoalWeight
import games.gridworld.GridWorldConstants.tickWeight
import games.gridworld.GridWorldConstants.wallChar
import games.sokoban.SimpleGrid
import ggi.AbstractGameState
import ggi.ExtendedAbstractGameState
import math.Vec2d
import views.*
import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.Path2D
import java.lang.Exception
import kotlin.random.Random

fun main() {
    // make a simple gridworld and test it

    val gridWorld = GridWorld()
    val cp = gridWorld.copy() as GridWorld

    repeat(10) {
        cp.next(intArrayOf(1))
        println("${cp.gridPosition} -> ${cp.score()}")
    }
    println()
    println(gridWorld.score())
    println(gridWorld.nTicks)

}

data class GridPosition(val x: Int, val y: Int) {
    fun move(action: Int): GridPosition {
        when (action) {
            0 -> return GridPosition(x + 1, y)
            1 -> return GridPosition(x, y + 1)
            2 -> return GridPosition(x - 1, y)
            3 -> return GridPosition(x, y - 1)
            4 -> return GridPosition(x, y)  // do nothing
            else -> throw Exception("Invalid Action: $action")
        }
    }

    fun nActions() = 5

    fun getNeighbours() : ArrayList<GridPosition> {
        // note that this will add the current position to the set of neighbours
        // providing that doNothing is included in the set of actions
        val list = ArrayList<GridPosition>()
        for (i in 0 until nActions())
            list.add(move(i))
        return list
    }

    fun getVec(): Vec2d = Vec2d(x.toDouble(), y.toDouble())
}

object GridWorldConstants {
    val maxTicks = 1000
    val avatarChar = 'A'
    val goalChar = '5'
    val navChar = '.'
    val wallChar = 'w'
    val subgoalChar = 's'

    // set this to 0.0 to be true to the original
    var distanceWeight = 0.0
    val tickWeight = 0.01
    var subgoalWeight = 0.01
    var removeSubgoalsWhenVisited = false
}

var totalTicks: Long = 0



class GridWorld : ExtendedAbstractGameState {

    override fun totalTicks(): Long {
        return totalTicks
    }

    override fun resetTotalTicks() {
        totalTicks = 0
    }

    override fun randomInitialState(): AbstractGameState {
        gridPosition = GridPosition(1,1)
        return this

    }

    var nTicks = 0
    var gridPosition = GridPosition(0, 0)
    var simpleGrid = SimpleGrid(0, 0)
    var goal = GridPosition(0, 0)
    var terminal = false
    var subgoals = HashSet<GridPosition>()

    fun readFile(file: String) {
        simpleGrid = simpleGrid.readFromFile(file)
        // now process the grid to find the avatar location
        // and the goal location
        for (x in 0 until simpleGrid.w) {
            for (y in 0 until simpleGrid.h) {
                if (simpleGrid.getCell(x, y) == avatarChar) {
                    simpleGrid.setCell(x, y, navChar)
                    gridPosition = GridPosition(x, y)
                }
                if (simpleGrid.getCell(x, y) == goalChar) {
                    simpleGrid.setCell(x, y, navChar)
                    goal = GridPosition(x, y)
                }
                if (simpleGrid.getCell(x, y) == subgoalChar) {
                    simpleGrid.setCell(x, y, navChar)
                    subgoals.add(GridPosition(x, y))
                }
            }
        }
        println(gridPosition)
        println(goal)
        println("Subboals:")
        for (sg in subgoals) println(sg)
    }
    val random = Random

    fun addSubgoals(n: Int) {
        var nFails = 0
        do {
            val x = random.nextInt(simpleGrid.w)
            val y = random.nextInt(simpleGrid.h)
            val gp = GridPosition(x,y)
            if ( simpleGrid.getCell(x, y) == navChar && !subgoals.contains(gp))
                subgoals.add(gp)
            else
                nFails++

        } while (subgoals.size < n && nFails < n*5)
        println("Added ${subgoals.size} random subgoals after $nFails failures")
    }

    fun isSubgoal(): Boolean {
        return subgoals.contains(gridPosition)
    }

    override fun copy(): AbstractGameState {
        val cp = GridWorld()
        cp.gridPosition = gridPosition
        cp.goal = goal
        // the grid is never modified, so no need for a deep copy
        cp.simpleGrid = simpleGrid
        cp.nTicks = nTicks
        cp.terminal = terminal
        cp.subgoals = subgoals.clone() as HashSet<GridPosition>
        return cp
    }

    override fun next(actions: IntArray): AbstractGameState {
        // if already finished then don't update anything, just return
        if (terminal) return this
        val proposed = gridPosition.move(actions[0])
        // if the proposal is to move to a navigable cell then accept the move
        with(proposed) {
            if (simpleGrid.getCell(x, y) == navChar) gridPosition = proposed
            // remove the subgoal
            if (removeSubgoalsWhenVisited) subgoals.remove(gridPosition)
        }
        if (isTerminal()) terminal = true
        nTicks++
        totalTicks++
        return this
    }

    override fun nActions(): Int {
        return 4
    }

    override fun score(): Double {
        return atGoalScore() -
                tickWeight * nTicks -
                distanceWeight * distanceScore() -
                subgoalWeight * subgoals.size

//         return distanceScore()
    }


    fun distanceScore(): Double {
        return goal.getVec().gridDistanceTo(gridPosition.getVec())
    }

    fun atGoalScore(): Double = if (gridPosition == goal) 1.0 else 0.0

    override fun isTerminal(): Boolean {
        return gridPosition == goal || nTicks >= maxTicks
    }

    override fun nTicks(): Int {
        return nTicks
    }
}

class GridWorldView(val w: Int, val h: Int, val cellSize: Int = 20) {
    val view = EasyDraw()

    fun update(playouts: ArrayList<IntArray>, gw: GridWorld) {
        // println("Updating ${playouts.size} playouts on GRID" )
        val lineColor = Color(155, 0, 128, 200)
        // lineColor.se

        for (seq in playouts) {
            val state = gw.copy() as GridWorld
            val points = ArrayList<Vec2d>()
            for (s in seq) {
                state.next(intArrayOf(s))
                points.add(state.gridPosition.getVec() * cellSize.toDouble() + Vec2d(cellSize / 2.0, cellSize / 2.0))
            }
            drawList.add(PolyDraw(points, null, lineColor, false))
        }
    }

    var drawList = ArrayList<Drawable>()

    fun update(gridWorld: GridWorld) : GridWorldView {

        // add in the grid lines
        drawList = ArrayList<Drawable>()

        with(gridWorld.simpleGrid) {
            drawList.add(CellDraw(0.0, 0.0, w.toDouble() * cellSize, h.toDouble() * cellSize, Color.orange, null))
        }

        with(gridWorld.simpleGrid) {
            drawList.add(GridLines(w, h, cellSize, cellSize))
        }

        with(gridWorld.simpleGrid) {
            for (i in 0 until w) {
                for (j in 0 until h) {
                    if (getCell(i, j) == wallChar) {
                        drawList.add(CellDraw(i * cellSize.toDouble(), j * cellSize.toDouble(),
                                cellSize.toDouble(), cellSize.toDouble(), Color.black, null))
                    }
                }
            }
        }

        for (s in gridWorld.subgoals) {
            drawList.add(Ellipse(s.x.toDouble() * cellSize, s.y.toDouble() * cellSize, cellSize.toDouble(), cellSize.toDouble(), Color.cyan, Color.blue))
        }

        with(gridWorld.gridPosition) {
            drawList.add(Ellipse(x.toDouble() * cellSize, y.toDouble() * cellSize, cellSize.toDouble(), cellSize.toDouble(), Color.blue, null))
        }

        with(gridWorld.goal) {
            val poly = arrayListOf<Vec2d>(
                    Vec2d(cellSize * .5, 0.0),
                    Vec2d(cellSize.toDouble(), cellSize * .5),
                    Vec2d(cellSize * .5, cellSize.toDouble()),
                    Vec2d(0.0, cellSize * .5)
            )
            val pd = PolyDraw(poly, Color.red, null)
            pd.s = Vec2d(x.toDouble() * cellSize, y.toDouble() * cellSize)
            drawList.add(pd)
        }
        return this
    }

    fun repaint() {
        view.drawable = drawList
        view.repaint()
    }
}
