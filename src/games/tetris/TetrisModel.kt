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
        var dropSkip = 10
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

