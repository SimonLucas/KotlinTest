package games.eventqueuegame

import agents.SimpleEvoAgent
import utilities.JEasyFrame
import kotlin.random.Random

fun main() {
    val params = EventGameParams(
            minSep = 50,
            planningHorizon = 100,
            OODALoop = intArrayOf(5, 5),
            blueLanchesterCoeff = 0.05,
            redLanchesterCoeff = 0.05,
            blueLanchesterExp = 0.5,
            redLanchesterExp = 0.5)
    val world = World(random = Random(1), params = params)
    val targets = mapOf(PlayerId.Blue to listOf(0, 2, 4, 5), PlayerId.Red to listOf(0, 1, 3, 5))
    val game = EventQueueGame(world, targets = emptyMap())
    game.scoreFunction = simpleScoreFunction(5.0, 1.0)
    // game.scoreFunction = specificTargetScoreFunction(50.0)

    println(world)

    val blueAgent = SimpleActionEvoAgent(SimpleEvoAgent(nEvals = 200, sequenceLength = 40,
            useMutationTransducer = false, probMutation = 0.1,
            horizon = params.planningHorizon),
            opponentModel = SimpleActionEvoAgent(SimpleEvoAgent(nEvals = 10, sequenceLength = 40,
                    useMutationTransducer = false, probMutation = 0.1,
                    horizon = params.planningHorizon)))
    game.registerAgent(0, blueAgent)
    game.registerAgent(1, SimpleActionEvoAgent(SimpleEvoAgent(nEvals = 200, sequenceLength = 40,
            useMutationTransducer = false, probMutation = 0.1, horizon = params.planningHorizon)))
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
