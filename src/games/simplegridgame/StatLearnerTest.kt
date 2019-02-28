package games.simplegridgame

import agents.SimpleEvoAgent
import games.gridgame.UpdateRule
import ggi.AbstractGameState
import ggi.SimplePlayerInterface
import utilities.StatSummary
import java.util.concurrent.ForkJoinPool

val minLut = 0
val maxLut = 512
val lutInteval = 5

fun main(args: Array<String>) {

    val executor = ForkJoinPool()

    // Learns many StatLearners in parallel
    val multiLearner = MultiLutStatLearner(minLut, maxLut, lutInteval)

    val game = SimpleGridGame(multiLearner, 30, 30)
    game.updateRule = LifeUpdateRule()
    game.rewardFactor = 1.0

    val agent1: SimplePlayerInterface = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 5, nEvals = 20)
    val agent2: SimplePlayerInterface = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 5, nEvals = 10)

    train(game, agent1, agent2, 10)

    val statSummaries = HashMap<Int, StatSummary>()

    val tasks = multiLearner.statLearners.map { entry ->

            val lut = entry.key
            val learner = entry.value
            return@map executor.submit {
                println("Playing with lut limit {$lut}")
                statSummaries.put(lut, testGames(agent1, learner, 10))
            }
    }

    // Wait for all the games to finish processsing in parallel
    tasks.forEach {t -> t.join()}

    statSummaries.toSortedMap().forEach { lut, summary ->
        run {
            println("Lut: ${lut}, Average Score: ${summary.mean()}")
        }
    }
}

fun train(game: AbstractGameState, agent1: SimplePlayerInterface, agent2: SimplePlayerInterface, trainingSteps: Int = 100) {
    harvestData = true
    for (i in 0 until trainingSteps) {

        game.next(
                intArrayOf(
                        agent1.getAction(game.copy(), Constants.player1),
                        agent2.getAction(game.copy(), Constants.player2)
                )
        )
        println("Training epoch [${i}]")
    }
    harvestData = false
}

fun testGames(agent: SimplePlayerInterface, learnedRule: UpdateRule, gamesPerEval: Int = 10): StatSummary {
    val ss = StatSummary()

    // Use the same learner for all games
    for (i in 0 until gamesPerEval) {

        val game = SimpleGridGame(null, w, h)
        game.updateRule = LifeUpdateRule()

        for (i in 0 until testSteps) {
            val actions = intArrayOf(0, 0)

            val agentCopy = game.copy() as SimpleGridGame
            agentCopy.updateRule = learnedRule

            game.next(
                    intArrayOf(
                            agent.getAction(agentCopy, Constants.player1),
                            0
                    )
            )

        }
        println("Game: ${i + 1}, score = ${game.score()}")
        ss.add(game.score())
    }
    return ss
}