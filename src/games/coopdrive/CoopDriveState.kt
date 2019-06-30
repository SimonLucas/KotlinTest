package games.coopdrive

import ggi.AbstractGameState
import ggi.AbstractGameStateMulti
import ggi.ExtendedAbstractGameState
import ggi.ExtendedAbstractGameStateMulti
import math.Vec2d

var totalTicks: Long = 0


class InternalGameState (var params:CoopDriveParams = CoopDriveParams()) {

    val w = 600
    val h = 300

    var vehicles = ArrayList<Vehicle>()

    // for now give each vehicle the same goal
    fun getGoal(id: Int) : Vec2d {
        if (id == 0) {
            return Vec2d(w * 0.6, h * 0.5)
        } else {
            return Vec2d(w * 0.4, h * 0.5)
        }
    }

    fun parkingBonus(v: Vehicle) : Int {
        return (v.d.sp(Vec2d(-1.0,0.0)) * params.parkingBonus).toInt()
    }

    fun range() = w * params.goalTolerance

    fun setupVehciles() : InternalGameState {
        vehicles = ArrayList<Vehicle>()
        vehicles.add(Vehicle(s= Vec2d(w*0.2, h*0.2), v=Vec2d(w/1000.0, 0.0), id=0))
        vehicles.add(Vehicle(s= Vec2d(w*0.8, h*0.2), v=Vec2d(w/1000.0, 0.0), d=Vec2d(-1.0, 0.0), id=1))
        return this
    }

    fun deepCopy(): InternalGameState {
        val cp = InternalGameState()
        cp.vehicles = ArrayList<Vehicle>()
        vehicles.forEach { e -> cp.vehicles.add(e.copy()) }
        cp.params = params.copy()
        return cp
    }
}

data class CoopDriveParams(val maxTicks: Int = 1000,
                           val bonus: Int = 10000,
                           val goalTolerance: Double = 0.05,
                           val timePenalty: Int = 10,
                           val parkingBonus: Int = 10000,
                           val crashPenalty:Int = -10000
                           )

data class CoopDriveState (var params: CoopDriveParams = CoopDriveParams(),
                           var state: InternalGameState = InternalGameState(params)): ExtendedAbstractGameStateMulti {

    init{
        randomInitialState()
    }

    var nTicks = 0

    fun messageString() : String {
        // at the moment this is just whether the game is over for the first player
        return "$nTicks, ${isTerminal(0)}, ${score(0)}, ${score(1)}"
    }

    override fun copy(): AbstractGameStateMulti {

        val cp = CoopDriveState()
        cp.state = state.deepCopy()
        return cp;

        // return CoopDriveState(state = state.deepCopy())
    }

    override fun next(actions: IntArray): AbstractGameStateMulti {
        if (state.vehicles[0].s.distanceTo(state.vehicles[1].s) < state.vehicles[0].scale + state.vehicles[1].scale) {
            state.vehicles[0].hasCrashed = true
            state.vehicles[1].hasCrashed = true
        }

        if (isTerminal(0)) return this

        for (i in 0 until Math.min(actions.size, state.vehicles.size)) {
            state.vehicles[i].next(actions[i])
            // println("Executing ${actions[i]}")
        }
        nTicks++
        totalTicks++
        return this
    }

    override fun nActions(playerId: Int): Int {
        return 5;
    }

    override fun score(playerId: Int): Double {
        // purely cooperative game, so no need for separate scores
        val range = state.range()
        var goalScore = 0
        for (v in state.vehicles) {
            val distance = v.s.distanceTo(state.getGoal(v.id))
            if (distance < range) goalScore += state.params.bonus + state.parkingBonus(v)
            else goalScore -= distance.toInt()
        }

        var rawScore = (goalScore - (state.params.timePenalty * nTicks).toDouble())
        if (state.vehicles[playerId].hasCrashed) rawScore += state.params.crashPenalty
        return rawScore
    }

    override fun isTerminal(playerId: Int): Boolean {
        // match each vehicle with it's goal
        if (nTicks >= state.params.maxTicks) return true
        if (state.vehicles[0].hasCrashed || state.vehicles[1].hasCrashed) return true

        for (v in state.vehicles) {
            // if any vehicle is not at its goal then return false
            if (!atGoal(v)) return false
        }
        return true
    }

    fun atGoal(v: Vehicle) = v.s.distanceTo(state.getGoal(v.id)) < state.range()

    override fun nTicks(): Int {
        return nTicks
    }

    override fun totalTicks(): Long {
        return totalTicks
    }

    override fun resetTotalTicks() {
        totalTicks = 0
    }

    override fun randomInitialState(): ExtendedAbstractGameStateMulti {
        state.setupVehciles()
        return this
    }

}