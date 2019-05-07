package games.eventqueuegame

import games.gridgame.Grid
import utilities.DrawUtil
import java.awt.*
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D
import javax.swing.JComponent

class WorldView (var game: EventQueueGame) : JComponent() {

    val oliveGreen = Color(84, 79, 61)

    val dim = Dimension(400, 250)
    val outline = Color.lightGray
    val playerCols = hashMapOf<PlayerId,Color>(
            PlayerId.Neutral to Color.getHSBColor(0.3f, 0.8f, 0.8f),
            PlayerId.Blue to Color.getHSBColor(0.57f, 0.9f, 0.9f),
            PlayerId.Red to Color.getHSBColor(0.0f, 0.9f, 0.9f),
            PlayerId.Fog to Color.gray
    )

    public override fun paintComponent(old: Graphics) {

        val world = game.world
        val g = old as Graphics2D
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.stroke = BasicStroke(5f)
        with (world) {

     //       val b = (1 + Math.sin(game.nTicks().toDouble() * 0.01 * Math.PI).toFloat()) / 2f
            // println(b)
            g.color = oliveGreen
            g.fillRect(0, 0, getWidth(), getHeight())

            // now need to work out a scale
            val xScale = getWidth() / width.toDouble()
            val yScale = getHeight() / height.toDouble()

            // now scale things accordingly

            for (r in routes) {
                g.setColor(outline)
                g.drawLine((cities[r.fromCity].location.x * xScale).toInt(), (cities[r.fromCity].location.y * yScale).toInt(),
                        (cities[r.toCity].location.x * xScale).toInt(), (cities[r.toCity].location.y * yScale).toInt())
            }

            for (c in cities) {
                val ellipse = Ellipse2D.Double(xScale * (c.location.x-c.radius), yScale * (c.location.y - c.radius),
                        2 * c.radius * xScale, 2 * c.radius * yScale)
                g.setColor(outline)
                g.draw(ellipse)
                g.setColor(playerCols[c.owner])
                g.fill(ellipse)
                val label = if (c.owner == PlayerId.Fog) "?" else  "${c.pop}"
                DrawUtil().centreString(g, label, xScale * c.location.x, yScale*c.location.y)
            }

            for (t in currentTransits) {
                val fractionOfJourney: Double = (currentTicks - t.startTime).toDouble() / (t.endTime - t.startTime).toDouble()
                val currentLocation = cities[t.fromCity].location.plus(cities[t.toCity].location.minus(cities[t.fromCity].location).times(fractionOfJourney))
                val ellipse = Ellipse2D.Double(xScale * (currentLocation.x-3.0), yScale * (currentLocation.y-3.0),
                        2 * 3.0 * xScale, 2 * 3.0 * yScale)
                g.setColor(playerCols[t.playerId])
                g.draw(ellipse)
                g.fill(ellipse)
                val label = "${t.nPeople}"
                g.setColor(Color.white)
                DrawUtil().centreString(g, label, xScale * currentLocation.x, yScale*currentLocation.y)
            }
        }
    }

//    fun drawCities(Graphics2D g) {
//
//    }

    override fun getPreferredSize(): Dimension {
        return dim
    }
}
