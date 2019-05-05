package games.sokoban

import agents.RandomAgent
import forwardmodels.decisiontree.DecisionTree
import forwardmodels.decisiontree.MultiClassDecisionTree
import forwardmodels.modelinterface.ForwardModelTrainerSimpleGridGame
import games.gridgame.InputType
import utilities.ElapsedTimer
import weka.classifiers.trees.J48
import java.lang.StringBuilder

fun main(args: Array<String>) {
    val game = Sokoban()
    game.print()
    val actions = intArrayOf(0, 0)
    //var agent1: SimplePlayerInterface = SimpleEvoAgent(useMutationTransducer = false, sequenceLength = 5, nEvals = 40)
    val agent = RandomAgent()

    val gathererAndTrainer = GathererAndTrainer()

    val timer = ElapsedTimer()
    val nSteps = 1000

    var predictedGrid : Grid

    for (i in 0 until nSteps) {
        actions[0] = agent.getAction(game.copy(), Constants.player1)
        val grid1 = game.board.deepCopy()
        // setting cells is a quick hack to get around the fact that
        // it's not properly updated
        grid1.setCell(grid1.playerX, grid1.playerY, 'A')
        val score1 = game.score()

        game.next(actions)
        val grid2 = game.board.deepCopy()
        grid2.setCell(grid2.playerX, grid2.playerY, 'A')

        if (game.nTicks > 1){
            gathererAndTrainer.evaluate(grid1, actions[0], grid2)
        }

        //gather new patterns
        gathererAndTrainer.addGrid(grid1, grid2, actions[0], game.score() - score1)
    }

    // now print the patterns
    println("Ran for $nSteps steps")
    println("Total local patterns = " + gathererAndTrainer.total)

    gathererAndTrainer.report()
    println()
    //game.print()

    println(timer)
}


class GathererAndTrainer {

    val tileData = HashMap<Example, TileDistribution>()
    val trainData = ArrayList<String>()
    val rewardData = HashMap<Example, RewardDistribution>()
    var tree : MultiClassDecisionTree
    var trainedPatterns = 0
    var total = 0
    var erroneousPredictions = 0
    var totalPredictions = 0

    init {
        val sb = StringBuilder()
        sb.append("(0,0)")
        for (xx in 0 - span .. 0 + span) {
            if (xx != 0) sb.append(";($xx,0)")
        }
        for (yy in 0 - span .. 0 + span) {
            if (yy != 0) sb.append(";(0,$yy)")
        }
        //sb.append(";action;result")
        tree = MultiClassDecisionTree(sb.toString())
        //trainData.add(sb.toString())
        println(trainData)
    }


    fun predictGrid(grid: Grid, action: Int) : Grid {
        val predictedGrid = grid.deepCopy()
        for (x in 0 until grid.getWidth()) {
            for (y in 0 until grid.getHeight()) {
                val ip = extractVectorFixed(grid, x, y)
                predictedGrid.setCell(x,y,tree.predictCell(ip, action).single())
            }
        }
        return predictedGrid
    }


    fun addGrid(grid1: Grid, grid2: Grid, action: Int, rewardDelta: Double) {
        assert(grid1.getWidth() == grid2.getWidth() && grid1.getHeight() == grid2.getHeight())
        for (x in 0 until grid1.getWidth()) {
            for (y in 0 until grid1.getHeight()) {
                val op = grid2.getCell(x, y)
                val ip = extractVectorFixed(grid1, x, y)
                // data[Example(ip, action, op)]++
                val example = Example(ip, action)

                // now update the tile data
                var tileDis = tileData[example]
                if (tileDis == null) {
                    tileDis = TileDistribution()
                    tileData.put(example, tileDis)

                    val sb = StringBuilder()
                    sb.append("${example.ip.joinToString(",")},${example.action},$op")
                    trainData.add(sb.toString())
                    tree.addDataPoint(example.ip, example.action, op)
                }
                tileDis.add(op)


                // now update the reward data
                var rewardDis = rewardData[example]
                if (rewardDis == null) {
                    rewardDis = RewardDistribution()
                    rewardData.put(example, rewardDis)
                }
                rewardDis.add(rewardDelta)

                // and the reward data

                total++
            }
        }
        tree.updateTree()
    }

    // should really generalise this to offer different extraction patterns
    fun extractVector(grid: Grid, x: Int, y: Int): ArrayList<Char> {
        val v = ArrayList<Char>()
        // add the centre cell
        //println("starting querries")
        v.add(grid.getCell(x,y))
        // now row except centre

        // Todo: there may be an error here:
        //  getting the grid at position (0,0) returns (w,w,w,w,w,w,w,w,w)
        //  based on the method getCell the following cells are reported to the user:
        //      querried cell   reported cell
        //      x: 0; y: 0;     xx: 0; yy: 0
        //      x: -2; y: 0;    xx: 6; yy: 0
        //      x: -1; y: 0;    xx: 7; yy: 0
        //      x: 1; y: 0;     xx: 1; yy: 0
        //      x: 2; y: 0;     xx: 2; yy: 0
        //      x: 0; y: -2;    xx: 0; yy: 5
        //      x: 0; y: -1;    xx: 0; yy: 6
        //      x: 0; y: 1;     xx: 0; yy: 1
        //      x: 0; y: 2;     xx: 0; yy: 2
        //  possible fix: check if array is out of bounds and report None or an empty char
        //  see: extractVectorFixed

        for (xx in x - span .. x + span) {
            if (xx != x) v.add(grid.getCell(xx, y))
        }
        // now column except centre
        for (yy in y - span .. y + span) {
            if (yy != y) v.add(grid.getCell(x, yy))
        }
        return v
    }

    // should really generalise this to offer different extraction patterns
    fun extractVectorFixed(grid: Grid, x: Int, y: Int): ArrayList<Char> {
        val v = ArrayList<Char>()
        // add the centre cell
        v.add(grid.getCell(x,y))
        //println("x:$x; y:$y")
        // now row except centre

        for (xx in x - span .. x + span) {
            if (xx != x) {
                //println("x:$xx; y:$y")
                if (xx >= 0 && xx < grid.w)
                    v.add(grid.getCell(xx, y))
                else
                    v.add('x')
            }
        }
        // now column except centre
        for (yy in y - span .. y + span) {
            if (yy != y) {
                //println("x:$x; y:$yy")
                if (yy >= 0 && yy < grid.h)
                    v.add(grid.getCell(x, yy))
                else
                    v.add('x')
            }
        }
        return v
    }

    fun report() {
        println("Tile distributions: generated ${this.tileData.size} unique tile observations")
        //tileData.forEach{key, value -> println("$key -> $value")}
        println("Reward distributions: generated ${this.rewardData.size} unique reward observations")
        //rewardData.forEach{key, value -> println("$key -> $value")}
        println()
        println("Decision Tree:")
        //trainData.forEach{el -> println(el)}
        //println(tree.trainingData.toString())
        //println(tree.trainingData.toSummaryString())
        //println(tree.tree.toString())
        //tree.trainingData.forEach { el -> println(el.toString())}
        println("Updated decision tree ${tree.timesTrained} times")
        println("total predictions: ${totalPredictions}")
        println("erroneous predictions: ${erroneousPredictions}")
    }

    fun evaluate(grid1: Grid, action : Int, grid2: Grid){
        val predictedGrid = this.predictGrid(grid1, action)
        val predicted = predictedGrid.grid.asSequence()
        val real = grid2.grid.asSequence()
        val correctlyPredicted = (predicted zip real).count{(a,b) -> a.equals(b)}

        totalPredictions += predicted.count()
        erroneousPredictions += predicted.count()-correctlyPredicted
    }
}
