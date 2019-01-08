package utilities

import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JFrame
import javax.swing.WindowConstants

class JEasyFrame(var comp: Component, title: String) : JFrame(title) {
    init {
        contentPane.add(BorderLayout.CENTER, comp)
        pack()
        this.isVisible = true
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        repaint()
    }
}
