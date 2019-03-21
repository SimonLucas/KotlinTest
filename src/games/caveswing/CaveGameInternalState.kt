package games.caveswing

import ggi.game.MovableObject
import math.Vector2d

data class CaveGameInternalState (
        var params: CaveSwingParams = CaveSwingParams(),
        var map: Map = Map().setup(params),
        // var nextAnchorIndex: Int = 0,
        var nTicks: Int = 0,
        var avatar: MovableObject = MovableObject(),
        var gameOver: Boolean = false,
        var currentAnchor: Anchor? = null,
        var bonusScore: Int = 0
) {
    fun deepCopy() : CaveGameInternalState {
        val cgs = CaveGameInternalState()
        // shallow copy the map and the current Anchor
        // deep copy the avatar and the params
        cgs.avatar = avatar.copy()
        cgs.params = params.deepCopy()
        // shallow copy the other ones
        cgs.map = map
        cgs.currentAnchor = currentAnchor
        cgs.nTicks = nTicks
        cgs.gameOver = gameOver
        cgs.bonusScore = bonusScore
        return cgs
    }

}
