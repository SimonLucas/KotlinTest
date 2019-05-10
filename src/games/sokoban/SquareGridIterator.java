package games.sokoban;


public class SquareGridIterator implements GridIterator {
    private int x;
    private int y;
    private GridInterface grid;
    private int size;
    private int cursor_i;
    private int cursor_j;

    public int getMaxElements() {
        return maxElements;
    }

    private int maxElements;

    public SquareGridIterator(int size) {
        this.size = size;
        this.maxElements = (size*2+1)*(size*2+1);
        this.x = 0;
        this.y = 0;
    }

    public void setCell(int x, int y){
        this.x = x;
        this.y = y;
        this.cursor_i = -size;
        this.cursor_j = -size;
    }

    public void setGrid(GridInterface grid){
        this.grid = grid;
    }

    private Character getCell(int x, int y){
        if (x >= 0 && x < grid.getWidth() && y >= 0 && y < grid.getHeight())
            return grid.getCell(x, y);
        else
            return 'x';
    }

    public boolean hasNext() {
        return cursor_i <= size && cursor_j <= size;
    }

    public String[] getHeader(){
        int prev_cursor_i = this.cursor_i;
        int prev_cursor_j = this.cursor_j;

        cursor_i = -size;
        cursor_j = -size;

        String[] header = new String[maxElements];

        for (int i = 0; i < this.maxElements; i++) {
            header[i] = "(" + cursor_i +"," + cursor_j + ")";

            this.cursor_i += 1;
            if (this.cursor_i > size){
                this.cursor_i = -size;
                this.cursor_j += 1;
            }
        }

        this.cursor_i = prev_cursor_i;
        this.cursor_j = prev_cursor_j;
        return header;
    }

    public Character next() {
        assert grid != null;
        char c = getCell(x+cursor_i, y+cursor_j);
        //System.out.println("cursor_i: " + cursor_i + "; cursor_j: " + cursor_j);
        this.cursor_i += 1;
        if (this.cursor_i > size){
            this.cursor_i = -size;
            this.cursor_j += 1;
        }
        return c;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public static void main(String[] args){
        Grid g = new Sokoban().getBoard();
        g.print();
        System.out.println();

        SquareGridIterator iterator = new SquareGridIterator(2);

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
        return "SquareGridIterator(" + this.size + ")";
    }
}