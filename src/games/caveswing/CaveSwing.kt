package games.caveswing

import agents.RandomAgent
import agents.SimpleEvoAgent
import ggi.AbstractGameState
import ggi.SimplePlayerInterface
import math.Vector2d
import utilities.ElapsedTimer
import utilities.Picker
import java.awt.geom.Rectangle2D
import java.io.Serializable
import java.util.*

// started the port at 14:58

// define the set of parameters


// this is Kotlin for a Singleton object
object Constants {
    var actionRelease: Int = 0
    val actionAttach: Int = 1
}


data class Anchor(val s: Vector2d) : Serializable {
    fun getForce(position: Vector2d, hooke: Double): Vector2d {
        // note that this is currently modelled as an elastic
        // with zero natural length; would be easy to update this
        // to model as a spring
        val tension = Vector2d(s)
        tension.subtract(position)
        tension.mul(hooke)
        return tension
    }
}

class Map : Serializable {
    var anchors = ArrayList<Anchor>()

    // the map specifies the dimensions and the set of anchors
    internal var bounds = Rectangle2D.Double()

    fun getAnchor(index: Int): Anchor? {
        return if (index < anchors.size) {
            anchors[index]
        } else {
            null
        }
    }

    fun getClosestAnchor(s: Vector2d): Anchor? {
        val picker = Picker<Anchor>(Picker.MIN_FIRST)
        for (a in anchors) {
            picker.add(a.s.dist(s), a)
        }
        return picker.best
    }

    fun setBounds(width: Int, height: Int): Map {
        bounds = Rectangle2D.Double(0.0, 0.0, width.toDouble(), height.toDouble())
        return this
    }

    fun setAnchors(nAnchors: Int, meanHeight: Double): Map {
        anchors = ArrayList()
        val gap = bounds.width / (nAnchors + 1)
        var x = gap / 2
        for (i in 0 until nAnchors) {
            val s = Vector2d(x, meanHeight)
            anchors.add(Anchor(s))
            x += gap
        }
        return this
    }

    fun setup(params: CaveSwingParams): Map {
        setBounds(params.width, params.height)
        setAnchors(params.nAnchors, params.meanAnchorHeight)
        return this
    }
}


class CaveGameState : AbstractGameState, Serializable {

    var params = CaveSwingParams()
    var map = Map().setup(params)

    // internal var actuator: Actuator? = null

    var nextAnchorIndex: Int = 0
    var nTicks: Int = 0

    var avatar = MovableObject(Vector2d(), Vector2d())

    internal var gameOver: Boolean = false
    // boolean isAttached;
    var currentAnchor: Anchor? = null

    // reward increasing x-position
    // reward a high Y- position
    // punish use of game ticks (i.e. want to get there as quickly as possible
    // it's important that nTicks is not incremented after the game is over
    // add in success bonus
    override fun score(): Double {
        var score = avatar.s.x * params.pointPerX + avatar.s.y * params.pointPerY - nTicks * params.costPerTick
        if (avatar.s.x >= params.width) {
            score += params.successBonus
        }
        if (avatar.s.y < 0 || avatar.s.y >= params.height) {
            score -= params.failurePenalty
        }
        return Math.floor(score)
    }

    // if already over, then return quickly
    // now test for game over
    override fun isTerminal(): Boolean {
        if (gameOver) return gameOver
        if (nTicks >= params.maxTicks || !map.bounds.contains(avatar.s.x, avatar.s.y)) {
            gameOver = true
        }
        return gameOver
    }

    fun setup(params: CaveSwingParams = this.params): CaveGameState {
        map = Map().setup(params)
        var s = Vector2d(params.width.toDouble() / 10, params.height.toDouble() / 2)
        avatar = MovableObject(s, Vector2d())
        return this
    }

    override fun copy(): AbstractGameState {
        val cgs = CaveGameState()
        // shallow copy the map and the current Anchor
        cgs.map = map
        cgs.currentAnchor = currentAnchor
        cgs.nextAnchorIndex = nextAnchorIndex
        cgs.nTicks = nTicks
        // deep copy the avatar and the params
        cgs.avatar = avatar.copy()
        cgs.params = params.deepCopy()
        cgs.gameOver = gameOver
        return cgs
    }

    override fun next(actions: IntArray, playerId: Int): AbstractGameState {
        // the array of actions is to allow for a multi-player game
        // quick return if game over
        if (isTerminal()) return this

        // otherwise let's calculate the updates
        val action = actions[0]
        val resultantForce = params.gravity.copy()

        // now will it be to attach or to release?
        if (action == Constants.actionAttach) {
            if (currentAnchor == null) {
                // if already attached, do nothing
                // if not yet attached, attach to the next one if available
                // currentAnchor = map.getAnchor(nextAnchorIndex);
                currentAnchor = map.getClosestAnchor(avatar.s)
                if (currentAnchor != null) {
                    // nextAnchorIndex++;
                }
            }
            // now if there is an anchor, apply the necessary force
            if (currentAnchor != null) {
                val tension = currentAnchor!!.getForce(avatar.s, params.hooke)
                resultantForce.add(tension)
            }
        } else if (action == Constants.actionRelease) {
            currentAnchor = null
        }
        avatar.update(resultantForce, params.lossFactor)
        nTicks++
        return this
    }

    override fun nActions(): Int {
        return 2
    }
}




data class MovableObject(var s: Vector2d, var v: Vector2d) : Serializable {

    fun update(resultantForce: Vector2d, lossFactor: Double): MovableObject {
        v.add(resultantForce)
        s.add(v)
        v.mul(lossFactor)
        return this
    }

    override fun toString(): String {
        return "$s : $v"
    }

    fun copy() : MovableObject {
        return MovableObject(s.copy(), v.copy())
    }
}
