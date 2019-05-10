package games.sokoban;

import java.util.Iterator;

public class CrossGridIterator implements GridIterator {
    private int x;
    private int y;
    private GridInterface grid;
    private int size;
    private int cursor_i;
    private int cursor_j;
    private int maxElements;
    private boolean horizontal;

    public int getMaxElements() {
        return maxElements;
    }

    public CrossGridIterator(int size) {
        this.size = size;
        this.maxElements = (size*2+1)+(size*2+1)-1;
        this.x = 0;
        this.y = 0;
    }

    public void setCell(int x, int y) {
        this.x = x;
        this.y = y;
        this.horizontal = false;
        this.cursor_i = -size;
        this.cursor_j = -size;
    }

    public void setGrid(GridInterface grid) {
        this.grid = grid;
    }

    public String[] getHeader() {
        int prev_cursor_i = this.cursor_i;
        int prev_cursor_j = this.cursor_j;

        cursor_i = -size;
        cursor_j = 0;

        String[] header = new String[maxElements];
        header[0] = "(0,0)";

        int el = 1;
        for (cursor_i = -size; cursor_i <= size; cursor_i++) {
            if (cursor_i != 0) {
                header[el] = "(" + cursor_i +"," + cursor_j + ")";
                el++;
            }
        }

        cursor_i = 0;
        for (cursor_j = -size; cursor_j <= size; cursor_j++) {
            if (cursor_j != 0) {
                header[el] = "(" + cursor_i +"," + cursor_j + ")";
                el++;
            }
        }

        this.cursor_i = prev_cursor_i;
        this.cursor_j = prev_cursor_j;
        return header;
    }

    public boolean hasNext() {
        return cursor_j <= size;
    }

    private Character getCell(int x, int y){
        assert grid != null;
        if (x >= 0 && x < grid.getWidth() && y >= 0 && y < grid.getHeight())
            return grid.getCell(x, y);
        else
            return 'x';
    }

    public Character next() {
        char c;
        if (this.horizontal){
            c = getCell(x+cursor_i, y);
            cursor_i += 1;
            if (cursor_i == 0) cursor_i++;
            if (cursor_i > size) horizontal = false;
        } else {
            if (cursor_i == -size){
                c = getCell(x,y);
                horizontal = true;
            } else {
                c = getCell(x, y+cursor_j);
                cursor_j += 1;
                if (cursor_j == 0) cursor_j++;
            }

        }
        return c;
    }

    public static void main(String[] args){
        Grid g = new Sokoban().getBoard();
        g.print();
        System.out.println();

        CrossGridIterator iterator = new CrossGridIterator(2);

        System.out.println("getHeader");
        for (String s : iterator.getHeader()) {
            System.out.println(s);
        }
        System.out.println();

        System.out.println("getPattern around cell x,y");
        iterator.setGrid(g);
        for (iterator.setCell(1,1); iterator.hasNext();) {
            Character c = iterator.next();
            System.out.println(c.toString());
        }
    }

    public String report(){
        return "CrossGridIterator(" + this.size + ")";
    }
}
