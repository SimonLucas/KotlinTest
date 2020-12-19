package test.builder

fun main() {
    val args = arrayListOf<String>("Hello HTML Builder", "It works", "Another line")
    println(result(args, "HTML in Kotlin"))
}

fun result(args: ArrayList<String>, title: String) =
        html {
            head {
                title {+ title}
            }
            body {
                h1 {+ title}
                p  {+"this format can be used as an alternative markup to XML"}

                // an element with attributes and text content
                a(href = "http://kotlinlang.org") {+"Kotlin"}

                // mixed content
                p {
                    +"This is some"
                    b {+"mixed"}
                    +"text. For more see the"
                    a(href = "http://kotlinlang.org") {+"Kotlin"}
                    +"project"
                }
                p {+"some text"}

                // content generated by
                p {
                    for (arg in args)
                        +arg
                }
            }
        }
