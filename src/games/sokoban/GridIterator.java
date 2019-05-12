package games.sokoban;

import java.util.Iterator;

public interface GridIterator extends Iterator<Character> {
    int getMaxElements();
    void setCell(int x, int y);
    void setGrid(GridInterface grid);
    String[] getHeader();
    String report();
}
