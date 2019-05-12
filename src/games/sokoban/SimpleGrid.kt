package games.sokoban

data class SimpleGrid(val w: Int, val h: Int) : GridInterface {

    // constructor(fullGrid: )

    var grid: CharArray = CharArray(w * h)

    fun getCell(i: Int): Char = grid[i]

    fun setCell(i: Int, v: Char) {
        grid[i] = v
    }

    override fun getCell(x: Int, y: Int): Char {
        val xx = (x + w) % w
        val yy = (y + h) % h
        return grid[xx + w * yy]
    }

    override fun setCell(x: Int, y: Int, value: Char) {
        if (x < 0 || y < 0 || x >= w || y >= h) return
        grid[x + w * y] = value
    }

    override fun getWidth(): Int {
        return this.w
    }

    override fun getHeight(): Int {
        return this.h
    }

    fun deepCopy(): SimpleGrid {
        val gc = this.copy()
        gc.grid = grid.copyOf()
        return gc
    }

    //    fun setGridArray(grid: CharArray) : SimpleGrid {
//        this.grid = grid.copyOf()
//        return this
//    }

    // todo - try commenting this out to check that all code is using the
    // keepPlayerCell option
    fun setGrid(grid: CharArray, playerX: Int, playerY: Int): SimpleGrid {
        this.grid = grid.copyOf()
        // this is a quick hack for now
        setCell(playerX, playerY, 'A')
        return this
    }

    fun setGridKeepPlayerCell(grid: CharArray, playerX: Int, playerY: Int): SimpleGrid {
        this.grid = grid.copyOf()
        if (getCell(playerX, playerY) == 'o')
            setCell(playerX, playerY, 'u')
        else
            setCell(playerX, playerY, 'A')

        return this
    }

    fun print() {
        for (i in 0 until grid.size) {
            print(grid[i])
            if ((i + 1) % w == 0)
                println()
        }
    }
}

