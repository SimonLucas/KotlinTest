package games.eventqueuegame

import ggi.AbstractGameState
import ggi.ExtendedAbstractGameState
import math.Vec2d
import java.util.*
import kotlin.random.Random

// todo : Create and test and Event Queue

data class Event (val tick: Int, val effect: Effect) : Comparable<Event> {
    override fun compareTo(other: Event): Int {
        return tick.compareTo(other.tick)
    }
}

enum class PlayerId {
    Blue, Red, Neutral, Fog
}

data class City (val location: Vec2d, var radius: Int = 40, var pop: Int = 100, var owner: PlayerId = PlayerId.Neutral)

data class EventGameParams (
        var nAttempts: Int = 10,
        var width: Int = 1000,
        var height: Int = 600,
        var minRad: Int = 20,
        var maxRad: Int = 100,
        var minPop: Int = 10,
        var maxPop: Int = 100,
        var minSep: Int = 30,
        var seed: Long? = 10
)


data class World (val cities: ArrayList<City> = ArrayList()) {
    var width = 1000
    var height = 600
    var random = Random(1)

    fun randomize(params: EventGameParams = EventGameParams()) : World {
        // just keep it like so
        cities.clear()
        with(params) {
            if (seed != null) random = Random(seed!!)
            for (i in 0 until nAttempts) {
                val rad = minRad + random.nextInt(maxRad - minRad)
                val pop = minPop + random.nextInt(maxPop - minPop)
                val location = Vec2d(rad + random.nextDouble(width - 2*rad.toDouble()),
                        rad + random.nextDouble(height - 2*rad.toDouble()))
                val city = City(location, rad, pop)
                if (canPlace(city, cities, minSep)) cities.add(city)
            }
        }
        return this
    }

    fun canPlace(c: City, cities: ArrayList<City>, minSep: Int) : Boolean {
        for (el in cities)
            if (c.location.distanceTo(el.location) < c.radius + el.radius + minSep) return false
        return true
    }

    fun randomiseIds() : World {
        for (c in cities) c.owner = PlayerId.values()[random.nextInt(PlayerId.values().size)]
        return this
    }
}




interface Effect {
    fun apply(world: World)
}

data class CityInflux(val nPeople: Int, val cityId: Int, val playerId: PlayerId) : Effect {
    override fun apply(world: World) {
        val city = world.cities[cityId]
        if (city.owner == playerId) {
            city.pop += nPeople
        } else {
            city.pop -= nPeople
            if (city.pop <= 0) {
                city.pop = -city.pop
                city.owner = playerId
            }
        }
    }
}

var totalTicks: Long = 0

class EventQueueGame : ExtendedAbstractGameState {

    var world = World()

    var eventQueue = PriorityQueue<Event>()

    var nTicks = 0

    override fun copy(): AbstractGameState {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun next(actions: IntArray): AbstractGameState {
        nTicks++
        totalTicks++

        val event = eventQueue.peek()
        if (event != null && event.tick < nTicks) {
            // the time has come to trigger it
            eventQueue.poll()
            event.effect.apply(world)
            println("Triggered event: ${event}" )
        }
        return this
    }

    override fun nActions(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun score(): Double {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isTerminal(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun nTicks(): Int {
        return nTicks
    }

    override fun totalTicks(): Long {
        return totalTicks
    }

    override fun resetTotalTicks() {
        totalTicks = 0
    }

    override fun randomInitialState(): AbstractGameState {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
