package test

class RandomArrayTest {

    fun sumElements(a: Array<DoubleArray>): Double {
        var tot = 0.0
        for (aa in a) {
            for (x in aa) {
                tot += x
            }
        }
        return tot
    }

}
