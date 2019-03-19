package games.citywars

import ggi.AbstractGameState
import ggi.ExtendedAbstractGameState
import java.util.*
import kotlin.math.absoluteValue

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

    fun print(lines : Boolean = true) {
        for (i in 0 until grid.size) {

            print(grid[i])

            if (lines && (i + 1) % w == 0)
                println()

        }
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

object Constants {
    val player1 = 0
    val player2 = 1
    val playerValues = intArrayOf(player1, player2)
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
    var troops : Grid = Grid(10, 10, getUnits(10, 10))

    val city_increment = 1
    val max_city = 100
    val empty = 0
    val city = 1
    val wall = 2


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

    fun getUnits(w : Int, h : Int) : IntArray
    {
        var listUnits : IntArray = IntArray(w*h)

        //x=3, y=2
        listUnits[3 + w * 3] = 50
        listUnits[3 + w * 7] = 50

        listUnits[7 + w * 3] = -50
        listUnits[7 + w * 7] = -50

        return listUnits
    }


    var nTicks = 0

    //fun doNothingAction() = intArrayOf(NIL,NIL)
    fun doNothingAction() = NIL

    override fun next(actions: IntArray): AbstractGameState {

        //Player actions
        for( playerID : Int in Constants.playerValues) {

            var playerAction: Int = actions[playerID]

            //correct for IDs
            playerAction += 10000

            var actionString: String = playerAction.toString()

            var dir: Int = Character.getNumericValue(actionString[0])
            var x: Int = Character.getNumericValue(actionString[1])
            var y: Int = Character.getNumericValue(actionString[2])
            var perc: Int = actionString.substring(3).toInt() + 1

            //println("ACTION: " + dir + " " + x + " " + y + " " + perc)

            var troop: Int = troops.getCell(x, y)

            var myTroops: Boolean = (playerID == 0 && troop > 0) || (playerID == 1 && troop < 0)
            if (myTroops) {
                //There's something to move here.
                when (dir) {
                    UP -> move(x, y, intArrayOf(0, -1), troop, perc, playerID)
                    RIGHT -> move(x, y, intArrayOf(1, 0), troop, perc, playerID)
                    DOWN -> move(x, y, intArrayOf(0, 1), troop, perc, playerID)
                    LEFT -> move(x, y, intArrayOf(-1, 0), troop, perc, playerID)
                    else -> println("INVALID ACTION: " + playerAction)
                }
            }

            totalTicks++
            nTicks++
        }

        //City actions: increment of city_increment per city if populated
        for (i in 0 until board.grid.size) {

            var cell : Int = board.grid[i]
            if (cell == city)
            {
                var troopsInCity : Int = troops.getCell(i)
                if (troopsInCity > 0)
                {
                    var nextTroops = troopsInCity + city_increment
                    if (nextTroops >= max_city)
                        nextTroops = 0
                    troops.setCell(i, nextTroops)

                }else if(troopsInCity < 0) {
                    var nextTroops = troopsInCity - city_increment
                    if (nextTroops <= -max_city)
                        nextTroops = 0
                    troops.setCell(i, nextTroops)
                }
            }

        }


        return this
    }

    fun move(x : Int, y: Int, dir : IntArray, troop : Int, perc : Int, playerID: Int)
    {
        var nextX : Int = x + dir[0]
        var nextY : Int = y + dir[1]
        if(board.inLimits(nextX, nextY))
        {
            var dest = board.getCell(nextX, nextY)
            var troopsToMove : Int = (troop * perc / 100.0).toInt()

            if( dest != wall) {
                troops.setCell(x,y, troops.getCell(x,y) - troopsToMove)
                troops.setCell(nextX,nextY, troops.getCell(nextX,nextY) + troopsToMove)
            }
        }

    }


    override fun nActions(): Int {
        return 40000
    }

    override fun score(): Double {
        return troops.grid.sum().toDouble();
        //return 0.0 //board.count('+').toDouble()
    }

    override fun isTerminal(): Boolean {
        return troops.grid.all( { i-> i >= 0} ) || troops.grid.all( { i-> i <= 0} );
        //return false// board.count('*') == 0
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
        board = Grid(10, 10, getGrid())
        troops = Grid(10, 10, getUnits(10, 10))
        return this
    }

    override fun copy(): AbstractGameState {
        val cityWarsCopy = CityWars()
        cityWarsCopy.nTicks = nTicks
        cityWarsCopy.troops = troops.deepCopy()
        cityWarsCopy.board = board
        return cityWarsCopy
    }

    fun print() {
        board.print()
        troops.print()
        println("Score: " + score() + ", terminal: " + isTerminal())
    }

    fun report()
    {
        print ("{ \"cities\": [")
        for (i in 0 until board.grid.size) {

            if(board.grid[i] == city)
                print("1" + ",")
            else
                print("0" + ",")
        }
        println("]")


        print ("\"player0\": [")
        for (i in 0 until troops.grid.size) {

            if(troops.grid[i] > 0)
                print(troops.grid[i].toString() + ",")
            else
                print("0,")
        }
        println("]")

        print ("\"player1\": [")
        for (i in 0 until troops.grid.size) {

            if(troops.grid[i] < 0)
                print((troops.grid[i].absoluteValue.toString() + ","))
            else
                print("0,")
        }
        println("]")


        print ("\"obstacles\": [")
        for (i in 0 until board.grid.size) {

            if(board.grid[i] == wall)
                print("1" + ",")
            else
                print("0" + ",")
        }
        println("]")

        var level: String =     "0000000000" +
                                "0000000000" +
                                "0000000000" +
                                "0000000000" +
                                "0001111100" +
                                "0000000100" +
                                "0000000100" +
                                "0000000100" +
                                "0000000000" +
                                "0000000000"
        print ("\"trajectory\": [")
        for (i in 0 until level.length) {
            print(level[i] + ",")
        }
        println("]")

        println ("\"arrival\": 0.4 }")

//        board.print(false)
//        troops.print(false)

    }

}

fun main(args: Array<String>) {
    var cityWars : CityWars = CityWars()
    cityWars.print()
    cityWars.next(intArrayOf(13349, 17349)) //RIGHT, (3,3), 50%  +    LEFT, (7,3), 50%
    //cityWars.next(intArrayOf()) //
    cityWars.print()
    cityWars.next(intArrayOf(14319, 16319))
    cityWars.print()
}