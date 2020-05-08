package games.tetris

import math.Vec2d
import kotlin.math.PI

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
