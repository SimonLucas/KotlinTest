package games.simplegridgame


/**
 * Learn a number of stat learners with different limits to their look-up tables
 */
class MultiLutStatLearner(val minLut: Int, val maxLut: Int, val interval: Int) : Learner() {

    val numLearners: Int
    val statLearners = HashMap<Int, StatLearner>()

    init {

        var learners = 0
        var lut = minLut
        while (lut <= maxLut) {

            statLearners.put(lut, StatLearner(lutSizeLimit = lut))

            lut += interval
            learners ++

        }

        numLearners = learners
        println("NumLearners = ${numLearners}")

    }

    override fun add(pattern: ArrayList<Int>, value: Int) {
        statLearners.forEach { lut, learner ->
            run {
                learner.add(pattern, value)
            }
        }
    }

}