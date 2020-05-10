package games.tetris

import utilities.JEasyFrame
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent

fun main() {
    Controller()
}

class Controller : Thread() {
    var count = 0
    var tm: TetrisModel
    var tv: TetrisView
    var gameOn = false
    var delay = 0
    fun newGame() {
        // initialise the model

        // note that there are two distinct
        gameOn = true
        delay = maxDelay
        start()
    }

    override fun run() {
        // this runs in the thread that's responsible for
        // moving the pieces down
        while (gameOn) {
            // try and place a new piece if required
            try {
                sleep(delays[delayState].toLong())
            } catch (e: Exception) {
            }
            if (tm.tetronSprite == null) {
                gameOn = tm.newShape()
                count++
                delayState = count / 10 % 2
            } else {
                if (!tm.move(0, down)) {
                    tm.place()
                    tm.checkRows()
                    tm.tetronSprite = null
                }
            }
            tv.repaint()
        }
        println("Game Over!")
    }

    fun handleKey(key: Int) {
        // System.out.println("Handling: " + key + " : " + KeyEvent.VK_RIGHT);
        when (key) {
            KeyEvent.VK_RIGHT -> tm.move(xRight, 0)
            KeyEvent.VK_LEFT -> tm.move(xLeft, 0)
            KeyEvent.VK_UP -> tm.rotate()
            KeyEvent.VK_SPACE -> dropDown()
            KeyEvent.VK_ENTER -> dropDown()
            KeyEvent.VK_DOWN -> tm.move(0, down)
            else -> {
                println("Unhandled Key Event")
            }
        }
        tv.repaint()
    }

    private fun dropDown() {
        while (tm.move(0, down));
        tm.place()
    }

    internal class Keys(private val c: Controller) : KeyAdapter() {
        override fun keyPressed(k: KeyEvent) {
            // System.out.println(k);
            c.handleKey(k.keyCode)
        }

    }

    fun fastMode() : Boolean {
        return delayState != 1
    }

    companion object {
        var maxDelay = 300
        var minDelay = 200
        var delays = intArrayOf(minDelay, maxDelay)
        var delayState = 1
        var nRows = 25
        var nCols = 13
        var xLeft = -1
        var xRight = 1
        var down = 1
    }

    init {
        tm = TetrisModel(nCols, nRows)
        tv = TetrisView(nCols, nRows)
        // tv.addKeyListener(new Keys(this));
        val frame = JEasyFrame(tv, "Tetris")
        frame.addKeyListener(Keys(this))
        newGame()
    }
}
