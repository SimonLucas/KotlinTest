package games.sokoban

import agents.RandomAgent
import ggi.SimplePlayerInterface


class ModelTrainer(val trainLevels: IntRange = 0..9,
                   val nStartsPerLevel:Int = 100,
                   val nStepsPerLevel:Int = 100) {

    fun trainModel(model: GridModel, agent: SimplePlayerInterface = RandomAgent(99))  {
        val models = ArrayList<GridModel>()
        models.add(model)
        this.trainModel(models, agent)
    }

    fun trainModel(models: ArrayList<GridModel>, agent: SimplePlayerInterface = RandomAgent(99)){
        val actions = intArrayOf(0,0)

        for (i in trainLevels) {
            for (j in 0 until nStartsPerLevel) {
                val game = Sokoban(i)
                for (k in 0 until nStepsPerLevel) {
                    val action = agent.getAction(game, Constants.player1)
                    actions[0] = action
                    val score = game.score()
                    val grid1 = game.board.getSimpleGrid()
                    game.next(actions)
                    val grid2 = game.board.getSimpleGrid()

                    for (model in models)
                        model.addGrid(grid1, grid2, action, game.score() - score)
                }
            }
        }
    }
}