package test

import utilities.JEasyFrame
import java.awt.BorderLayout
import java.awt.TextArea
import java.util.*
import javax.swing.JComponent
import kotlin.collections.HashSet

fun main() {


    JEasyFrame(Main(), "Uniqueify")




}

class Main : JComponent() {
    val textArea = TextArea()
    val output = TextArea()
    init {
        layout = BorderLayout()
        add(textArea, BorderLayout.NORTH)
        add(output, BorderLayout.SOUTH)
        textArea.addTextListener { e -> run {
            output.text = uniqueWords(textArea.text)
        } }
    }

    private fun uniqueWords(text: String?): String {
        if (text == null) return "No words"

        val list = ArrayList<String>()
        val set = HashSet<String>()
        val scanner = Scanner(text)
        scanner.useDelimiter("\n")
        while (scanner.hasNext()) {
            val str = scanner.next()
            if (str != null) {
                if (!set.contains(str)) {
                    set.add(str)
                    list.add(str)
                }
            }
        }
        val words = StringBuffer()
        for (w in list) {
            words.append("$w\n")
        }
        return words.toString()

    }

}
