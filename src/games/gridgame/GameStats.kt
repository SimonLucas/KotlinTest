package games.gridgame

import agents.DoNothingAgent
import agents.RandomAgent
import ggi.SimplePlayerInterface
import java.io.FileWriter
import java.util.stream.Collectors
import java.util.stream.IntStream
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

open class GenerateGameData(val width: Int = 20, val height: Int = 20, seed: Long = -1) {

    val gridGame: GridGame

    init {
        this.gridGame = GridGame(width, height, seed = seed)
    }

    /**
     * Calculate all possible state strings
     *
     * for example in a 3x3 grid, all combinations of the following pattern
     * NNNNNNNNNX
     *
     * where N in [0,1] and X in 0-8
     *
     */
    fun calculatePossibleStates(patternSize: Int): List<String> {
        val patternSet = HashSet<String>()

        /**
         * Generate the 2^patternSet combinations
         */
        fun generateBinary(n: Int, l: List<String>): List<String> {
            if (n == 0) {
                return l
            } else {
                if (l.size == 0) {
                    return generateBinary(n - 1, listOf("0", "0"))
                } else {
                    return generateBinary(n - 1, l.map { it + "0" } + l.map { it + "1" })
                }
            }
        }


        val possiblePatterns = generateBinary(patternSize, ArrayList())

        // Add the possible actions to each of the binary patterns NNNNNNNN+X
        return possiblePatterns.flatMap { pattern: String -> IntStream.rangeClosed(0, 9).boxed().map { pattern + it }.collect(Collectors.toList()) }

    }

    /**
     * Generate data in csv format for the given number of steps, seed and agents
     *
     * Two csvs will be generated:
     *  - DataSet: contains all of the convolutional patterns and actions at each state
     *  - Stats: contains stats for the patterns seen in the game
     */
    fun generateData(nSteps: Int = 100, statsInterval: Int, agent1: SimplePlayerInterface, agent2: SimplePlayerInterface, seed: Long) {

        val possibleStates = calculatePossibleStates(9)

        val stateStatsWriter = StateStatsWriter(possibleStates, statsInterval, nSteps, agent1, agent2, seed)
        val dataSetWriter = DataSetWriter(nSteps, agent1, agent2, seed)
        val outputWriter = OutputWriter(nSteps, agent1, agent2, seed)

        val actions = intArrayOf(0, 0)

        for (i in 0 until nSteps) {
            val gameState = gridGame.copy();
            actions[0] = agent1.getAction(gameState, Constants.player1);
            actions[1] = agent2.getAction(gameState, Constants.player2);


            val currentState = gridGame.grid.copy()

            gridGame.next(actions)

            val nextState = gridGame.grid.copy()

            val patterns = getPatterns(currentState, nextState, actions)

            stateStatsWriter.addData(patterns, actions)
            dataSetWriter.addData(patterns, actions)
            outputWriter.addData(patterns, actions, i)

        }
    }

    /**
     * Create all of the convolution patterns from the current state
     */
    fun getPatterns(currentState: Grid, nextState: Grid, actions: IntArray): List<Pattern> {

        val patterns = ArrayList<Pattern>()

        val inputs = currentState.copy()
        inputs.setAll(0)

        for (action in actions)
            if (action != gridGame.doNothingAction())
                inputs.setCell(action, 1)

        for (i in 0 until height) {
            for (j in 0 until width) {
                val pattern = Pattern(vectorExtractor(currentState, i, j), nextState.getCell(i, j))
                when (includeNeighbourInputs) {
                    InputType.PlayerInt -> pattern.ip.add(getActionInt(inputs, i, j))
                    InputType.PlayerOneHot -> pattern.ip.addAll(vectorExtractor(inputs, i, j))
                }

                patterns.add(pattern)
            }
        }

        return patterns
    }


}

/**
 * Abstract class for writing data to csv files
 */
abstract class CSVWriter(val filename: String) {

    val fileWriter: FileWriter

    var rowCount = 0

    init {
        this.fileWriter = FileWriter(filename)
    }

    fun writeHeader(headers: List<String>) {
        writeLine(headers, "-")
    }

    fun writeLine(data: List<String>, rowId: String = this.rowCount.toString()) {
        try {
            fileWriter.append(rowId)

            for (col in data) {
                fileWriter.append(',')
                fileWriter.append(col)
            }

            fileWriter.append('\n')
            rowCount++
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}

/**
 * Calculates and writes statistics for every interval given by [interval]
 */
class StateStatsWriter(val possibleStates: List<String>, val interval: Int, val nSteps: Int, agent1: SimplePlayerInterface, agent2: SimplePlayerInterface, seed: Long) :
        CSVWriter("Stats-Seed:" + seed + "-A1:" + agent1.getAgentType() + "-A2:" + agent2.getAgentType() + "-nSteps:" + nSteps + ".csv") {

    init {
        writeHeader(possibleStates)
    }

    val patternCounter = HashMap<String, Int>()
    var totalSteps = 0
    var totalPatterns = 0

    fun addData(patterns: List<Pattern>, actions: IntArray) {

        for (pattern in patterns) {
            val patternString = pattern.ip.joinToString(separator = "")

            // If there is no value, set it to 0
            // Increment the value if it exists
            patternCounter.compute(patternString) { key: String, value: Int? -> value?.let { it + 1 } ?: 1 }
        }

        totalPatterns += patterns.size

        // Once we have enough patterns (interval) create stats for this interval and push to csv
        if (totalSteps % interval == 0) {
            calculateAndStoreStats(patternCounter, totalPatterns)
            printStatsSummary(patternCounter, totalPatterns)
        }

        totalSteps++
    }

    private fun calculateAndStoreStats(patternCounter: Map<String, Int>, totalPatterns: Int) {
        val statsRow = possibleStates
                .map { patternCounter.getOrDefault(it, 0) / totalPatterns.toDouble() }
                .map { "%.6f".format(it) }


        writeLine(statsRow)
    }

    private fun printStatsSummary(patternCounter: Map<String, Int>, totalPatterns: Int) {
        val sorted = patternCounter.toList().sortedBy { (_, value) -> value }.reversed()

        println("Top 10 states observed")
        for (i in 0 until 10) {
            println(sorted[i].first + " -> %.4f".format(sorted[i].second / totalPatterns.toDouble()))
        }
        println()
    }


}

open class DataSetWriter(nSteps: Int, agent1: SimplePlayerInterface, agent2: SimplePlayerInterface, seed: Long) :
        CSVWriter("DataSet-Seed:" + seed + "-A1:" + agent1.getAgentType() + "-A2:" + agent2.getAgentType() + "-nSteps:" + nSteps + ".csv") {

    fun addData(patterns: List<Pattern>, actions: IntArray) {

        val patternStrings = patterns.map { it.ip.joinToString(separator = "") }

        writeLine(patternStrings)
    }

}

open class OutputWriter(val nSteps: Int, agent1: SimplePlayerInterface, agent2: SimplePlayerInterface, seed: Long) :
        CSVWriter("Output-Seed:" + seed + "-A1:" + agent1.getAgentType() + "-A2:" + agent2.getAgentType() + "-nSteps:" + nSteps + ".csv") {

    // When the pattern was first seen
    val firstSeen = HashMap<String, Int>()

    // What the output was
    val output = HashMap<String, Int>()


    fun addData(patterns: List<Pattern>, actions: IntArray, step: Int) {

        patterns.forEach { pattern: Pattern ->
            run {

                val patternString = pattern.ip.joinToString(separator = "")
                // if we have never seen this state before, set the step number
                firstSeen.computeIfAbsent(patternString) { step }

                // If we have never seen this state before, set the output
                output.computeIfAbsent(patternString) { pattern.op }
            }
        }

        if (step == nSteps-1) {
            printCSV()
        }
    }

    private fun printCSV() {
        firstSeen.forEach { k, v ->
            run {
                writeLine(listOf(k, v.toString(), output[k].toString()))
            }
        }
    }
}

fun main(args: Array<String>) {

    val seed = 10L
    val dataGenerator = GenerateGameData(30, 30, seed);

    val agent1 = RandomAgent()
    val agent2 = DoNothingAgent()

    dataGenerator.generateData(1000, 10, agent1, agent2, seed)
}