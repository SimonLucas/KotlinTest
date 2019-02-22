package games.breakout;

import math.Vector2d;
import utilities.DrawUtil;
import utilities.JEasyFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

public class BreakoutView extends JComponent {
    int viewWidth = 600, viewHeight = 400;
    public BreakoutGameState gameState = new BreakoutGameState();
    Color brickColor = Color.getHSBColor(0.1f, 1, 1);
    Color batColor = Color.getHSBColor(0.5f, 1, 1);
    Color ballColor = Color.getHSBColor(0.9f, 1, 1);
    Color bg = Color.getHSBColor(0.1f, 0.2f, 0.2f);
    Color faintLine = new Color(1, 1, 1, 0.5f);
    Color scoreColor = Color.white;

    Color[] brickColors = {
            Color.getHSBColor(0.05f, 1, 1),
            Color.getHSBColor(0.15f, 1, 1),
            Color.getHSBColor(0.25f, 1, 1),
            Color.getHSBColor(0.35f, 1, 1),
            Color.getHSBColor(0.45f, 1, 1),
            Color.getHSBColor(0.55f, 1, 1),
            Color.getHSBColor(0.65f, 1, 1),
            Color.getHSBColor(0.75f, 1, 1),
            Color.getHSBColor(0.85f, 1, 1),
    };

    public Dimension getPreferredSize() {
        return new Dimension(viewWidth, viewHeight);
    }

    public void paintComponent(Graphics go) {
        Graphics2D g = (Graphics2D) go;
        // calculate the upper left of the gridGame
        // and how it should map to the game view
        g.setColor(bg);
        g.fillRect(0, 0, getWidth(), getHeight());
        // start by drawing the gridGame
        ViewParams vp = new ViewParams(gameState.getState().getParams());
        drawWall(g, vp);
        drawBat(g, vp);
        drawBall(g, vp);
        drawScore(g, vp);
    }

    private void drawScore(Graphics2D g, ViewParams vp) {
        String message = String.format("Score: %d", (int) gameState.score());
        DrawUtil draw = new DrawUtil();
        draw.setFontSize(20);
        draw.setColor(scoreColor);
        draw.centreString(g, message, getWidth()/2, vp.cellHeight);
    }

    // static double ballSize = 0.02;

    private void drawBall(Graphics2D g, ViewParams vp) {
        Vector2d s = gameState.getState().getBall().getS();
        // System.out.println("Ball: " + s);
        double cx = s.getX() * getWidth();
        double cy = s.getY() * getHeight();


        double pixWidth = getWidth() * vp.ballRad;
        double pixHeight = getHeight() * vp.ballRad;
        Ellipse2D.Double rect = new Ellipse2D.Double(cx - pixWidth / 2, cy - pixHeight / 2, pixWidth, pixHeight);
        g.setColor(ballColor);
        g.fill(rect);
    }

    private void drawBat(Graphics2D g, ViewParams vp) {
        Vector2d s = gameState.getState().getBat();
        // System.out.println("Bat: " + s);
        double cx = s.getX() * getWidth();
        double cy = s.getY() * getHeight();
        double pixWidth = getWidth() * (gameState.getState().getParams().getBatWidth() - vp.ballRad);
        double pixHeight = getHeight() * (gameState.getState().getParams().getBatHeight() - vp.ballRad);

        Rectangle2D.Double rect = new Rectangle2D.Double(cx - pixWidth / 2, cy - pixHeight / 2, pixWidth, pixHeight);
        g.setColor(batColor);
        g.fill(rect);
    }

    void drawWall(Graphics2D g, ViewParams vp) {
        for (int i = 0; i < vp.cellsWide; i++) {
            for (int j = 0; j < vp.cellsHigh; j++) {
                // find centre
                double cx = (i + 0.5) * vp.cellWidth, cy = (j + 0.5) * vp.cellHeight;
//                Rectangle2D.Double brick =
//                        new Rectangle2D.Double(
//                                i * vp.cellWidth, j * vp.cellHeight, vp.cellWidth, vp.cellHeight);

                double pixWidth = vp.cellWidth - getWidth() * vp.ballRad;
                double pixHeight = vp.cellHeight - getHeight() * vp.ballRad;

                Rectangle2D.Double brick =
                        new Rectangle2D.Double(
                                cx - pixWidth / 2,
                                cy - pixHeight / 2,
                                pixWidth, pixHeight);

                g.setColor(getBrickColor(i,j));
                g.fill(brick);

                // g.setColor(faintLine);
                // g.draw(brick);
            }
        }
    }

    private Color getBrickColor(int i, int j) {
        if (gameState.getState().getBricks()[i][j] == 1) {
            if (j < brickColors.length-1) {
                return brickColors[j];
            } else {
                return brickColor;
            }
        }
        else
            return bg;
    }

    class ViewParams {
        ViewParams(BreakoutParams params) {
            this.params = params;
            setup();
        }

        BreakoutParams params;
        double upperLeft;

        int cellsWide, cellsHigh;
        double cellWidth, cellHeight;
        double ballRad, batWidth, batHeight;

        void setup() {
            cellsWide = params.getGridWidth();
            cellsHigh = params.getGridHeight();
            cellWidth = getWidth() * 1.0 / cellsWide;
            cellHeight = getHeight() * 1.0 / cellsHigh;
            ballRad = params.getBallSize();
            batWidth = params.getBatWidth();
            batHeight = params.getBatHeight();
        }
    }

    public static void main(String[] args) {
        new JEasyFrame(new BreakoutView(), "Breakout View");
    }

}
