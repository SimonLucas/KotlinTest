package games.sokogame

import ggi.AbstractGameState
import ggi.ExtendedAbstractGameState
import math.Vector2d

class SokobanState {
    val avatar: Vector2d = Vector2d()


    


}

class SokobanGame : ExtendedAbstractGameState {



    override fun copy(): AbstractGameState {

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

    }

    override fun next(actions: IntArray): AbstractGameState {

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun totalTicks(): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun resetTotalTicks() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun randomInitialState(): AbstractGameState {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.



    }



    fun moveAvatar(v: Vector2d) {

    }

}