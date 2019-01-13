package utilities

/**
 * This class is used to model the statistics
 * of a fix of numbers.  For the statistics
 * we choose here it is not necessary to store
 * all the numbers - just keeping a running total
 * of how many, the sum and the sum of the squares
 * is sufficient (plus max and min, for max and min).
 *
 *
 * This is a simpler version of StatisticalSummary that does
 * not include statistical tests, or the Watch class.
 */

class StatSummary @JvmOverloads constructor(// following line can cause prog to hang - bug in Java?
        // protected long serialVersionUID = new Double("-1490108905720833569").longValue();
        // protected long serialVersionUID = 123;
        var name: String? = "" // defaults to ""
) : Comparable<StatSummary> {
    private var sum: Double = 0.toDouble()
    private var sumsq: Double = 0.toDouble()
    private var min: Double = 0.toDouble()
    private var max: Double = 0.toDouble()

    private var mean: Double = 0.toDouble()
    private var sd: Double = 0.toDouble()


    private var strict = false

    // trick class loader into loading this now
    // private static StatisticalTests dummy = new StatisticalTests();

    internal var n: Int = 0
    internal var valid: Boolean = false

    init {
        n = 0
        sum = 0.0
        sumsq = 0.0
        // ensure that the first number to be
        // added will fix up min and max to
        // be that number
        min = java.lang.Double.POSITIVE_INFINITY
        max = java.lang.Double.NEGATIVE_INFINITY
        // System.out.println("Finished Creating SS");
        valid = false
    }// System.out.println("Creating SS");

    fun setStrict(strict: Boolean): StatSummary {
        this.strict = strict
        return this
    }

    fun reset() {
        n = 0
        sum = 0.0
        sumsq = 0.0
        // ensure that the first number to be
        // added will fix up min and max to
        // be that number
        min = java.lang.Double.POSITIVE_INFINITY
        max = java.lang.Double.NEGATIVE_INFINITY
    }

    fun max(): Double {
        if (strict && n < 1) throw RuntimeException(strictMessage)
        return max
    }

    fun min(): Double {
        if (strict && n < 1) throw RuntimeException(strictMessage)
        return min
    }

    fun mean(): Double {
        if (strict && n < 1) throw RuntimeException(strictMessage)
        if (!valid)
            computeStats()
        return mean
    }

    fun sum(): Double {
        if (strict && n < 1) throw RuntimeException(strictMessage)
        return sum
    }

    // returns the sum of the squares of the differences
    //  between the mean and the ith values
    fun sumSquareDiff(): Double {
        return sumsq - n.toDouble() * mean() * mean()
    }


    private fun computeStats() {
        if (!valid) {
            mean = sum / n
            var num = sumsq - n.toDouble() * mean * mean
            if (num < 0) {
                // avoids tiny negative numbers possible through imprecision
                num = 0.0
            }
            // System.out.println("Num = " + num);
            sd = Math.sqrt(num / (n - 1))
            // System.out.println(" GVGAISimpleTest: sd = " + sd);
            // System.out.println(" GVGAISimpleTest: n = " + n);
            valid = true
        }
    }

    fun sd(): Double {
        if (strict && n < 2) throw RuntimeException(strictMessage)
        if (!valid)
            computeStats()
        return sd
    }

    fun n(): Int {
        return n
    }

    fun stdErr(): Double {
        return sd() / Math.sqrt(n.toDouble())
    }

    fun add(ss: StatSummary): StatSummary {
        // implications for Watch?
        n += ss.n
        sum += ss.sum
        sumsq += ss.sumsq
        max = Math.max(max, ss.max)
        min = Math.min(min, ss.min)
        valid = false
        return this
    }

    fun add(d: Double): StatSummary {
        n++
        sum += d
        sumsq += d * d
        min = Math.min(min, d)
        max = Math.max(max, d)
        valid = false
        return this
    }

    // note: this method removes from mean and standard deviation
    // but cannot efficently adjudt min and max
    fun removeFromMean(d: Double) {
        if (n < 1) {
            n--
            sum -= d
            sumsq -= d * d
            valid = false
        }
    }

    fun add(n: Number): StatSummary {
        add(n.toDouble())
        return this
    }

    //    public void add(double[] d) {
    //        for (int i = 0; i < d.length; i++) {
    //            add(d[i]);
    //        }
    //    }
    //
    fun add(vararg xa: Double): StatSummary {
        for (x in xa) {
            add(x)
        }
        return this
    }

    fun add(xa: List<Double>): StatSummary {
        for (x in xa) {
            add(x)
        }
        return this
    }

    override fun toString(): String {
        var s = if (name == null) "" else name!! + "\n"
        s += " min = " + min() + "\n" +
                " max = " + max() + "\n" +
                " ave = " + mean() + "\n" +
                " sd  = " + sd() + "\n" +
                " se  = " + stdErr() + "\n" +
                " sum  = " + sum + "\n" +
                " sumsq  = " + sumsq + "\n" +
                " n   = " + n
        return s

    }

    override fun compareTo(o: StatSummary): Int {
        if (mean() > o.mean()) return 1
        return if (mean() < o.mean()) -1 else 0
    }

    companion object {


        internal var strictMessage = "No values in summary"
    }
}// System.out.println("Exited default...");