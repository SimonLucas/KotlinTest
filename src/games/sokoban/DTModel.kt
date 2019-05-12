package games.sokoban

import agents.RandomAgent
import forwardmodels.decisiontree.MultiClassDecisionTree
import ggi.AbstractGameState


const val pretrainTicks = 100
const val pretrainGames = 100
const val trainDifferentLevels = false
var debug = false
var previousValue = 0
var useFastPrediction = true

class DTModel(private val gridIterator: GridIterator,
              tree : MultiClassDecisionTree? = null) : ForwardGridModel, GridModel {
    var totalAnalysedPatterns = 0
    private var tileData = HashSet<Example>()
    private var tree : MultiClassDecisionTree
    private var erroneousPredictions = 0
    private var totalPredictions = 0
    private var grid : SimpleGrid = SimpleGrid(0,0)
    var score = 0.0
    private var bypassScore = true
    private var trainable = true

    init {
        if (tree == null) {
            this.tree = MultiClassDecisionTree(gridIterator)
        } else {
            this.tree = tree
            this.trainable = false
        }
    }

    override fun getGrid() : SimpleGrid { return grid }
    override fun setGrid(simpleGrid: SimpleGrid) {
        grid = simpleGrid
    }

    fun getTreeInfoString():String{
        return this.tree.treeInfoString
    }

    private fun train(){
        println("Pretraining Phase")

        for (i in 0 until pretrainGames) {
            if (!trainDifferentLevels)
                trainingGame(-1)
            else
                trainingGame(i%50)
            if (debug){
                println("$erroneousPredictions out of $totalPredictions")
                //println("${erroneousPredictions-previousValue}, $erroneousPredictions, ${i* pretrainTicks}")
                //previousValue = erroneousPredictions
            }

        }

        println("finished training")
        println("$pretrainGames trained games; $pretrainTicks ticks each")
        println("tree was updated ${tree.timesTrained} times")
        println("total analysed patterns = $totalAnalysedPatterns")
        println()
    }

    private fun trainingGame(level : Int){
        val game = Sokoban(level)
        val agent = RandomAgent()
        val actions = intArrayOf(0, 0)

        if (debug) {
            println("training on level: $level")
            game.print()
            println()
        }


        for (i in 0 until pretrainTicks) {
            actions[0] = agent.getAction(game.copy(), Constants.player1)

            //prepare data for training
            val grid1 = game.board.deepCopy()
            if (grid1.getCell(grid1.playerX, grid1.playerY) == 'o')
                grid1.setCell(grid1.playerX, grid1.playerY, 'u')
            else
                grid1.setCell(grid1.playerX, grid1.playerY, 'A')

            game.next(actions)

            val grid2 = game.board.deepCopy()
            if (grid2.getCell(grid2.playerX, grid2.playerY) == 'o')
                grid2.setCell(grid2.playerX, grid2.playerY, 'u')
            else
                grid2.setCell(grid2.playerX, grid2.playerY, 'A')

            if (debug) this.evaluate(grid1, actions[0], grid2)
            this.addGrid(grid1, grid2, actions[0], 0.0)    //gather new patterns and retrain
        }
    }

    private fun predictGrid(grid: GridInterface, action: Int) : SimpleGrid {
        val predictedGrid : SimpleGrid
        if (useFastPrediction) {
            predictedGrid = tree.predict(grid, action)
        }
        else {
            predictedGrid = SimpleGrid(grid.getWidth(), grid.getHeight())
            for (x in 0 until grid.getWidth()) {
                for (y in 0 until grid.getHeight()) {
                    val ip = extractVector(grid, x, y)
                    predictedGrid.setCell(x,y,tree.predictCell(ip, action).single())
                }
            }
        }
        return predictedGrid
    }

    private fun predictGridWithTrueValues(grid1: GridInterface, action: Int, grid2: GridInterface) : SimpleGrid {
        val predictedGrid = SimpleGrid(grid.getWidth(), grid.getHeight())
        for (x in 0 until grid1.getWidth()) {
            for (y in 0 until grid1.getHeight()) {
                val ip = extractVector(grid1, x, y)
                predictedGrid.setCell(x,y,tree.predictCell(ip, action).single())
                if (predictedGrid.getCell(x,y) != grid2.getCell(x,y)){
                    println("before")
                    showPatternAt(grid1, x, y)
                    println()
                    println("after")
                    showPatternAt(grid2, x, y)
                    println("Action: $action")
                    println("True result: ${grid2.getCell(x,y)}")
                    println("predicted Result: ${tree.predictCell(ip, action)}")

                    println()
                    if (totalTicks > 1000)
                        tree.predictCell(ip, action)
                }
            }
        }
        return predictedGrid
    }

    override fun addGrid(grid1: GridInterface, grid2: GridInterface, action: Int, reward: Double) {
        assert(grid1.getWidth() == grid2.getWidth() && grid1.getHeight() == grid2.getHeight())
        if (!trainable)
            return

        if (useFastPrediction) {
            //tree.addGrid(grid1, grid2, action)
            //totalAnalysedPatterns += grid1.getHeight() * grid1.getWidth()
            //
            val it = tree.gridIterator;
            it.setGrid(grid1)

            for (x in 0 until grid1.getWidth()) {
                for (y in 0 until grid1.getHeight()) {
                    val op = grid2.getCell(x, y)
                    val ip = extractVector(it, x, y)
                    val example = Example(ip, action)

                    if (!tileData.contains(example)) {
                        tree.addDataPoint(example.ip, example.action, op)
                        tileData.add(example)
                    }
                    totalAnalysedPatterns++
                }
            }
            //println("number of patterns $totalAnalysedPatterns")
        } else {
            for (x in 0 until grid1.getWidth()) {
                for (y in 0 until grid1.getHeight()) {
                    val op = grid2.getCell(x, y)
                    val ip = extractVector(grid1, x, y)
                    val example = Example(ip, action)

                    if (!tileData.contains(example)) {
                        tree.addDataPoint(example.ip, example.action, op)
                        tileData.add(example)
                    }

                    totalAnalysedPatterns++
                }
            }
        }
        tree.updateTree()
    }

    fun extractVector(it: GridIterator, x:Int, y:Int):ArrayList<Char> {
        val a = ArrayList<Char>()
        it.setCell(x,y)
        it.forEach {a.add(it)}
        return a
    }

    // should really generalise this to offer different extraction patterns
    private fun extractVector(grid: GridInterface, x: Int, y: Int, span: Int = 2): ArrayList<Char> {
        val v = ArrayList<Char>()
        // add the centre cell
        v.add(grid.getCell(x,y))
        //println("x:$x; y:$y")
        // now row except centre

        for (xx in x - span .. x + span) {
            if (xx != x) {
                //println("x:$xx; y:$y")
                if (xx >= 0 && xx < grid.getWidth())
                    v.add(grid.getCell(xx, y))
                else
                    v.add('x')
            }
        }
        // now column except centre
        for (yy in y - span .. y + span) {
            if (yy != y) {
                //println("x:$x; y:$yy")
                if (yy >= 0 && yy < grid.getHeight())
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
        println()
        println("Decision Tree:")
        //trainData.forEach{el -> println(el)}
        //println(tree.trainingData.toString())
        //println(tree.trainingData.toSummaryString())
        //println(tree.tree.toString())
        //tree.trainingData.forEach { el -> println(el.toString())}
        println("Updated decision tree $tree.timesTrained times")
        println("total predictions: $totalPredictions")
        println("erroneous predictions: $erroneousPredictions")
    }

    fun evaluate(grid1: Grid, action : Int, grid2: Grid){
        val predictedGrid = if (printDetails)
            this.predictGridWithTrueValues(grid1, action, grid2)
        else
            this.predictGrid(grid1, action)

        val predicted = predictedGrid.grid.asSequence()
        val real = grid2.grid.asSequence()
        val correctlyPredicted = (predicted zip real).count{(a,b) -> a == b }

        totalPredictions += predicted.count()
        erroneousPredictions += predicted.count()-correctlyPredicted
        if (printErrors)
            println("$totalTicks, $erroneousPredictions")
    }

    private fun showPatternAt(grid: GridInterface, x: Int, y: Int){
        println("    ${grid.getCell(x,y-2)}    ")
        println("    ${grid.getCell(x,y-1)}    ")
        println("${grid.getCell(x-2,y-0)},${grid.getCell(x-1,y-0)},${grid.getCell(x,y-0)}," +
                "${grid.getCell(x+1,y-0)},${grid.getCell(x+2,y-0)}")
        println("    ${grid.getCell(x,y+1)}    ")
        println("    ${grid.getCell(x,y+2)}    ")
    }

    companion object Ticker {
        var total: Long = 0
    }

//    override fun setGridArray(array: CharArray, playerX: Int, playerY: Int): ForwardGridModel {
//        grid.setGridKeepPlayerCell(array, playerX, playerY)
//        return this
//    }

    override fun copy(): AbstractGameState {
        val dtm = DTModel(this.gridIterator,  tree)
        dtm.grid = grid.deepCopy()
        dtm.score = score
        dtm.trainable = false
        return dtm
    }

    override fun next(actions: IntArray): AbstractGameState {
        val action = actions[0]
        val nextGrid = predictGrid(grid, action)
        grid = nextGrid
        nTicks++
        total++
        return this
    }

    override fun nActions(): Int {
        // for now just return the correct answer for Sokoban
        return 5
    }

    override fun score(): Double {
        return if(bypassScore) countScore() else score
    }

    private fun countScore(): Double {
        return grid.grid.count { t -> t == '+' }.toDouble()
    }

    override fun isTerminal(): Boolean {
        // return false for now as we don't have a way of learning this yet
        return false
    }

    var nTicks = 0
    override fun nTicks(): Int {
        return nTicks
    }

    override fun totalTicks(): Long {
        return total
    }

    override fun resetTotalTicks() {
        total = 0
    }

    override fun randomInitialState(): AbstractGameState {
        // deliberately do nothing for now
        println("Not able to set a random initial state")
        return this
    }

    override fun toString() : String {
        return "DTModel:\t ${gridIterator.report()};\t ${tree.treeInfoString}"
    }
}