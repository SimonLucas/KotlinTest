package games.eventqueuegame

fun simpleScoreFunction(cityValue: Double, forceValue: Double): (EventQueueGame) -> Double {
    return {
        // 5 points per city
        val blueCities = it.world.cities.count { c -> c.owner == PlayerId.Blue }
        val redCities = it.world.cities.count { c -> c.owner == PlayerId.Red }
        // then add the total of all forces
        val blueForces = it.world.cities.filter { c -> c.owner == PlayerId.Blue }.sumByDouble(City::pop) +
                it.world.currentTransits.filter { t -> t.playerId == PlayerId.Blue }.sumByDouble(Transit::nPeople)
        val redForces = it.world.cities.filter { c -> c.owner == PlayerId.Red }.sumByDouble(City::pop) +
                it.world.currentTransits.filter { t -> t.playerId == PlayerId.Red }.sumByDouble(Transit::nPeople)
        cityValue * (blueCities - redCities) + (blueForces - redForces) * forceValue
    }
}