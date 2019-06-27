package games.coopdrive

import ggi.AbstractGameState
import ggi.ExtendedAbstractGameState
import math.Vec2d

var totalTicks: Long = 0


class InternalGameState (var params:CoopDriveParams = CoopDriveParams()) {

    val w = 600
    val h = 300

    var vehicles = ArrayList<Vehicle>()

    // for now give each vehicle the same goal
    fun getGoal(id: Int) : Vec2d {
        return Vec2d(w * 0.8, h * 0.5)
    }

    fun setupVehciles() : InternalGameState {
        vehicles = ArrayList<Vehicle>()
        vehicles.add(Vehicle(s= Vec2d(w*0.2, h*0.2)))
        return this
    }

    fun deepCopy(): InternalGameState {
        val cp = InternalGameState()
        cp.vehicles = vehicles.clone() as ArrayList<Vehicle>
        cp.params = params.copy()
        return cp
    }

}

data class Vehicle(var s:Vec2d = Vec2d(), var v:Vec2d=Vec2d(), val id:Int=0) {
    fun next(action: Int) : Vehicle {
        s = s + v
        return this
    }
}

data class CoopDriveParams(val maxTicks:Int = 1000, val bonus:Int = 100, val goalTolerance: Double = 0.05)

data class CoopDriveState (var state: InternalGameState = InternalGameState()): ExtendedAbstractGameState {

    var nTicks = 0

    override fun copy(): AbstractGameState {

        return CoopDriveState(state = state.deepCopy())
    }

    override fun next(actions: IntArray): AbstractGameState {

        // update each vehicle

        nTicks++
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun nActions(): Int {
        // return
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun score(): Double {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isTerminal(): Boolean {
        // match each vehicle with it's goal

        for (v in state.vehicles) {
            // if any vehicle is not at its goal then return false
            if (!atGoal(v)) return false
        }
        return true
    }

    fun atGoal(v: Vehicle) : Boolean {
        val range = state.params.goalTolerance * state.w
        return v.s.distanceTo(state.getGoal(v.id)) < range
    }

    override fun nTicks(): Int {
        return nTicks
    }

    override fun totalTicks(): Long {
        return games.breakout.totalTicks
    }

    override fun resetTotalTicks() {
        totalTicks = 0
    }

    override fun randomInitialState(): AbstractGameState {
        state.setupVehciles()
        return this
    }

}