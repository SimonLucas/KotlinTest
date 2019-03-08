package views;

import games.gridgame.Grid;
import games.gridgame.GridGame;

import javax.swing.*;
import java.awt.*;

public class GridView extends JComponent {

    int cellSize = 20;
    // GridGame gridGame;
    public Grid grid;
    boolean deadBlack = true;

    public boolean gridLines = true;

//    public GridView(GridGame gridGame) {
//        this.gridGame = gridGame;
//    }


    public GridView(Grid grid) {
        this.grid = grid;
    }

    public void paintComponent(Graphics g) {
        int n = grid.getW() * grid.getH();
        for (int i = 0; i < n; i++) {
            float h = grid.getCell(i) == 0 ? 0.35f : 0.89f;
            g.setColor(Color.getHSBColor(h, 1, 1));
            if (deadBlack && grid.getCell(i) == 0) g.setColor(Color.black);
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
