package utilities

class ElapsedTimer {
    // allows for easy reporting of elapsed time
    internal var oldTime: Long = 0

    init {
        oldTime = System.currentTimeMillis()
    }

    fun elapsed(): Long {
        return System.currentTimeMillis() - oldTime
    }

    fun reset() {
        oldTime = System.currentTimeMillis()
    }

    override fun toString(): String {
        // now resets the timer...
        val ret = elapsed().toString() + " ms elapsed"
        reset()
        return ret
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val t = ElapsedTimer()
            println("ms elasped: " + t.elapsed())
        }
    }
}


