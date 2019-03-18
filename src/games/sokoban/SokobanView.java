package games.sokoban;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class SokobanView extends JComponent {

    final char EMPTY = '.';
    final char BOX = '*';
    final char HOLE = 'o';
    final char AVATAR = 'A';
    final char WALL  = 'w';
    final char BOXIN = '+';

    int cellSize = 50;
    // GridGame gridGame;
    public Grid grid;
    boolean deadBlack = true;

    public boolean gridLines = true;

//    public GridView(GridGame gridGame) {
//        this.gridGame = gridGame;
//    }





    public SokobanView(Grid grid) {
        this.grid = grid;
    }

    public void paintComponent(Graphics g) {
        int n = grid.getW() * grid.getH();
        for (int i = 0; i < n; i++) {
            Color col = new Color(1,1,1);
            char tile = grid.getCell(i);


            int pX = grid.getPlayerX();
            int pY = grid.getPlayerY();
            if (pX == i % grid.getWidth() && pY == (i / grid.getHeight()) - 1) {
                g.setColor(Color.CYAN);
            } else {
                switch (tile) {
                    case EMPTY:
                        col = Color.WHITE;
                        break;
                    case BOX:
                        col = new Color(250, 160, 0);
                        break;
                    case HOLE:
                        col = Color.BLACK;
                        break;
                    case WALL:
                        col = Color.DARK_GRAY;
                        break;
                    case BOXIN:
                        col = new Color(250, 212, 0);
                        break;
                }
                g.setColor(col);
            }


//            float h = grid.getCell(i) == 0 ? 0.35f : 0.89f;
//            g.setColor(Color.getHSBColor(h, 1, 1));
//            if (deadBlack && grid.getCell(i) == 0) g.setColor(Color.black);

            int x = cellSize * (i % grid.getW()), y = cellSize * (i / grid.getW());
            g.fillRect(x, y, cellSize, cellSize);
        }
        // paint faint gridlines separately
        if (gridLines) {
            g.setColor(new Color(128, 128, 128, 128));
            for (int i = 0; i < n; i++) {
                int x = cellSize * (i % grid.getW()), y = cellSize * (i / grid.getW());
                g.drawRect(x, y, cellSize, cellSize);
            }
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension(cellSize * grid.getW(), cellSize * grid.getH());
    }
}
