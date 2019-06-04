package test.junit

import agents.MCTS.*
import games.citywars.LEFT
import games.eventqueuegame.*
import ggi.*
import ggi.game.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.lang.AssertionError
import kotlin.test.assertEquals

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

class MCTSMasterTest {

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
    fun bestActionWithNoDataIsNoAction() {
        val testState = simpleMazeGame.copy() as ActionAbstractGameState
        assertEquals(agents[2].getBestAction(testState), NoAction)
    }

    @Test
    fun bestActionWithSimpleSelection() {
        val testState = simpleMazeGame.copy() as ActionAbstractGameState
        val rootNode = TTNode(params, testState.possibleActions(2))
        agents[2].tree[MazeStateFunction(testState)] = rootNode
        rootNode.update(Move(2, Direction.LEFT), testState.possibleActions(2), 1.0)
        rootNode.update(Move(2, Direction.LEFT), testState.possibleActions(2), 1.0)
        rootNode.update(Move(2, Direction.RIGHT), testState.possibleActions(2), 2.0)
        rootNode.update(NoAction, testState.possibleActions(2), 1.5)
        assertEquals(agents[2].getBestAction(testState), Move(2, Direction.RIGHT))
    }

    @Test
    fun bestActionWithRobustSelection() {
        val thisParams = params.copy(selectionMethod = MCTSSelectionMethod.ROBUST)
        agents[2] = MCTSTranspositionTableAgentMaster(params = thisParams, stateFunction = MazeStateFunction)
        val testState = simpleMazeGame.copy() as ActionAbstractGameState
        val rootNode = TTNode(thisParams, testState.possibleActions(2))
        agents[2].tree[MazeStateFunction(testState)] = rootNode
        rootNode.update(Move(2, Direction.LEFT), testState.possibleActions(2), 1.0)
        rootNode.update(Move(2, Direction.LEFT), testState.possibleActions(2), 1.0)
        rootNode.update(Move(2, Direction.RIGHT), testState.possibleActions(2), 2.0)
        rootNode.update(NoAction, testState.possibleActions(2), 1.5)
        assertEquals(agents[2].getBestAction(testState), Move(2, Direction.LEFT))
    }

    @Test
    fun resetTreeDoesSo() {
        bestActionWithSimpleSelection()
        assertEquals(agents[2].tree.size, 1)
        assertEquals(agents[2].tree.values.flatMap { it.actionMap.values }.sumBy(MCStatistics::visitCount), 4)
        assertEquals(agents[1].tree.values.flatMap { it.actionMap.values }.sumBy(MCStatistics::visitCount), 0)
        assertEquals(agents[0].tree.values.flatMap { it.actionMap.values }.sumBy(MCStatistics::visitCount), 0)
        agents[2].resetTree(simpleMazeGame.copy() as ActionAbstractGameState, 2)
        assertEquals(agents[2].tree.size, 1)
        assertEquals(agents[2].tree.values.flatMap { it.actionMap.values }.sumBy(MCStatistics::visitCount), 0)

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

class MCTSChildTest {
    class MCTSChildTestAgent(tree: MutableMap<String, TTNode>, params: MCTSParameters, stateFunction: MazeStateFunction)
        : MCTSTranspositionTableAgentChild(tree, params, stateFunction) {


        var rolloutCalls = 0
        var expansionCalls = 0
        var treeCalls = 0
        // to make trajectory visible for testing
        fun trajectory() = trajectory

        override fun rolloutPolicy(state: ActionAbstractGameState, possibleActions: List<Action>): Action {
            rolloutCalls++
            return super.rolloutPolicy(state, possibleActions)
        }

        override fun expansionPolicy(node: TTNode, state: ActionAbstractGameState, possibleActions: List<Action>): Action {
            expansionCalls++
            return super.expansionPolicy(node, state, possibleActions)
        }

        override fun treePolicy(node: TTNode, state: ActionAbstractGameState, possibleActions: List<Action>): Action {
            treeCalls++
            return super.treePolicy(node, state, possibleActions)
        }

    }

    var simpleMazeGame = SimpleMazeGame(3, 10)
    val tree = mutableMapOf<String, TTNode>()
    val params = MCTSParameters()
    var childAgent = MCTSChildTestAgent(tree, params, MazeStateFunction)

    @BeforeEach
    fun setup() {
        simpleMazeGame = SimpleMazeGame(3, 10)
        tree.clear()
        childAgent = MCTSChildTestAgent(tree, params, MazeStateFunction)
    }

    @Test
    fun testTrajectoryPopulatedAndBackPropagationIsCorrect() {
        val root = MazeStateFunction(simpleMazeGame)
        tree[root] = TTNode(params, simpleMazeGame.possibleActions(0))
        assertEquals(tree.size, 1)
        assertTrue(tree.containsKey(root))

        val actionsSelected = mutableListOf<Action>()
        val statesEnRoute = mutableListOf<String>()
        repeat(5) {
            actionsSelected.add(childAgent.getAction(simpleMazeGame, 0))
            actionsSelected.last().apply(simpleMazeGame)
            statesEnRoute.add(MazeStateFunction(simpleMazeGame))
        }
        assertEquals(childAgent.trajectory().size, 5)
        childAgent.backPropagate(5.0)
        assertEquals(tree.size, 2)
        assertTrue(tree.containsKey(root))
        assertTrue(tree.containsKey(statesEnRoute[0]))

        (0 until 5).forEach { i -> assertEquals(actionsSelected[i], childAgent.trajectory().poll().third) }
    }

    @Test
    fun treePolicy() {
        // Tree policy will prioritise unused actions first
        val root = MazeStateFunction(simpleMazeGame)
        tree[root] = TTNode(params, simpleMazeGame.possibleActions(0))
        tree[root]!!.update(Move(0, Direction.LEFT), simpleMazeGame.possibleActions(0), 5.0)
        assertNotEquals(childAgent.treePolicy(tree[root]!!, simpleMazeGame, simpleMazeGame.possibleActions(0)), Move(0, Direction.LEFT))
        tree[root]!!.update(NoAction, simpleMazeGame.possibleActions(0), 4.0)
        assertEquals(childAgent.treePolicy(tree[root]!!, simpleMazeGame, simpleMazeGame.possibleActions(0)), Move(0, Direction.RIGHT))
        tree[root]!!.update(Move(0, Direction.RIGHT), simpleMazeGame.possibleActions(0), 4.0)
        assertEquals(childAgent.treePolicy(tree[root]!!, simpleMazeGame, simpleMazeGame.possibleActions(0)), Move(0, Direction.LEFT))
        assertEquals(childAgent.treeCalls, 3)
        assertEquals(childAgent.expansionCalls, 0)
        assertEquals(childAgent.rolloutCalls, 0)
    }

    @Test
    fun expansionPolicy() {
        val root = MazeStateFunction(simpleMazeGame)
        tree[root] = TTNode(params, simpleMazeGame.possibleActions(0))
        tree[root]!!.update(Move(0, Direction.LEFT), simpleMazeGame.possibleActions(0), 5.0)
        assertNotEquals(childAgent.expansionPolicy(tree[root]!!, simpleMazeGame, simpleMazeGame.possibleActions(0)), Move(0, Direction.LEFT))
        tree[root]!!.update(NoAction, simpleMazeGame.possibleActions(0), 4.0)
        assertEquals(childAgent.expansionPolicy(tree[root]!!, simpleMazeGame, simpleMazeGame.possibleActions(0)), Move(0, Direction.RIGHT))
        tree[root]!!.update(Move(0, Direction.RIGHT), simpleMazeGame.possibleActions(0), 4.0)
        assertThrows(AssertionError::class.java) { childAgent.expansionPolicy(tree[root]!!, simpleMazeGame, simpleMazeGame.possibleActions(0)) }
        assertEquals(childAgent.treeCalls, 0)
        assertEquals(childAgent.expansionCalls, 3)
        assertEquals(childAgent.rolloutCalls, 0)
    }

    @Test
    fun rolloutPolicy() {
        val rolloutActions = mutableListOf<Action>()
        repeat(100) {
            rolloutActions.add(childAgent.rolloutPolicy(simpleMazeGame, simpleMazeGame.possibleActions(0)))
        }
        assertEquals(rolloutActions.count{it == NoAction}.toDouble(), 33.0, 10.0)
        assertEquals(rolloutActions.count{it == Move(0, Direction.LEFT)}.toDouble(), 33.0, 10.0)
        assertEquals(rolloutActions.count{it == Move(0, Direction.RIGHT)}.toDouble(), 33.0, 10.0)
        assertEquals(childAgent.treeCalls, 0)
        assertEquals(childAgent.expansionCalls, 0)
        assertEquals(childAgent.rolloutCalls, 100)

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
        actions.add(node.getRandomUnexploredAction(allActions))
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

