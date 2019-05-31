package test.junit

import agents.MCTS.*
import games.eventqueuegame.*
import ggi.*
import ggi.game.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class SimpleMazeGame(val playerCount: Int, val target: Int) : ActionAbstractGameState {

    val eventQueue = EventQueue()
    override fun registerAgent(player: Int, agent: SimpleActionPlayerInterface) = eventQueue.registerAgent(player, agent, nTicks())
    override fun getAgent(player: Int) = eventQueue.getAgent(player)
    override fun planEvent(time: Int, action: Action) {
        eventQueue.add(Event(time, action))
    }

    var currentPosition = IntArray(playerCount) { 0 }  // initialise all players to the origin

    override fun nActions() = 3
    // LEFT, RIGHT, STOP

    override fun score(player: Int): Double {
        // we just have a score of 1.0 for reaching the goal
        return if (currentPosition[player] >= target) 1.0 else 0.0
    }

    override fun playerCount() = playerCount

    override fun possibleActions(player: Int) = listOf(
            Move(player, Direction.LEFT),
            Move(player, Direction.RIGHT),
            NoAction)

    override fun codonsPerAction() = 1
    override fun translateGene(player: Int, gene: IntArray) = possibleActions(player).getOrElse(gene[0]) { NoAction }

    override fun copy(): AbstractGameState {
        val retValue = SimpleMazeGame(playerCount, target)
        retValue.currentPosition = currentPosition.copyOf()
        retValue.eventQueue.addAll(eventQueue)
        retValue.eventQueue.currentTime = nTicks()
        return retValue
    }

    override fun isTerminal() = currentPosition.any { it >= target }

    override fun nTicks() = eventQueue.currentTime

    override fun next(forwardTicks: Int): ActionAbstractGameState {
        eventQueue.next(forwardTicks, this)
        return this
    }
}

enum class Direction { LEFT, RIGHT }

data class Move(val player: Int, val direction: Direction) : Action {
    override fun apply(state: ActionAbstractGameState): Int {
        if (state is SimpleMazeGame) {
            when (direction) {
                Direction.LEFT -> state.currentPosition[player]--
                Direction.RIGHT -> state.currentPosition[player]++
            }
        }
        return state.nTicks() + 1
    }

    override fun visibleTo(player: Int, state: ActionAbstractGameState) = true
}

object MazeStateFunction : StateSummarizer {
    override fun invoke(state: ActionAbstractGameState): String {
        if (state is SimpleMazeGame) {
            // state is just time, and the current position of all agents
            return with(StringBuilder()) {
                append(state.nTicks())
                append("|")
                append(state.currentPosition.joinToString(separator = "|"))
            }.toString()
        }
        return ""
    }

}

class SimpleMCTSTest() {

    val params = MCTSParameters(
            C = 1.0,
            selectionMethod = MCTSSelectionMethod.SIMPLE,
            maxPlayouts = 100,
            timeLimit = 1000,
            horizon = 20,     // limit of actions taken across both tree and rollout policies
            discountRate = 1.0
    )
    val simpleMazeGame = SimpleMazeGame(3, 10)
    val agents = arrayListOf(
            MCTSTranspositionTableAgentMaster(params = params.copy(maxPlayouts = 2), stateFunction = MazeStateFunction),
            MCTSTranspositionTableAgentMaster(params = params.copy(maxPlayouts = 5), stateFunction = MazeStateFunction),
            MCTSTranspositionTableAgentMaster(params = params.copy(maxPlayouts = 10), stateFunction = MazeStateFunction)
    )

    @BeforeEach
    fun setup() {
        agents.withIndex().forEach { (i, agent) -> simpleMazeGame.registerAgent(i, agent) }
    }

    @Test
    fun oneNodeAddedToTreePerIteration() {
        assert(simpleMazeGame.eventQueue.any { e -> e.tick == 0 && e.action is MakeDecision && e.action.playerRef == 0 })
        assert(simpleMazeGame.eventQueue.any { e -> e.tick == 0 && e.action is MakeDecision && e.action.playerRef == 1 })
        assert(simpleMazeGame.eventQueue.any { e -> e.tick == 0 && e.action is MakeDecision && e.action.playerRef == 2 })
        assertEquals(simpleMazeGame.eventQueue.size, 3)
        assertEquals(agents[0].tree.size, 0)
        assertEquals(agents[1].tree.size, 0)
        assertEquals(agents[2].tree.size, 0)

        simpleMazeGame.next(1)

        assert(simpleMazeGame.eventQueue.any { e -> e.tick == 1 && e.action is MakeDecision && e.action.playerRef == 0 })
        assert(simpleMazeGame.eventQueue.any { e -> e.tick == 1 && e.action is MakeDecision && e.action.playerRef == 1 })
        assert(simpleMazeGame.eventQueue.any { e -> e.tick == 1 && e.action is MakeDecision && e.action.playerRef == 2 })
        assertEquals(agents[2].tree.size, 11)
        assertEquals(agents[1].tree.size, 6)
        assertEquals(agents[0].tree.size, 3)
    }

    @Test
    fun allNodesExpandedBeforeNextOnePicked() {
        simpleMazeGame.next(1)
        assertEquals(agents[0].tree.size, 3)
        val rootAgent0 = agents[0].tree["0|0|0|0"]
        assertFalse(rootAgent0 == null)
        assertEquals(rootAgent0!!.actionMap.values.count { it.visitCount == 1 }, 2)
        assertEquals(rootAgent0.actionMap.values.count { it.validVisitCount == 2 }, 3)
        assertEquals(agents[0].tree.values.flatMap { n -> n.actionMap.values }.count { it.visitCount == 1 }, 2)
        assertEquals(agents[0].tree.values.flatMap { n -> n.actionMap.values }.count { it.validVisitCount == 2 }, 3)

        assertEquals(agents[1].tree.values.flatMap { n -> n.actionMap.values }.count { it.visitCount == 2 }, 2)
        assertEquals(agents[1].tree.values.flatMap { n -> n.actionMap.values }.count { it.validVisitCount == 5 }, 3)
        assertEquals(agents[1].tree.values.flatMap { n -> n.actionMap.values }.count { it.visitCount == 1 }, 3)
        assertEquals(agents[1].tree.values.flatMap { n -> n.actionMap.values }.count { it.validVisitCount == 1 }, 6)
    }
}

class MCStatisticsTest {

    @Test
    fun checkMean() {
        val stats = MCStatistics()
        assertEquals(stats.mean, Double.NaN)
        stats.sum += 10.0
        assertEquals(stats.mean, Double.NaN)
        stats.visitCount++
        assertEquals(stats.mean, 10.0)
        stats.sum += 25.0
        stats.visitCount++
        assertEquals(stats.mean, 17.5)
    }

    @Test
    fun testUCTDefault() {
        val stats = MCStatistics(params = MCTSParameters(C = 2.0))
        assertEquals(stats.UCTScore(), Double.POSITIVE_INFINITY)
        stats.sum = 30.0
        stats.visitCount = 10
        stats.validVisitCount = 100
        assertEquals(stats.UCTScore(), 3.0 + 2.0 * Math.sqrt(Math.log(100.0) / 10))
    }
}

class TTNodeTest {

    val allActions = listOf(
            Move(0, Direction.LEFT),
            Move(0, Direction.RIGHT),
            NoAction)

    @Test
    fun updateTest() {
        val node = TTNode(MCTSParameters(), allActions)
        val stats = node.actionMap[NoAction]!!
        assertTrue(node.actionMap.values.all { it.validVisitCount == 0 })
        assertEquals(stats.mean, Double.NaN)
        assertEquals(stats.max, Double.NEGATIVE_INFINITY)
        assertEquals(stats.min, Double.POSITIVE_INFINITY)
        assertEquals(stats.sum, 0.0)
        assertEquals(stats.sumSquares, 0.0)

        node.update(NoAction, allActions, 2.0)

        assertEquals(stats.mean, 2.0)
        assertEquals(stats.max, 2.0)
        assertEquals(stats.min, 2.0)
        assertEquals(stats.sum, 2.0)
        assertEquals(stats.sumSquares, 4.0)
        assertEquals(stats.validVisitCount, 1)
        assertEquals(stats.visitCount, 1)
        assertTrue(node.actionMap.values.all { it.validVisitCount == 1 })
    }

    @Test
    fun testUnexploredActions() {
        val node = TTNode(MCTSParameters(), allActions)
        assertTrue(node.hasUnexploredActions())
        val actions = ArrayList<Action>(3)
        actions.add(node.getRandomUnexploredAction(allActions))
        node.update(actions[0], allActions, 1.0)
        assertTrue(node.hasUnexploredActions())
        actions.add( node.getRandomUnexploredAction(allActions))
        node.update(actions[1], allActions, 2.0)
        assertTrue(node.hasUnexploredActions())
        actions.add(node.getRandomUnexploredAction(allActions))
        node.update(actions[2], allActions, 3.0)
        assertFalse(node.hasUnexploredActions())

        assertTrue((allActions - actions).isEmpty())
        assertTrue((actions - allActions).isEmpty())
        assertEquals(node.getUCTAction(allActions), actions[2])
    }
}
