package games.sokoban

import ggi.AbstractGameState
import ggi.ExtendedAbstractGameState
import java.util.*

val random = Random()

data class Grid(val w: Int = 8, val h: Int = 7) {

    var playerX: Int = -1
    var playerY: Int = -1
    var grid: CharArray = readGrid()


    fun readGrid() : CharArray
    {
        var level: String =     "wwwwwwww" +
                                "ww.....w" +
                                "ww.o*o.w" +
                                "ww.*.*.w" +
                                "w..o*o.w" +
                                "w..A...w" +
                                "wwwwwwww"

        var arraygrid = CharArray(level.length)
        level.toCharArray(arraygrid)

        //Find player
        var playerLoc = arraygrid.indexOf('A')
        if (playerLoc == -1)
        {
            println("ERROR: No player in level")
        }else{
            playerX = playerLoc % w
            playerY = (playerLoc / h) - 1
            arraygrid.set(playerLoc, '.')
        }

        //Find boxes (count only for now?)


        return arraygrid
    }


    fun getCell(i: Int): Char = grid[i]

    fun setCell(i: Int, v: Char) {
        grid[i] = v
    }

    fun getCell(x: Int, y: Int): Char {
        val xx = (x + w) % w
        val yy = (y + h) % h
        return grid[xx + w * yy]
    }

    fun setCell(x: Int, y: Int, value: Char) {
        if (x < 0 || y < 0 || x >= w || y >= h) return
        grid[x + w * y] = value
    }

    fun difference (other: Grid) : Int {
        var tot = 0
        // lazily assume same dimensions...
        for (i in 0 until grid.size)
            tot += if (grid[i] == other.grid[i]) 0 else 1
        return tot
    }

    fun count (cObj : Char) : Int {
        var count: Int = 0
        for ( c : Char in grid) {
            if (c == cObj)
                count++
        }
        return count
    }

    fun print() {
        for (i in 0 until grid.size) {
            if ((i + 1) % w == 0)
                println(grid[i])
            else
                print(grid[i])
        }
        println("Player at: " + playerX + " " + playerY + "; " + count('*') + " boxes.")
    }

    init {

        print()

    }

    fun deepCopy(): Grid {
        val gc = this.copy()
        gc.grid = grid.copyOf()
        return gc
    }

}

var totalTicks: Long = 0

open class Sokoban : ExtendedAbstractGameState {

    var board : Grid = Grid()
    var nTicks = 0


    override fun next(actions: IntArray): AbstractGameState {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.


        games.simplegridgame.totalTicks++
        nTicks++

    }

    override fun nActions(): Int {
        return 5
    }

    override fun score(): Double {
        return board.count('+').toDouble()
    }

    override fun isTerminal(): Boolean {
        return board.count('*') == 0
    }

    override fun nTicks(): Int {
        return nTicks
    }

    override fun totalTicks(): Long {
        return totalTicks
    }

    override fun resetTotalTicks() {
        totalTicks = 0
    }

    override fun randomInitialState(): AbstractGameState {
        TODO("not gonna go down that rabit hole...") //To change body of created functions use File | Settings | File Templates.
    }

    override fun copy(): AbstractGameState {
        val sokobanCopy = Sokoban()
        sokobanCopy.nTicks = nTicks
        sokobanCopy.board = board.deepCopy()
        return sokobanCopy
    }

}

fun main(args: Array<String>) {
    var sokoban : Sokoban = Sokoban()
}