package games.sokoban

import agents.RandomAgent
import games.gridgame.data
import utilities.ElapsedTimer
import utilities.JEasyFrame
import utilities.StatSummary

fun main(args: Array<String>) {

    var game = Sokoban()
    game.print()
    val actions = intArrayOf(0, 0)
    //var agent1: SimplePlayerInterface = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 5, nEvals = 40)
    var agent = RandomAgent()

    val gatherer = Gatherer()

    val timer = ElapsedTimer()
    val nSteps = 10000
    for (i in 0 until nSteps) {
        actions[0] = agent.getAction(game.copy(), Constants.player1)
        val grid1 = game.board.deepCopy()
        game.next(actions)
        val grid2 = game.board.deepCopy()
        gatherer.addGrid(grid1, grid2, actions[0])
    }

    // now print the patterns

    println("Ran for $nSteps steps")
    println("Generated ${gatherer.data.size} unique observations")
    println("Total local patterns = " + gatherer.total)
    gatherer.report()
    game.print()
    println(timer)
}

data class Example(val ip: ArrayList<Char>, val action: Int)

class Distribution() {
    val dis = HashMap<Char,Int>()
    fun add(op: Char) {
        var count = dis.get(op)
        if (count == null) count =0
        count++
        dis[op] = count
    }
    override fun toString() : String {
        return dis.toString()
    }
}

class Gatherer {

    val data = HashMap<Example, Distribution>()
    var total = 0

    fun addGrid(grid1: Grid, grid2: Grid, action: Int) {
        assert(grid1.getWidth() == grid2.getWidth() && grid1.getHeight() == grid2.getHeight())
        for (x in 0 until grid1.getWidth()) {
            for (y in 0 until grid1.getHeight()) {
                val op = grid2.getCell(x, y)
                val ip = extractVector(grid1, x, y)
                // data[Example(ip, action, op)]++
                val example = Example(ip, action)
                var distribution = data[example]
                if (distribution == null) {
                    distribution = Distribution()
                    data.put(example, distribution)
                }
                distribution.add(op)
                total++
            }
        }
    }

    val span = 2
    // should really generalise this to offer different extraction patterns
    fun extractVector(grid: Grid, x: Int, y: Int): ArrayList<Char> {
        val v = ArrayList<Char>()
        // add the centre cell
        v.add(grid.getCell(x,y))
        // now row except centre
        for (xx in x - span .. x + span) {
            if (xx != x) v.add(grid.getCell(xx, y))
        }
        // now column except centre
        for (yy in y - span .. y + span) {
            if (yy != y) v.add(grid.getCell(x, yy))
        }
        return v
    }

    fun report() {
        data.forEach{key, value -> println("$key -> $value")}
    }
}

