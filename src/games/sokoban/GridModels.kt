package games.sokoban

interface GridInterface {
    fun getCell(x: Int, y: Int): Char
    fun setCell(x: Int, y: Int, value: Char)
    fun getWidth() : Int
    fun getHeight() : Int
}


data class SimpleGrid(val w: Int = 8, val h: Int = 7) : GridInterface {

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

    override fun getWidth() : Int {
        return this.w
    }

    override fun getHeight() : Int {
        return this.h
    }

    fun deepCopy(): SimpleGrid {
        val gc = this.copy()
        gc.grid = grid.copyOf()
        return gc
    }
}

// should really generalise this to offer different extraction patterns
fun extractVector(grid: GridInterface, x: Int, y: Int): ArrayList<Char> {
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


