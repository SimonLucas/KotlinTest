package games.eventqueuegame

fun simpleScoreFunction(cityValue: Double, forceValue: Double): (EventQueueGame, Int) -> Double {
    return { game: EventQueueGame, player: Int ->
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


fun specificTargetScoreFunction(targets: List<Int>, targetValue: Double = 100.0, ownForceValue: Double = 1.0, enemyForceValue: Double = -1.0) {

}