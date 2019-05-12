package games.sokoban;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class SokobanView extends JComponent {

    final char EMPTY = '.';
    final char BOX = '*';
    final char HOLE = 'o';
    final char AVATAR = 'A';
    final char AVATARONHOLE = 'u';
    final char WALL  = 'w';
    final char BOXIN = '+';

    final boolean useImages = true;
    public Image emptyImage;
    public Image boxImage;
    public Image boxinImage;
    public Image holeImage;
    public Image avatarImage;
    public Image wallImage;


    int cellSize = 50;
    // GridGame gridGame;
    public Grid grid;
    boolean deadBlack = true;

    public boolean gridLines = true;


    public SokobanView(Grid grid) {
        this.grid = grid;
        this.emptyImage = getImage("img/empty.png");
        this.boxImage = getImage("img/box.png");
        this.boxinImage = getImage("img/boxin.png");
        this.holeImage = getImage("img/hole.png");
        this.avatarImage = getImage("img/avatar.png");
        this.wallImage = getImage("img/wall.png");
    }

    public void paintComponent(Graphics g) {
        int n = grid.getW() * grid.getH();
        for (int i = 0; i < n; i++) {

            char tile = grid.getCell(i);

            if(useImages)
            {
                boolean requiresBackground = false;
                Image toDraw = null;
                int pX = grid.getPlayerX();
                int pY = grid.getPlayerY();
                if (pX == i % grid.getWidth() && pY == (i / grid.getWidth())) {
                    toDraw = this.avatarImage;
                    requiresBackground = true;
                }else
                {
                    switch (tile) {
                        case EMPTY:
                            toDraw = emptyImage;
                            break;
                        case BOX:
                            toDraw = boxImage;
                            break;
                        case HOLE:
                            toDraw = holeImage;
                            requiresBackground = true;
                            break;
                        case WALL:
                            toDraw = wallImage;
                            break;
                        case BOXIN:
                            toDraw = boxinImage;
                            break;
                        case AVATAR:
                            toDraw = avatarImage;
                            requiresBackground = true;
                            break;
                        case AVATARONHOLE:
                            toDraw = avatarImage;
                            requiresBackground = true;
                            break;
                    }
                }

                int x = cellSize * (i % grid.getW()), y = cellSize * (i / grid.getW());

                if(requiresBackground)
                {
                    g.drawImage(emptyImage, x, y, cellSize, cellSize, null);
                }
                
                g.drawImage(toDraw, x, y, cellSize, cellSize, null);




            }else {
                Color col = new Color(1,1,1);

                int pX = grid.getPlayerX();
                int pY = grid.getPlayerY();
                if (pX == i % grid.getWidth() && pY == (i / grid.getWidth())) {
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


                int x = cellSize * (i % grid.getW()), y = cellSize * (i / grid.getW());
                g.fillRect(x, y, cellSize, cellSize);
            }
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

    private Image getImage(String image_file)
    {
        try {

            if((new File(image_file).exists())) {
                return ImageIO.read(new File(image_file));
            }

            return ImageIO.read(this.getClass().getResource("/" + image_file));
        } catch (IOException e) {
            //e.printStackTrace();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return null;
    }
}
