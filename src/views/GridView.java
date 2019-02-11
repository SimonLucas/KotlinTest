package views;

import games.gridgame.Grid;
import games.gridgame.GridGame;

import javax.swing.*;
import java.awt.*;

public class GridView extends JComponent {

    int cellSize = 20;
    float inc = 0.01f;
    GridGame gridGame;
    boolean deadBlack = true;

    public GridView(GridGame gridGame) {
        this.gridGame = gridGame;
    }

    public void paintComponent(Graphics g) {
        Grid grid = gridGame.getGrid();
        int n = grid.getW() * grid.getH();
        for (int i = 0; i < n; i++) {
            float h = grid.getCell(i) == 0 ? 0.35f : 0.89f;
            g.setColor(Color.getHSBColor(h, 1, 1));
            if (deadBlack && grid.getCell(i) == 0) g.setColor(Color.black);
            int x = cellSize * (i % grid.getW()), y = cellSize * (i / grid.getW());
            g.fillRect(x, y, cellSize, cellSize);
        }
    }

    public Dimension getPreferredSize() {
        Grid grid = gridGame.getGrid();
        return new Dimension(cellSize * grid.getW(), cellSize * grid.getH());
    }
}
