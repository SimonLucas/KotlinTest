package test

import utilities.JEasyFrame
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent

fun main() {
    JEasyFrame(MouseEventTest(), "MouseEventTest")
}

class MouseEventTest : JComponent() {

    init {
        addMouseListener(
                object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent?) {
                        super.mouseClicked(e)
                        if (e != null) process(e.x, e.y)
                    }
                }
        )
    }

    fun process(x: Int, y: Int) {
        println("($x, $y)")
    }

    override fun paintComponent(g: Graphics) {
        g.color = Color.getHSBColor(0.2f, 0.5f, 1.0f)
        g.fillRect(0, 0, width, height)
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(500, 500)
    }
}