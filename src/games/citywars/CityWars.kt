package games.citywars

import ggi.AbstractGameState
import ggi.ExtendedAbstractGameState
import java.util.*

val random = Random()



data class Grid(val w: Int = 15, val h: Int = 7, var grid: IntArray) {

    fun randomGrid() = IntArray(w * h, { games.gridgame.random.nextInt(2) })

    fun setAll (v: Int) {grid.fill(v)}

    fun getCell(i: Int): Int = grid[i]

    fun setCell(i: Int, v: Int) {
        grid[i] = v
    }

    fun invertCell(i: Int) {
        grid[i] = 1 - grid[i]
    }

    fun getCell(x: Int, y: Int): Int {
//        if (!wrap)
//            if (x < 0 || y < 0 || x >= w || y >= h) return Constants.outside
        val xx = (x + w) % w
        val yy = (y + h) % h
        return grid[xx + w * yy]
    }

    fun setCell(x: Int, y: Int, value: Int) {
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

    fun getWidth() : Int {
        return this.w
    }

    fun getHeight() : Int {
        return this.h
    }


    init {
    }

    fun deepCopy(): Grid {
        val gc = this.copy()
        gc.grid = grid.copyOf()
        return gc
    }

    fun inLimits(x: Int, y: Int): Boolean {
        return ! (x < 0 || x >= w || y < 0 || y > h)
    }

    fun exchange (x: Int, y: Int, x2: Int, y2: Int) {
        var c1 = getCell(x, y)
        var c2 = getCell(x2, y2)
        setCell(x,y,c2)
        setCell(x2,y2,c1)
    }
}


var totalTicks: Long = 0

val NIL: Int = 0
val UP: Int = 1
val RIGHT: Int = 2
val DOWN: Int = 3
val LEFT: Int = 4
val ACTIONS: IntArray = intArrayOf(NIL, UP, RIGHT, DOWN, LEFT)


open class CityWars : ExtendedAbstractGameState {


    var board : Grid = Grid(10, 10, getGrid())
    var troops : Grid = Grid(10, 10, getUnits(10))


    fun getGrid() : IntArray
    {
        //0: empty, 1: city, 2: obstacle
        var level: String =     "0000000000" +
                                "0000000000" +
                                "0000000000" +
                                "0001020100" +
                                "0000000000" +
                                "0000020000" +
                                "0000000000" +
                                "0001020100" +
                                "0000000000" +
                                "0000000000"

        var listGrid : List<Int> = level.map { it.toString().toInt() }
        return listGrid.toIntArray()
    }

    fun getUnits(w : Int) : IntArray
    {
        var listUnits : IntArray = IntArray(4)

        //x=3, y=2
        listUnits[3 + w * 3] = 50
        listUnits[3 + w * 7] = 50

        listUnits[7 + w * 3] = -50
        listUnits[7 + w * 7] = -50

        return listUnits
    }


    var nTicks = 0


    override fun next(actions: IntArray): AbstractGameState {

        var player0Action : Int = actions[0]
//
//        if(player0Action != NIL)
//        {
//            when(player0Action) {
//                UP -> move(intArrayOf(0, -1))
//                RIGHT -> move(intArrayOf(1, 0))
//                DOWN -> move(intArrayOf(0, 1))
//                LEFT -> move(intArrayOf(-1,0))
//                else -> println("INVALID ACTION: " + player0Action)
//            }
//        }

        totalTicks++
        nTicks++
        return this
    }

//    fun move(dir : IntArray)
//    {
//        var nextX : Int = board.playerX + dir[0]
//        var nextY : Int = board.playerY + dir[1]
//
//        //Check board limits
//        if (! board.inLimits(nextX, nextY) )
//            return
//
//        var destCell : Char = board.getCell(nextX, nextY)
//
////        println("Moving into: " + destCell)
//
//        when(destCell) {
//            board.WALL -> return        //Moves against walls
//            board.BOXIN ->  {
//                //println("BOXIN")
//                return
//            }//return       //Moves against box in place (change this for different versions of Sokoban)
//            board.EMPTY -> {            //Move with no obstacle, ALLOWED
//                //Empty, we move player at the end.
//            }
//            board.HOLE -> {           //Move to a hole, ALLOWED
//                //Empty, we move player at the end.
//            }
//            board.BOX ->                //GOOD MOVE?
//            {
//                //Against a box. Will move if empty on the other side.
//                var forwardX : Int = nextX + dir[0]
//                var forwardY : Int = nextY + dir[1]
//                if (! board.inLimits(forwardX, forwardY) ) //Pushing against outside of board, do nothing.
//                    return
//
//                var forwardCell : Char = board.getCell(forwardX, forwardY)
//                when(forwardCell)
//                {
//                    board.WALL -> return        //Moves against walls
//                    board.BOXIN -> return       //Moves against box in place (change this for different versions of Sokoban)
//                    board.BOX -> return         //Push against a BOX, we don't forward the push
//                    board.EMPTY -> {            //PROGRESS! (I hope)
//                           board.exchange(nextX, nextY, forwardX, forwardY)
//                    }
//                    board.HOLE -> {             //EUREKA!
//                        board.setCell(nextX, nextY, board.EMPTY)
//                        board.setCell(forwardX, forwardY, board.BOXIN)
//                    }
//                }
//            }
//        }
//
//        board.playerX = nextX
//        board.playerY = nextY
//    }

    override fun nActions(): Int {
        return 10 * 10 * 4 * 100
    }

    override fun score(): Double {
        return 0.0 //board.count('+').toDouble()
    }

    override fun isTerminal(): Boolean {
        return false// board.count('*') == 0
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
        val sokobanCopy = CityWars()
        sokobanCopy.nTicks = nTicks
        sokobanCopy.board = board.deepCopy()
        return sokobanCopy
    }

    fun print() {
        //board.print()
        //println("Score: " + score() + ", terminal: " + isTerminal())
    }
}

fun main(args: Array<String>) {
    var cityWars : CityWars = CityWars()
    cityWars.print()
}