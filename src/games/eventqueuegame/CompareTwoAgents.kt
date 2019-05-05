package games.eventqueuegame

import agents.SimpleEvoAgent
import utilities.JEasyFrame
import kotlin.random.Random

val calendar = java.util.Calendar.getInstance()

fun main() {
    val params = EventGameParams(minSep = 50)
    val agents = HashMap<PlayerId, SimpleEvoAgent>()
    agents[PlayerId.Blue] = SimpleEvoAgent(nEvals = 50, sequenceLength = 200)
    agents[PlayerId.Red] = SimpleEvoAgent(nEvals = 5, sequenceLength = 200)

    var blueWins = 0;
    var redWins = 0;
    var draws = 0
    val maxGames = 500
    val startTime = calendar.timeInMillis
    for (g in 1..maxGames) {
        val world = World(speed = 10.0, random = Random(1), params = params)
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
        while (!game.isTerminal()) {
            val redGene = agents[PlayerId.Red]?.getActions(game, 1)?.slice(0..3) ?: listOf(0, 0, 0, 0)
            val redAction = LaunchExpedition(PlayerId.Red, redGene.get(0), redGene.get(1), redGene.get(2), redGene.get(3))
            val blueGene = agents[PlayerId.Blue]?.getActions(game, 0)?.slice(0..3) ?: listOf(0, 0, 0, 0)
            val blueAction = LaunchExpedition(PlayerId.Blue, blueGene.get(0), blueGene.get(1), blueGene.get(2), blueGene.get(3))
            game.next(listOf(blueAction, redAction))
        }
        val gameScore = game.score()
        println(gameScore)
        when {
            gameScore > 0.0 -> blueWins++
            gameScore < 0.0 -> redWins++
            else -> draws++
        }
    }
    println("$blueWins wins for Blue, $redWins for Red and $draws draws out of $maxGames in ${(calendar.timeInMillis - startTime) / maxGames} ms per game")
}
