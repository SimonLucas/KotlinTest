package games.eventqueuegame

import games.gridgame.Grid
import utilities.DrawUtil
import java.awt.*
import java.awt.geom.*
import java.awt.geom.Rectangle2D
import javax.swing.JComponent
import kotlin.math.roundToInt

class WorldView(var game: EventQueueGame) : JComponent() {

    val oliveGreen = Color(84, 79, 61)

    val dim = Dimension(400, 250)
    val outline = Color.lightGray
    val playerCols = hashMapOf<PlayerId, Color>(
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
        with(world) {

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
                val ellipse: Shape = if (c.fort) {
                    Rectangle2D.Double(xScale * (c.location.x - c.radius), yScale * (c.location.y - c.radius),
                            2 * c.radius * xScale, 2 * c.radius * yScale)
                } else {
                    Ellipse2D.Double(xScale * (c.location.x - c.radius), yScale * (c.location.y - c.radius),
                            2 * c.radius * xScale, 2 * c.radius * yScale)
                }
                g.setColor(outline)
                g.draw(ellipse)
                g.setColor(playerCols[c.owner])
                g.fill(ellipse)
                val label = if (c.owner == PlayerId.Fog) "?" else "${c.pop.roundToInt()}"
                DrawUtil().centreString(g, label, xScale * c.location.x, yScale * c.location.y)
                DrawUtil().centreString(g, c.name, xScale * (c.location.x + params.minRad + 20), yScale * (c.location.y + params.minRad + 20), Color.WHITE)
            }

            for (t in currentTransits) {
                val currentLocation = t.currentPosition(world.currentTicks, cities)
                val ellipse = Ellipse2D.Double(xScale * (currentLocation.x - 3.0), yScale * (currentLocation.y - 3.0),
                        2 * 3.0 * xScale, 2 * 3.0 * yScale)
                g.setColor(playerCols[t.playerId])
                g.draw(ellipse)
                g.fill(ellipse)
                val label = "${t.nPeople.roundToInt()}"
                g.setColor(Color.white)
                DrawUtil().centreString(g, label, xScale * currentLocation.x, yScale * currentLocation.y)
            }

            for (t in game.targets[0]) {
                DrawUtil().centreString(g, "TGT", xScale * (cities[t].location.x + params.minRad + 40),
                        yScale * (cities[t].location.y + params.minRad), playerCols[PlayerId.Blue])
            }
            for (t in game.targets[1]) {
                DrawUtil().centreString(g, "TGT", xScale * (cities[t].location.x + params.minRad + 40),
                        yScale * (cities[t].location.y + params.minRad - 40), playerCols[PlayerId.Red])
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
