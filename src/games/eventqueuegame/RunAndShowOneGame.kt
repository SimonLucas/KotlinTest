package games.eventqueuegame

import agents.SimpleEvoAgent
import utilities.JEasyFrame
import kotlin.random.Random

fun main() {
    val params = EventGameParams(
            fogOfWar = true,
            citySeparation = 50,
            speed = 7.0,
            planningHorizon = 200,
            OODALoop = intArrayOf(25, 25),
            blueLanchesterCoeff = 0.05,
            redLanchesterCoeff = 0.05,
            blueLanchesterExp = 0.5,
            redLanchesterExp = 0.5,
            percentFort = 0.25,
            fortAttackerCoeffDivisor = 2.0,
            fortDefenderExpIncrease = 0.1)
    val world = World(random = Random(1), params = params)
    val targets = mapOf(PlayerId.Blue to listOf(0, 2, 4, 5), PlayerId.Red to listOf(0, 1, 3, 5))
    val game = EventQueueGame(world, targets = emptyMap())
    game.scoreFunction = simpleScoreFunction(5.0, 1.0)
 //   game.scoreFunction = specificTargetScoreFunction(50.0)

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

    val multiView = ListComponent()
    val omniView = WorldView(game)
    val redView = WorldView(game)
    val blueView = WorldView(game)
    multiView.add(omniView)
    multiView.add(redView)
    multiView.add(blueView)
    //   val planView = PlanView(game.getAgent(0), game, 0)
//    multiView.add(planView)
    val frame = JEasyFrame(multiView, "Event Based Game")

    while (!game.isTerminal()) {
        game.next(1)
        redView.game = game.copy(1)
        blueView.game = game.copy(0)
        frame.title = "${game.nTicks()}"
        multiView.repaint()
        //      planView.refresh()
        Thread.sleep(50)
    }
}
