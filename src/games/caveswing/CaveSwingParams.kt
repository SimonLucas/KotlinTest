package games.caveswing

import math.Vector2d
import java.io.Serializable
import java.util.*


data class CaveSwingParams (
        // duration
        var maxTicks: Int = 1000,
        // forces
        var gravity: Vector2d = Vector2d(0.0, 0.4),
        var hooke: Double = 0.02,
        var lossFactor: Double = 0.9999,

        // game map params
        var width: Int = 1500,
        var height: Int = 350,
        var nAnchors: Int = 10,
        var gridScale : Int = 20,
        var meanAnchorHeight: Double = height * 0.4,

        // score related params
        var successBonus: Int = 1000,
        var failurePenalty: Int = 1000,
        var pointPerX: Int = 10,
        var pointPerY: Int = -10,
        var costPerTick: Int = 10,

        var longJump:Boolean = true,

        var random: Random = Random()
) : Serializable {
    fun deepCopy() : CaveSwingParams {
        return copy(gravity = gravity.copy())
    }
}

