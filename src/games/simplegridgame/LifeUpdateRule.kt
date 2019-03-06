package games.simplegridgame

import games.gridgame.Grid
import games.gridgame.UpdateRule
import games.gridgame.vectorExtractor

class LifeUpdateRule : UpdateRule {
    override fun cellUpdate(grid: Grid, x: Int, y: Int): Int {
        val cells =  vectorExtractor(grid, x, y)
        return SimpleGridGame().lifeRule(cells)
    }
}

class CaveUpdateRule : UpdateRule {
    val t = 4
    override fun cellUpdate(grid: Grid, x: Int, y: Int): Int {
        val cells =  vectorExtractor(grid, x, y)
        return if (cells.sum() > t) 1 else 0
    }
}

