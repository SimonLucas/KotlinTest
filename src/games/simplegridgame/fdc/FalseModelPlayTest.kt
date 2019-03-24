package games.simplegridgame.fdc

import agents.DoNothingAgent
import agents.SimpleEvoAgent
import games.gridgame.UpdateRule
import games.simplegridgame.SimpleGridGame
import games.simplegridgame.h
import games.simplegridgame.w
import utilities.StatSummary


fun main() {

    val tester = FalseModelPlayTest()

    // start with a true model
    // then do

}

class FalseModelPlayTest {

    val agent = SimpleEvoAgent(nEvals = 50, sequenceLength = 5, probMutation = 0.3)

    fun playTests(r1: UpdateRule, r2: UpdateRule, playSteps: Int = 100, nGames: Int = 5) : StatSummary {
        val ss = StatSummary("Play Tests")
        for(i in 0 until nGames) {
            println("Game: " + i)
            ss.add(playTest(r1, r2, playSteps))
        }
        return ss
    }


    fun playTest(r1: UpdateRule, r2: UpdateRule, playSteps: Int = 100) : Double {
        // test and checking the differences a number of times
        val game = SimpleGridGame(w, h)
        game.updateRule = r1

        val agent2 = DoNothingAgent()

        for (i in 0 until playSteps) {


            val falseCopy = game.copy() as SimpleGridGame
            falseCopy.updateRule = r2

            // PLAN the action in the game with the false model
            val action = agent.getAction(falseCopy.copy(), 0)
            val dummy = agent2.getAction(falseCopy.copy(), 1)
            val actions = intArrayOf(action, dummy)

            // now TAKE the action in the true game

            game.next(actions)
        }
        // println("Test ${i}, \t diff = ${diff}")
        println("\t score = ${game.score()}")
        return game.score()
    }

}