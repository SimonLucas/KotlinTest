package games.eventqueuegame

object StatsCollator {

    private var statistics: MutableMap<String, Double> = HashMap()
    private var count: MutableMap<String, Int> = HashMap()

    fun clear() {
        statistics = HashMap()
        count = HashMap()
    }

    fun addStatistics(newStats: Map<String, Double>) {
        newStats.forEach { (k, v) -> addStatistics(k, v) }
    }

    fun addStatistics(key: String, value: Double) {
        val oldV = statistics.getOrDefault(key, 0.00)
        val newValue = oldV + value
        statistics[key] = newValue
        val newCount = count.getOrDefault(key, 0) + 1
        count[key] = newCount
    }
    fun addStatistics(key: String, value: Int) = addStatistics(key, value.toDouble())
    fun addStatistics(key: String, value: Long) = addStatistics(key, value.toDouble())

    fun summaryString(): String {
        return statistics.entries
                .map { (k, v) -> String.format("%-20s = %.4g\n", k, v / (count[k]!!)) }
                .sorted()
                .joinToString(separator = "")
    }
}

