package games.eventqueuegame

import agents.SimpleEvoAgent
import utilities.JEasyFrame
import kotlin.random.Random

fun main() {
    val params = EventGameParams(
            minSep = 50,
            planningHorizon = 100,
            defaultOODALoop = 5,
            blueLanchesterCoeff = 0.05,
            redLanchesterCoeff = 0.05,
            blueLanchesterExp = 0.5,
            redLanchesterExp = 0.5)
    val world = World(random = Random(1), params = params)
    val game = EventQueueGame(world)
    game.scoreFunction = simpleScoreFunction(5.0, 1.0)

    println(world)

    val blueAgent = SimpleActionEvoAgent(SimpleEvoAgent(nEvals = 500, sequenceLength = 40,
            useMutationTransducer = false, probMutation = 0.1,
            horizon = params.planningHorizon),
            opponentModel = SimpleActionEvoAgent(SimpleEvoAgent(nEvals = 10, sequenceLength = 40,
                    useMutationTransducer = false, probMutation = 0.1,
                    horizon = params.planningHorizon)))
    game.registerAgent(0, blueAgent)
    game.registerAgent(1, SimpleActionEvoAgent(SimpleEvoAgent(nEvals = 100, sequenceLength = 40, useMutationTransducer = false, probMutation = 0.1, horizon = params.planningHorizon)))
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
