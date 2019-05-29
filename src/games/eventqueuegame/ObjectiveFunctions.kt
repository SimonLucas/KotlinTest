package games.eventqueuegame

import java.lang.AssertionError

fun simpleScoreFunction(cityValue: Double, forceValue: Double): (LandCombatGame, Int) -> Double {
    return { game: LandCombatGame, player: Int ->
        val sign = if (player == 0) +1 else -1
        with(game.world) {
            val blueCities = cities.count { c -> c.owner == PlayerId.Blue }
            val redCities = cities.count { c -> c.owner == PlayerId.Red }
            // then add the total of all forces
            val blueForces = cities.filter { c -> c.owner == PlayerId.Blue }.sumByDouble(City::pop) +
                    currentTransits.filter { t -> t.playerId == PlayerId.Blue }.sumByDouble(Transit::nPeople)
            val redForces = cities.filter { c -> c.owner == PlayerId.Red }.sumByDouble(City::pop) +
                    currentTransits.filter { t -> t.playerId == PlayerId.Red }.sumByDouble(Transit::nPeople)
            sign * (cityValue * (blueCities - redCities) + (blueForces - redForces) * forceValue)
        }
    }
}


fun specificTargetScoreFunction(targetValue: Double = 100.0,
                                ownForceValue: Double = 1.0, enemyForceValue: Double = -1.0): (LandCombatGame, Int) -> Double {
    return { game: LandCombatGame, player: Int ->
        val playerColour = when (player) {
            0 -> PlayerId.Blue
            1 -> PlayerId.Red
            else -> throw AssertionError("Unknown Player")
        }
        with(game.world) {
            val targetsAcquired = game.targets[playerColour]?.count { i -> cities[i].owner == playerColour} ?: 0
            val ourForces = cities.filter { c -> c.owner == playerColour }.sumByDouble(City::pop) +
                    currentTransits.filter { t -> t.playerId == playerColour }.sumByDouble(Transit::nPeople)
            val enemyForces = cities.filter { c -> c.owner != playerColour }.sumByDouble(City::pop) +
                    currentTransits.filter { t -> t.playerId != playerColour }.sumByDouble(Transit::nPeople)
            targetsAcquired * targetValue + ourForces * ownForceValue - enemyForces * enemyForceValue
        }
    }
}