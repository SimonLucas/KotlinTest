package games.gridgame

import agents.SimpleEvoAgent
import ggi.AbstractGameState
import ggi.ExtendedAbstractGameState
import utilities.JEasyFrame
import views.GridView
import java.util.*

// started at 20:44

val random = Random()

object Constants {
    val player1 = 0
    val player2 = 1
    val playerValues = intArrayOf(player1, player2)
    val on: Int = 1
    val off: Int = 0
    val outside: Int = 0
    // val range = 0..1
}

data class Grid(val w: Int = 20, val h: Int = 20) {
    var grid: IntArray = IntArray(w * h, { random.nextInt(2) })

    fun getCell(i: Int): Int = grid[i]

    fun setCell(i: Int, v: Int) {
        grid[i] = v
    }

    fun invertCell(i: Int) {
        grid[i] = 1 - grid[i]
    }

    fun getCell(x: Int, y: Int): Int {
        if (x < 0 || y < 0 || x >= w || y >= h) return Constants.outside
        return grid[x + w * y]
    }

    fun setCell(x: Int, y: Int, value: Int) {
        if (x < 0 || y < 0 || x >= w || y >= h) return
        grid[x + w * y] = value
    }

    init {

        // println(grid)


    }

    fun deepCopy() : Grid {
        val gc = this.copy()
        gc.grid = grid.copyOf()
        return gc
    }

}

var totalTicks: Long = 0

class GridGame : ExtendedAbstractGameState {

    val updateRule = MyRule()
    var grid: Grid = Grid()
    var nTicks = 0

    constructor(w:Int=20, h:Int=20) {
        grid = Grid(w,h)
    }

    override fun copy(): AbstractGameState {
        val gridGame = GridGame()
        gridGame.nTicks = nTicks
        gridGame.grid = grid.deepCopy()
        return gridGame
    }

    override fun next(actions: IntArray, playerId: Int): AbstractGameState {

//        val p1Action = actions[0]
//        val p2Action = actions[1]
//        // if both players choose the same action then do nothing
//
//        // otherwise invert at each position - or if inverting then no need to ignore
//        grid.invertCell(p1Action)
//        grid.invertCell(p2Action)


        val gridCopy = grid.copy()

        for (i in 0 until grid.w) {
            for (j in 0 until grid.h) {
                gridCopy.setCell(i, j, updateRule.cellUpdate(grid, i, j))
            }
        }
        grid = gridCopy
        grid.invertCell(actions[playerId])

        totalTicks++
        nTicks++
        return this
    }

    override fun nActions(): Int {
        return grid.grid.size
    }

    override fun score(): Double {
        return -grid.grid.sum().toDouble()
    }

    override fun isTerminal(): Boolean {
        // for now let this never end!
        return false
    }

    override fun nTicks(): Int {
        return nTicks
    }

    override fun totalTicks(): Long {
        return totalTicks
    }

    override fun resetTotalTicks() {
        totalTicks = 0;
    }

}


interface UpdateRule {
    fun cellUpdate(grid: Grid, x: Int, y: Int): Int
}

class MyRule : UpdateRule {

    fun sumFun(sum: Int): Int {
        return if (sum < 3 || sum > 4) 0 else 1
    }

    override fun cellUpdate(grid: Grid, x: Int, y: Int): Int {
        var sum = 0
        for (xx in x - 1..x + 1) {
            for (yy in y - 1..y + 1) {
                sum += grid.getCell(xx, yy)

            }
        }
        return sumFun(sum)
    }
}

fun main(args: Array<String>) {
    val game = GridGame(25,25)
    println(game.grid)
    val gv = GridView(game)
    val frame = JEasyFrame(gv, "Life Game")
    val actions = intArrayOf(0, 0)
    var agent = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 5, nEvals = 40)

    while (true) {
        actions[0] = agent.getAction(game.copy(), Constants.player1)
        game.next(actions, 0)
        gv.repaint()
        Thread.sleep(100)
        frame.title = "tick = ${game.nTicks}, score = ${game.score()}"
    }
}

