package games.simplegridgame

import agents.DoNothingAgent
import utilities.JEasyFrame
import views.GridView
import java.awt.Color
import java.awt.FlowLayout
import javax.swing.JComponent

fun main() {

    // create a random game state, copy it, then run it forward using different rules
    // for each one create multiple views and add them to a Panel
    val g1 = SimpleGridGame(10, 10)
    val g2 = g1.copy() as SimpleGridGame
    g2.updateRule = CaveUpdateRule()
    stateTrajectory(g1)
    stateTrajectory(g2)
}

fun stateTrajectory(game: SimpleGridGame) {
    val reps = 5
    val listView = ListComponent()

    for (i in 0 until reps) {
        val gv = GridView(game.grid.deepCopy())
        listView.add(gv)
        game.next(intArrayOf(DoNothingAgent().getAction(game, 0)))
    }

    JEasyFrame(listView, "State Trajectory")

}


internal class ListComponent : JComponent() {
    init {
        background = Color.getHSBColor(0.7f, 1.0f, 1.0f)
        layout = FlowLayout(FlowLayout.CENTER, 5, 5)
    }

    //        public void add(PairView pairView) {
    //            add(pairView);
    //        }

}

