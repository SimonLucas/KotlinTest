package games.eventqueuegame

import agents.SimpleEvoAgent
import ggi.SimpleActionPlayerInterface
import test.Player
import utilities.JEasyFrame
import kotlin.random.Random

fun main() {
    val params = EventGameParams(minSep = 50)
    val agents = HashMap<PlayerId, SimpleActionPlayerInterface>()
    agents[PlayerId.Blue] = SimpleActionEvoAgent(SimpleEvoAgent(nEvals = 20, sequenceLength = 12, horizon = 100, useMutationTransducer = false, probMutation = 0.25))
//        opponentModel = SimpleActionEvoAgent(SimpleEvoAgent(nEvals = 10, sequenceLength = 12, useMutationTransducer = false, probMutation = 0.25, horizon = 100)))
    agents[PlayerId.Red] = SimpleActionEvoAgent(SimpleEvoAgent(nEvals = 20, sequenceLength = 40, horizon = 100, useMutationTransducer = false, probMutation = 0.1))

    var blueWins = 0;
    var redWins = 0;
    var draws = 0
    val maxGames = 1000

    val startTime = java.util.Calendar.getInstance().timeInMillis
    for (r in 1..maxGames) {

        agents[PlayerId.Blue]?.reset()
        agents[PlayerId.Red]?.reset()

        val world = World(random = Random(r), params = params)
        val game = EventQueueGame(world)
        game.scoreFunction = simpleScoreFunction(5.0, 1.0)

        game.registerAgent(0, agents[PlayerId.Blue] ?: SimpleActionDoNothing)
        game.registerAgent(1, agents[PlayerId.Red] ?: SimpleActionDoNothing)
        game.next(1000)
        val gameScore = game.score()
//        println(gameScore)
        when {
            gameScore > 0.0 -> blueWins++
            gameScore < 0.0 -> redWins++
            else -> draws++
        }
    }
    println("$blueWins wins for Blue, $redWins for Red and $draws draws out of $maxGames in ${(java.util.Calendar.getInstance().timeInMillis - startTime) / maxGames} ms per game")
}
