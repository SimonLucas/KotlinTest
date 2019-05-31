package games.eventqueuegame

import agents.*
import agents.MCTS.*
import ggi.SimpleActionPlayerInterface
import kotlin.random.Random

fun main() {
    StatsCollator.clear()
    val params = EventGameParams(citySeparation = 50,
            OODALoop = intArrayOf(20, 20))
    val agents = HashMap<PlayerId, SimpleActionPlayerInterface>()
    agents[PlayerId.Blue] = SimpleActionEvoAgent(SimpleEvoAgent(nEvals = 1000, timeLimit = 50, sequenceLength = 40, horizon = 100, useMutationTransducer = false, probMutation = 0.25))
//        opponentModel = SimpleActionEvoAgent(SimpleEvoAgent(nEvals = 10, sequenceLength = 12, useMutationTransducer = false, probMutation = 0.25, horizon = 100)))
//    agents[PlayerId.Red] = SimpleActionEvoAgent(SimpleEvoAgent(nEvals = 20, sequenceLength = 40, horizon = 100, useMutationTransducer = false, probMutation = 0.25))
    agents[PlayerId.Red] = MCTSTranspositionTableAgentMaster(MCTSParameters(maxPlayouts = 1000, timeLimit = 50, horizon = 100), LandCombatStateFunction)

    var blueWins = 0;
    var redWins = 0;
    var draws = 0
    val maxGames = 10

    val startTime = java.util.Calendar.getInstance().timeInMillis
    val useConstantWorld = false
    val constantWorld = 1
    for (r in 1..maxGames) {

        agents[PlayerId.Blue]?.reset()
        agents[PlayerId.Red]?.reset()

        val world = World(random = Random( if (useConstantWorld) constantWorld else r), params = params)
        val game = LandCombatGame(world)
        game.scoreFunction = simpleScoreFunction(5.0, 1.0)

        game.registerAgent(0, agents[PlayerId.Blue] ?: SimpleActionDoNothing)
        game.registerAgent(1, agents[PlayerId.Red] ?: SimpleActionDoNothing)
        game.next(1000)
        val gameScore = game.score(0)
        println(String.format("Game %2d\tScore: %4.1f", r, gameScore))
        when {
            gameScore > 0.0 -> blueWins++
            gameScore < 0.0 -> redWins++
            else -> draws++
        }
    }
    println("$blueWins wins for Blue, $redWins for Red and $draws draws out of $maxGames in ${(java.util.Calendar.getInstance().timeInMillis - startTime) / maxGames} ms per game")
    println(StatsCollator.summaryString())
}
