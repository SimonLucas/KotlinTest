package test

fun main(args: Array<String>) {
    with(StringBuilder()) {
        append("content: ")
        append(javaClass.canonicalName)
    }
}