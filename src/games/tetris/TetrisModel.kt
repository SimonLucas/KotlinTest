package games.tetris

import games.tetris.TetrisConstants.BG
import ggi.ExtendedAbstractGameState
import math.Vec2d
import kotlin.math.PI

fun main() {
    val model = TetrisModel()
    println(model.gameOn())
}


class TetrisModel(var nCols: Int = defaultCols, var nRows: Int = defaultRows) {
    val a = Array(nCols) { IntArray(nRows) { BG } };
    var score = 0
    var tetronSprite: TetronSprite? = null

    // the blockCount is used here for a simple deterministic version of the
    // game which is useful for debugging and comparison
    // or maybe better to use the one in TetrisGame instead...
    var blockCount = 0

    init {
        if (tetronSprite == null) newShape()
    }

    fun copy() : TetrisModel {
        val tm = TetrisModel(nCols, nRows)
        tm.score = score
        tm.blockCount = blockCount
        tm.tetronSprite = tetronSprite?.copy()
        for (i in 0 until nCols)
            for (j in 0 until nRows)
                tm.a[i][j] = a[i][j]
        return tm
    }

    // this is called after every accepted move
    fun checkRows(): Boolean {
        var flag = false
        var r = 0
        while (r < nRows) {
            if (full(r)) {
                // pling();
                flag = true
                // this gives higher scores for rows popped higher up
                // who dares wins
                score += baseReward // + heightFactor * (nRows - r)
                clearRow(r)
                scroll(r)
                r--
            }
            r++
        }
        return flag
    }

    private fun full(r: Int): Boolean {
        for (i in 0 until nCols) {
            if (a[i][r] == BG) {
                return false
            }
        }
        return true
    }

    private fun clearRow(r: Int) {
        for (i in 0 until nCols) {
            a[i][r] = BG
        }
    }

    private fun scroll(sr: Int) {
        for (r in sr downTo 1)
            for (c in 0 until nCols)
                a[c][r] = a[c][r - 1]
    }

    fun move(dx: Int, dy: Int): Boolean {
        val ts = tetronSprite
        return if (ts != null) {
            ts.move(dx, dy, a)
        } else false
    }

    fun rotate() {
        val ts = tetronSprite
        if (ts != null) ts.rotate(a)
    }

    fun place() {
        val ts = tetronSprite
        if (ts != null) {
            ts.place(a)
            // justLanded = true
        }
    }

    fun gameOn() : Boolean {
        val ts = tetronSprite
        return (ts != null && ts.valid(a))
    }

    fun newShape(): Boolean {
        // create a shape in the correct place and then check for it's
        // validity

        val tType =
                if (cyclicBlockType) {
                    blockCount % Tetrons.shapes.size
                }
                 else {
                    rand.nextInt(Tetrons.shapes.size)
                }
        // increment the block count whether we use it to determine block type or not
        blockCount++
        val tColor = if (randomShapeColours) rand.nextInt(Tetrons.shapes.size) else tType
        val x = (nCols / 2) -1
        val y = 2
        val rotation = if (randomInitialRotation) rand.nextInt(4) else 0
        val ts = TetronSprite(x, y, rotation, tType, tColor)
        if (ts.valid(a)) {
            tetronSprite = ts
            return true
        } else {
            tetronSprite = null
            return false
        }
    }

    // the ghost block plays no part in the functional gameplay
    // but is useful to show human (or vision-based)
    // players where the block would fall
    // if dropped
    fun getGhost(): TetronSprite? {
        val ts = tetronSprite
        if (ts != null) {
            val ghost = ts.copy()
            while (ghost.move(0, 1, a));
            return ghost
        } else return null
    }

    companion object {
        // var BG = 0
        val rand = kotlin.random.Random
        var baseReward = 100
        var heightFactor = 100
        var defaultRows = 24
        var defaultCols = 8
        var randomShapeColours = false
        var cyclicBlockType = true
        var randomInitialRotation = false
        var includeColumnDiffs = true
        var gameOverPenalty = 0
    }
}

typealias Tetron = Array<Cell>

object TetrisConstants {
    var BG = Tetrons.shapes.size
    var baseReward = 100
    var heightFactor = 100
    var defaultRows = 30
    var defaultCols = 60
}

object Tetrons {

    val square = arrayOf(
            arrayOf(Cell(0, 0), Cell(0, 1), Cell(1, 0), Cell(1, 1))
    )
    val tee = arrayOf(
            arrayOf(Cell(0, 0), Cell(1, 0), Cell(0, -1), Cell(0, 1)),
            arrayOf(Cell(0, 0), Cell(-1, 0), Cell(1, 0), Cell(0, 1)),
            arrayOf(Cell(0, 0), Cell(-1, 0), Cell(0, -1), Cell(0, 1)),
            arrayOf(Cell(0, 0), Cell(1, 0), Cell(0, -1), Cell(-1, 0))
    )
    val straight = arrayOf(
            arrayOf(Cell(0, -1), Cell(0, 0), Cell(0, 1), Cell(0, 2)),
            arrayOf(Cell(-1, 0), Cell(0, 0), Cell(1, 0), Cell(2, 0))
    )

    val lCorn = arrayOf(
            arrayOf(Cell(1, -1), Cell(0, -1), Cell(0, 0), Cell(0, 1)),
            arrayOf(Cell(-1, 0), Cell(0, 0), Cell(1, 0), Cell(1, 1)),
            arrayOf(Cell(0, 1), Cell(1, -1), Cell(1, 0), Cell(1, 1)),
            arrayOf(Cell(-1, 0), Cell(0, 0), Cell(1, 0), Cell(-1, -1))
    )

    val rCorn = arrayOf(
            arrayOf(Cell(1, 1), Cell(0, -1), Cell(0, 0), Cell(0, 1)),
            arrayOf(Cell(-1, 0), Cell(0, 0), Cell(1, 0), Cell(-1, 1)),
            arrayOf(Cell(0, -1), Cell(1, -1), Cell(1, 0), Cell(1, 1)),
            arrayOf(Cell(-1, 0), Cell(0, 0), Cell(1, 0), Cell(1, -1))
    )

    val lSkew = arrayOf(
            arrayOf(Cell(-1, 1), Cell(0, 1), Cell(0, 0), Cell(1, 0)),
            arrayOf(Cell(0, -1), Cell(0, 0), Cell(1, 0), Cell(1, 1)) )

    val rSkew = arrayOf(
            arrayOf(Cell(-1, 0), Cell(0, 0), Cell(0, 1), Cell(1, 1)),
            arrayOf(Cell(0, 1), Cell(0, 0), Cell(1, 0), Cell(1, -1)) )


    val shapes = arrayOf(square, lCorn, rCorn, straight, rSkew, lSkew, tee)
    // val shapes = arrayOf(lSkew, rSkew)

    fun getShape(index: Int, rot: Int): Tetron {
        val n = shapes[index].size
        return shapes[index][rot % n]
    }

    // fun randShape(): Tetron = shapes[rand.nextInt(shapes.size)]

}

data class TetronSprite(var x: Int, var y: Int, var rot: Int, var tetron: Int, var color: Int) {

    fun move(dx: Int, dy: Int, a: Array<IntArray>): Boolean {
        // try the move, but undo it if it fails
        x += dx
        y += dy
        return if (valid(a)) {
            true
        } else {
            x -= dx
            y -= dy
            false
        }
    }

    fun rotate(a: Array<IntArray>): Boolean {
        // make the rotation
        rot++
        return if (valid(a)) {
            // System.out.println("rotated");
            true
        } else {
            // undo rotation
            // System.out.println("un rotated");
            rot--
            false
        }
    }

    private fun outOfBounds(a: Array<IntArray>, cell: Cell): Boolean {
        // current piece location is given by x,y
        return cell.x < 0 || cell.y < 0 || cell.x >= a.size || cell.y >= a[0].size
    }

    fun valid(a: Array<IntArray>): Boolean {
        val theta = (rot % 4) * PI / 2
        for (c in Tetrons.getShape(tetron, rot)) {
            val cell = translate(c)
            if (outOfBounds(a, cell)) return false
            if (a[cell.x][cell.y] != TetrisConstants.BG) return false
        }
        // passed all checks - a valid place to put the Tetronimo
        return true
    }

    fun getCells(): ArrayList<Cell> {
        val cells = ArrayList<Cell>()
        for (cell in Tetrons.getShape(tetron, rot))
            cells.add(translate(cell))
        return cells
    }

    // fun cell(p: Vec2d) = Cell(Math.round(p.x).toInt(), Math.round(p.y).toInt())
    fun cell(p: Vec2d) = Cell(p.x.toInt(), p.y.toInt())

    fun place(a: Array<IntArray>) {
        for (c in Tetrons.getShape(tetron, rot)) {
            val cell = translate(c)
            a[cell.x][cell.y] = color
        }
    }

    fun translate(cell: Cell) = Cell(x + cell.x, y + cell.y)

//    fun transform(v: Vec2d, theta: Double): Cell {
//        // transform the core position of the specified block type in a tetronimo based on
//        // the current location and rotation
//        val rotated = v.rotatedBy(theta)
//        // now translate to current location
//        return Cell(rotated.x.toInt() + x, rotated.y.toInt() + y)
//    }
//

}

data class Cell(val x: Int, val y: Int)
