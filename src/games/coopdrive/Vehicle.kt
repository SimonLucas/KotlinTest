package games.coopdrive

import math.Vec2d

data class Vehicle(var s: Vec2d = Vec2d(),
                   var v: Vec2d = Vec2d(),
                   var d: Vec2d = Vec2d(1.0, 0.0),
                   var speed:Double  = 0.0,
                   val id:Int=0,
                   var carLike:Boolean = false,
                   var hasCrashed:Boolean = false,
                   val scale: Double = 20.0
                   ) {

    // time increment for each game tick
    val dt = 1.0 / 10
    val thrust = 10.0
    val brake = thrust
    val nActions = 5


    // turn through this angle per second
    // when turning on the spot
    val steerRate = 0.1 * Math.PI

    // turn by this amount of the steer rate per pixel moved when car like
    val turnRate = 0.1
    //
    fun getThrust(action: Int) : Double {
        if (action == 1) return thrust
        if (action == 2) return -brake
        return 0.0
    }

    fun getTurn(action: Int) : Double {
        if (action == 3) return steerRate
        if (action == 4) return -steerRate
        return 0.0
    }

    fun next(action: Int) : Vehicle {
        if (carLike) {
            d = d.rotatedBy(getTurn(action) * dt * speed * turnRate)
        } else {
            d = d.rotatedBy(getTurn(action) * dt)
        }
        if (carLike) {
            speed += dt * getThrust(action)
            s += d * dt * speed
        } else {
            v += d * dt * getThrust(action)
            s += v * dt
        }
        return this
    }
}
