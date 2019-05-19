package games.eventqueuegame

import agents.SimpleEvoAgent
import ggi.SimpleActionPlayerInterface
import utilities.JEasyFrame
import kotlin.random.Random

fun main() {
    val params = EventGameParams(minSep = 50)
    val agents = HashMap<PlayerId, SimpleActionPlayerInterface>()
    agents[PlayerId.Blue] = SimpleActionEvoAgent(SimpleEvoAgent(nEvals = 50, sequenceLength = 40, horizon = 100 ,useMutationTransducer = false, probMutation = 0.1),
            opponentModel = SimpleActionEvoAgent(SimpleEvoAgent(nEvals = 50, sequenceLength = 40, useMutationTransducer = false, probMutation = 0.1, horizon = params.planningHorizon)))
    agents[PlayerId.Red] = SimpleActionEvoAgent(SimpleEvoAgent(nEvals = 50, sequenceLength = 40, horizon = 100, useMutationTransducer = false, probMutation = 0.1))

    var blueWins = 0;
    var redWins = 0;
    var draws = 0
    val maxGames = 500
    val startTime = java.util.Calendar.getInstance().timeInMillis
    for (g in 1..maxGames) {
        val world = World(random = Random(1), params = params)
        val game = EventQueueGame(world)
        game.scoreFunction = {
            // 5 points per city
            val blueCities = it.world.cities.count { c -> c.owner == PlayerId.Blue }
            val redCities = it.world.cities.count { c -> c.owner == PlayerId.Red }
            // then add the total of all forces
            val blueForces = it.world.cities.filter { c -> c.owner == PlayerId.Blue }.sumBy(City::pop) +
                    it.world.currentTransits.filter { t -> t.playerId == PlayerId.Blue }.sumBy(Transit::nPeople)
            val redForces = it.world.cities.filter { c -> c.owner == PlayerId.Red }.sumBy(City::pop) +
                    it.world.currentTransits.filter { t -> t.playerId == PlayerId.Red }.sumBy(Transit::nPeople)
            5.0 * (blueCities - redCities) + blueForces - redForces
        }

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
