package utilities

class Picker<T> @JvmOverloads constructor(//
        internal var order: Int = MAX_FIRST) {

    var best: T? = null
        internal set
    var bestScore: Double? = null
        internal set
    var nItems: Int = 0

    init {
        reset()
    }

    fun add(score: Double, value: T) {
        // each value must be unique: keep it in the set of values
        // and throw an exception if violated

        if (best == null) {
            bestScore = score
            best = value
        } else {
            // System.out.println(order * score + " >? " + bestScore * order + " : " + (order * score > bestScore * order));
            if (order * score > bestScore!! * order) {
                bestScore = score
                best = value
            }
        }
        nItems++
    }

    fun reset() {
        nItems = 0
        bestScore = if (order == MAX_FIRST) java.lang.Double.NEGATIVE_INFINITY else java.lang.Double.POSITIVE_INFINITY
    }

    override fun toString(): String {
        return "Picker: $best : $bestScore"
    }

    companion object {
        // keeps just the best item so far

        @JvmStatic
        fun main(args: Array<String>) {
            val picker = Picker<Int>(Picker.MAX_FIRST)
            println(picker.best)
            picker.add(2.0, 1)
            picker.add(6.0, 2)
            picker.add(1.1, 3)
            picker.add(5.0, 0)
            println(picker)
        }

        var MAX_FIRST = 1
        var MIN_FIRST = -1
    }
}// boolean strict = true;
