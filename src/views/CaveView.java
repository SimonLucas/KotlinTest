package views;

import games.caveswing.*;
import math.Vector2d;
import utilities.DrawUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;

public class CaveView extends JComponent {
    CaveSwingParams params;
    CaveGameState gameState;
    CaveGameInternalState internalState;
    Color bg = Color.black;
    Color deadZone = Color.getHSBColor(0.9f, 1, 1);
    Color goalZone = Color.getHSBColor(0.3f, 1, 1);
    Color finishZone = Color.getHSBColor(0.7f, 1, 1);

    int nStars = 200;
    int rad = 10;
    Color anchorColor = Color.getHSBColor(0.17f, 1, 1);
    Color avatarColor = Color.getHSBColor(0.50f, 1, 1);
    Color fruitColor = Color.gray;

    int scoreFontSize = 16;
    int planetFontSize = 14;
    DrawUtil scoreDraw = new DrawUtil().setColor(Color.white).setFontSize(scoreFontSize);

    public boolean scrollView = true;
    public int scrollWidth = 600;

    public ArrayList<int[]> playouts;

    double scale = 1.0;

    static Stroke stroke = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);


    public CaveView setGameState(CaveGameState gameState) {
        this.gameState = gameState;
        internalState = gameState.getState();
        setParams(internalState.getParams());
        return this;
    }

    public CaveView setParams(CaveSwingParams params) {
        this.params = params;
        setStars();
        return this;
    }

    private void setStars() {
        for (int i = 0; i < nStars; i++) {
            stars.add(new Star());
        }
    }

    public String getTitle() {
        return internalState.getNTicks() + " : " + nPaints;
    }

    public Dimension getPreferredSize() {
        if (scrollView) {
            return new Dimension((int) (scale * scrollWidth), (int) (scale * params.getHeight()));
        } else {
            return new Dimension((int) (scale * params.getWidth()), (int) (scale * params.getHeight()));
        }
    }

    public int nPaints = 0;

    public void paintComponent(Graphics go) {
        Graphics2D g = (Graphics2D) go;
        g.scale(scale, scale);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.fillRect(0, 0, getWidth(), getHeight());

        double xScroll = -internalState.getAvatar().getS().getX() + scrollWidth / 2;
        if (scrollView) {
            g.translate(xScroll, 0);
        }
        paintStars(g);
        paintZones(g);

        paintAnchors(g);
        paintAvatar(g);
        paintItems(g);
        drawPlayouts(g);

        if (scrollView) {
            g.translate(-xScroll, 0);
        }
        // have to paint the score last so that it is not obscured by any game objects
        paintScore(g);
        nPaints++;
    }

    private void paintAnchors(Graphics2D g) {
        g.setColor(Color.white);
        int index = 0;
        Anchor closestAnchor = internalState.getMap().getClosestAnchor(internalState.getAvatar().getS());
        for (Anchor a : internalState.getMap().getAnchors()) {
            if (a.equals(closestAnchor)) {
                g.setColor(anchorColor);
            } else {
                g.setColor(Color.lightGray);
            }
            g.fillOval((int) a.getS().getX() - rad, (int) a.getS().getY() - rad, 2 * rad, 2 * rad);
            index++;
        }
    }

    private void paintItems(Graphics2D g) {
        g.setColor(Color.white);
        // int scale = internalState.getParams();
        for (Item item : internalState.getMap().getItems().values()) {

        }
    }

    static Color ropeColor = new Color(178, 34, 34);


    private void paintAvatar(Graphics2D g) {
        g.setColor(avatarColor);
        Vector2d s = internalState.getAvatar().getS();
        g.fillRect((int) s.getX() - rad, (int) s.getY() - rad, 2 * rad, 2 * rad);
        if (internalState.getCurrentAnchor() != null) {
            // todo: draw a rope to the selected one
            g.setColor(ropeColor);
            g.setStroke(new BasicStroke(rad / 2));
            Vector2d p = internalState.getCurrentAnchor().getS();
            g.drawLine((int) s.getX(), (int) s.getY(), (int) p.getX(), (int) p.getY());
        }
    }

    private void paintScore(Graphics2D g) {
        g.setColor(Color.white);
        int score = (int) gameState.score();
        String message = String.format("%d", score);
        scoreDraw.centreString(g, message, getWidth() / 2, scoreFontSize);
    }

    private void drawPlayouts(Graphics2D g) {
        try {
            g.setColor(new Color(255, 0, 128, 100));
            if (playouts != null) {
                for (int[] seq : playouts) {
                    drawShipPlayout(g, (CaveGameState) gameState.copy(), seq);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawShipPlayout(Graphics2D g, CaveGameState gameState, int[] seq) {

        g.setStroke(stroke);
        Path2D path = new Path2D.Double();
        Vector2d pos = internalState.getAvatar().getS();
        path.moveTo(pos.getX(), pos.getY());
        int playerId = 0;
        for (int a : seq) {
            gameState.next(new int[]{a});
            pos = internalState.getAvatar().getS();
            path.lineTo(pos.getX(), pos.getY());
        }
        g.draw(path);
    }

    ArrayList<Star> stars = new ArrayList();

    private void paintStars(Graphics2D g) {
        for (Star star : stars) star.draw(g);
    }

    static int zoneWidth = 200;
    static int goalRatio = 10;
    static int borderRatio = 20;

    private void paintZones(Graphics2D g) {

        CaveSwingParams p = internalState.getParams();
        g.setColor(deadZone);
        g.fillRect(-zoneWidth, 0, zoneWidth, getHeight());
        g.fillRect(0, 0, p.getWidth(), getHeight() / borderRatio);
        g.fillRect(0, getHeight() - getHeight() / borderRatio, p.getWidth(), getHeight() / borderRatio);

        g.setColor(finishZone);
        g.fillRect(p.getWidth(), 0, zoneWidth, getHeight());

        g.setColor(goalZone);
        g.fillRect(p.getWidth(), 0, zoneWidth / goalRatio, getHeight() / goalRatio);


    }

    class Star {
        int x, y;
        double inc;
        double shine = params.getRandom().nextDouble();

        Star() {
            x = params.getRandom().nextInt(params.getWidth());
            y = params.getRandom().nextInt(params.getHeight());
            inc = 0.1 * (params.getRandom().nextDouble() + 1);
        }

        void draw(Graphics2D g) {
            shine += inc;
            float bright = (float) (1 + Math.sin(shine)) / 2;
            Color grey = new Color(bright, 1 - bright, bright);
            g.setColor(grey);
            g.fillRect(x, y, 2, 2);
        }
    }
}
