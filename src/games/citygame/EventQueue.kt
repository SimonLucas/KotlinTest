package games.citygame

import utilities.ElapsedTimer
import utilities.StatSummary
import java.util.*
import kotlin.random.Random


fun main() {

    val nEvents = 1e2.toInt()

    val range = 1e8.toInt()
    val random = Random
    var events = PriorityQueue<Event>()
    val stats = StatSummary()

    val t = ElapsedTimer()

    for (i in 0 until nEvents.toInt()) {
        events.add(Event(random.nextInt(range)))
    }

    println(t)

    var n = 0

    val removeFromOld = 0

    while(!events.isEmpty()) {
        stats.add(events.remove().tick)
        val other = PriorityQueue<Event>(events)
        (0 until removeFromOld).forEach{if (!events.isEmpty()) events.remove()}
        // events = other
        n++
        if (!other.isEmpty()) println(other.remove())
    }
    println(t)
    println(stats.mean().toInt())
    println("n = " + n)
}

interface EventQueue {

}

data class Event (val tick: Int) : Comparable<Event> {
    override fun compareTo(other: Event): Int {
        return tick.compareTo(other.tick)
    }
}

