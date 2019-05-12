package games.sokoban

import ggi.AbstractGameState
import ggi.ExtendedAbstractGameState
import java.util.*
import java.io.File

val random = Random()

data class Grid(val levelNo: Int = -1) : GridInterface {

    var playerX: Int = -1
    var playerY: Int = -1
    val EMPTY: Char = '.'
    val BOX: Char = '*'
    val HOLE: Char = 'o'
    val AVATAR: Char = 'A'
    val AVATARONHOLE: Char = 'u'
    val WALL: Char = 'w'
    val BOXIN: Char = '+'
    var w: Int = -1
    var h: Int = -1

    var grid: CharArray = readGrid()
    var nBoxes = 0
    var nBoxesIn = 0

    fun readGrid() : CharArray
    {
        val file = File("data/Sokoban/levels/level-"+levelNo+".txt")
        var lines:List<String> = file.readLines()
        val dims = lines[1].split(",")
        this.w = dims[0].toInt()
        this.h = dims[1].toInt()

        var level: String = ""
        for (l in 2..(lines.size-1))
            level += lines[l]

        var arraygrid = CharArray(level.length)
        level.toCharArray(arraygrid)

        setArray(arraygrid)


        return arraygrid
    }




    fun setArray(array: CharArray) : Grid {
        //Find player
        var playerLoc = array.indexOf(AVATAR)
        if (playerLoc == -1)
        {
            // println("ERROR: No player in level")
        } else {
            playerX = playerLoc % w
            playerY = playerLoc / w
            array.set(playerLoc, EMPTY)
        }

        grid = array
        return this
    }

    fun forceArray(array: CharArray) : Grid {
        var playerLoc = array.indexOf(AVATAR)
        if (playerLoc ==-1)
            playerLoc = array.indexOf(AVATARONHOLE)
        if (playerLoc == -1)
        {
            println("ERROR: No player in level")
        } else {
            playerX = playerLoc % w
            playerY = playerLoc / w
        }
        grid = array
        return this
    }

    fun getSimpleGrid() : SimpleGrid {
        val simpleGrid = SimpleGrid(w,h)
        simpleGrid.setGridKeepPlayerCell(grid.copyOf(), playerX, playerY)
        return simpleGrid
    }

    fun boxScore()
    {
        nBoxes--
        nBoxesIn++
    }

    fun boxScoreReverse()
    {
        nBoxes++
        nBoxesIn--
    }

    fun getCell(i: Int): Char = grid[i]

    fun setCell(i: Int, v: Char) {
        grid[i] = v
    }

    override fun getCell(x: Int, y: Int): Char {
        val xx = (x + w) % w
        val yy = (y + h) % h
        //println("x: $x; y: $y; xx: $xx; yy: $yy")
        return grid[xx + w * yy]
    }

    override fun setCell(x: Int, y: Int, value: Char) {
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

            if (playerX == i % w && playerY == (i / w)) // - 1)
                print(AVATAR)
            else
                print(grid[i])

            if ((i + 1) % w == 0)
                println()

        }
        println("Player at: " + playerX + " " + playerY + "; " + count(BOX) + " boxes")
    }

    override fun getWidth() : Int {
        return this.w
    }

    override fun getHeight() : Int {
        return this.h
    }

    init {
        //Count boxes
        nBoxes = count(BOX)
        nBoxesIn = count(BOXIN)  //Could be 0, but this is for levels with some already in (why not)
    }

    fun deepCopy(): Grid {
        val gc = this.copy()
        gc.grid = grid.copyOf()
        gc.playerX = playerX
        gc.playerY = playerY

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

object SokobanConstants
{
    val NIL: Int = 0
    val UP: Int = 1
    val RIGHT: Int = 2
    val DOWN: Int = 3
    val LEFT: Int = 4
    val ACTIONS: IntArray = intArrayOf(NIL, UP, RIGHT, DOWN, LEFT)
}

open class Sokoban(private var level : Int = -1) : ExtendedAbstractGameState {

    var board : Grid = Grid(level)
    var nTicks = 0

    override fun next(actions: IntArray): AbstractGameState {

        var playerAction : Int = actions[0]

        if(playerAction != SokobanConstants.NIL)
        {
            when(playerAction) {
                SokobanConstants.UP -> move(intArrayOf(0, -1))
                SokobanConstants.RIGHT -> move(intArrayOf(1, 0))
                SokobanConstants.DOWN -> move(intArrayOf(0, 1))
                SokobanConstants.LEFT -> move(intArrayOf(-1,0))
                else -> println("INVALID ACTION: " + playerAction)
            }
        }

        totalTicks++
        nTicks++
        return this
    }


    fun move(dir : IntArray)
    {
        var nextX : Int = board.playerX + dir[0]
        var nextY : Int = board.playerY + dir[1]

        //Check board limits
        if (! board.inLimits(nextX, nextY) )
            return

        var destCell : Char = board.getCell(nextX, nextY)

//        println("Moving into: " + destCell)

        when(destCell) {
            board.WALL -> return        //Moves against walls
            board.BOXIN ->  {
                //Against a box in a hole. Will move if empty on the other side.
                var forwardX : Int = nextX + dir[0]
                var forwardY : Int = nextY + dir[1]
                if (! board.inLimits(forwardX, forwardY) ) //Pushing against outside of board, do nothing.
                    return

                var forwardCell : Char = board.getCell(forwardX, forwardY)
                when(forwardCell)
                {
                    board.WALL -> return        //Moves against walls
                    board.BOXIN -> return       //Moves against box in place (change this for different versions of Sokoban)
                    board.BOX -> return         //Push against a BOX, we don't forward the push
                    board.EMPTY -> {            //PROGRESS! (I hope)
                        board.setCell(nextX, nextY, board.HOLE)
                        board.setCell(forwardX, forwardY, board.BOX)
                        board.boxScoreReverse() //Score goes down, box removed from hole
                    }
                    board.HOLE -> {             //EUREKA!
                        board.setCell(nextX, nextY, board.HOLE)
                        board.setCell(forwardX, forwardY, board.BOXIN)
                        //board.boxScore()  //No need to change the score, went from one hole to another
                    }
                }
            }//return       //Now also allows moving boxes that are already on targets (change this for different versions of Sokoban)
            board.EMPTY -> {            //Move with no obstacle, ALLOWED
                //Empty, we move player at the end.
            }
            board.HOLE -> {           //Move to a hole, ALLOWED
                //Empty, we move player at the end.
            }
            board.BOX ->                //GOOD MOVE?
            {
                //Against a box. Will move if empty on the other side.
                var forwardX : Int = nextX + dir[0]
                var forwardY : Int = nextY + dir[1]
                if (! board.inLimits(forwardX, forwardY) ) //Pushing against outside of board, do nothing.
                    return

                var forwardCell : Char = board.getCell(forwardX, forwardY)
                when(forwardCell)
                {
                    board.WALL -> return        //Moves against walls
                    board.BOXIN -> return       //Moves against box in place (change this for different versions of Sokoban)
                    board.BOX -> return         //Push against a BOX, we don't forward the push
                    board.EMPTY -> {            //PROGRESS! (I hope)
                           board.exchange(nextX, nextY, forwardX, forwardY)
                    }
                    board.HOLE -> {             //EUREKA!
                        board.setCell(nextX, nextY, board.EMPTY)
                        board.setCell(forwardX, forwardY, board.BOXIN)
                        board.boxScore()
                    }
                }
            }
        }

        board.playerX = nextX
        board.playerY = nextY
    }

    override fun nActions(): Int {
        return SokobanConstants.ACTIONS.size
    }

    override fun score(): Double {
        return board.nBoxesIn.toDouble()
    }

    override fun isTerminal(): Boolean {
        return board.nBoxes == 0
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
        val sokobanCopy = Sokoban(level)
        sokobanCopy.nTicks = nTicks
        sokobanCopy.board = board.deepCopy()
        return sokobanCopy
    }

    fun print() {
        board.print()
        println("Score: " + score() + ", terminal: " + isTerminal() +
                "; Player at x " + board.playerX + ", y: " + board.playerY)
    }
}

fun main(args: Array<String>) {
    var sokoban : Sokoban = Sokoban()
    sokoban.print()
    sokoban.next(intArrayOf(SokobanConstants.UP))

//    THIS FAILS TO SOLVE THE LEVEL
//    sokoban.print()
//    sokoban.next(intArrayOf(RIGHT))
//    sokoban.print()
//    sokoban.next(intArrayOf(DOWN))
//    sokoban.next(intArrayOf(RIGHT))
//    sokoban.next(intArrayOf(RIGHT))
//    sokoban.next(intArrayOf(UP))
//    sokoban.next(intArrayOf(UP))
//    sokoban.next(intArrayOf(UP))
//    sokoban.next(intArrayOf(LEFT))
//    sokoban.print()
//    sokoban.next(intArrayOf(DOWN))

//    THIS SOLVES THE LEVEL
//    sokoban.next(intArrayOf(SokobanConstants.UP))
//    sokoban.next(intArrayOf(SokobanConstants.UP))
//    sokoban.print()
//    sokoban.next(intArrayOf(SokobanConstants.DOWN))
//    sokoban.next(intArrayOf(SokobanConstants.DOWN))
//    sokoban.next(intArrayOf(SokobanConstants.RIGHT))
//    sokoban.next(intArrayOf(SokobanConstants.RIGHT))
//    sokoban.next(intArrayOf(SokobanConstants.UP))
//    sokoban.next(intArrayOf(SokobanConstants.UP))
//    sokoban.print()
//    sokoban.next(intArrayOf(SokobanConstants.DOWN))
//    sokoban.next(intArrayOf(SokobanConstants.LEFT))

//    var w:Int = 8
//    for (i in 0 until sokoban.board.grid.size){
//        println("> " + i + ": " + (i%w) + " " + (i/w))
//    }

    println("------")

    var W:Int = 8
    var H:Int = 7
    for (j in 0 until H)
        for (i in 0 until W)
            println("> " + i + "," + j + ": " + (i + W * j))


    sokoban.print()

}