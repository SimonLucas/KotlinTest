package games.eventqueuegame

import agents.SimpleEvoAgent
import utilities.JEasyFrame
import kotlin.random.Random

fun main() {
    val params = EventGameParams(minSep = 50, defaultOODALoop = 5)
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

    println(world)

    val blueAgent = SimpleActionEvoAgent(SimpleEvoAgent(nEvals = 100, sequenceLength = 40,
            useMutationTransducer = false, probMutation = 0.1,
            horizon = params.planningHorizon),
            opponentModel = SimpleActionEvoAgent(SimpleEvoAgent(nEvals = 10, sequenceLength = 40,
                    useMutationTransducer = false, probMutation = 0.1,
                    horizon = params.planningHorizon)))
    game.registerAgent(0, blueAgent)
    game.registerAgent(1, SimpleActionEvoAgent(SimpleEvoAgent(nEvals = 50, sequenceLength = 40, useMutationTransducer = false, probMutation = 0.1, horizon = params.planningHorizon)))
    // MakeDecision(PlayerId.Blue).apply(game)
    //  MakeDecision(PlayerId.Red).apply(game)

    println(world)


    val multiView = ListComponent()
    multiView.add(WorldView(game))
    val planView = PlanView(game.getAgent(0), game, 0)
//    multiView.add(planView)
    val frame = JEasyFrame(multiView, "Event Based Game")

    while (!game.isTerminal()) {
        game.next(1)
        frame.title = "${game.nTicks()}"
        multiView.repaint()
        planView.refresh()
        Thread.sleep(50)
    }
}
