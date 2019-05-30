package games.eventqueuegame

import ggi.game.*
import kotlin.math.roundToInt

object LandCombatStateFunction : StateSummarizer {
    override fun invoke(state: ActionAbstractGameState): String {
        if (state is LandCombatGame) {
            // features to use...
            // for each city: ownership, population
            // for each route/player: number of transits, total population of transits
            return with(StringBuilder()) {
                state.world.cities.forEach {
                    append(playerIDToNumber(it.owner))
                    append(",")
                    append(it.pop.roundToInt())
                    append("|")
                }
                state.world.routes.forEach { arc ->
                    val transitsOnArcByPlayer = state.world.currentTransits.filter { t -> t.fromCity == arc.fromCity && t.toCity == arc.toCity }.groupBy { it.playerId }
                    append(transitsOnArcByPlayer[PlayerId.Blue]?.count() ?: 0)
                    append(",")
                    append(transitsOnArcByPlayer[PlayerId.Red]?.count() ?: 0)
                    append(",")
                    append(transitsOnArcByPlayer[PlayerId.Blue]?.sumByDouble(Transit::nPeople)?.roundToInt() ?: 0)
                    append(",")
                    append(transitsOnArcByPlayer[PlayerId.Red]?.sumByDouble(Transit::nPeople)?.roundToInt() ?: 0)
                    append("|")
                }
                this
            }.toString()
        }
        return ""
    }
}