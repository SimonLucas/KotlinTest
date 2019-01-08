package test

import com.google.gson.Gson
import games.caveswing.CaveGameState
import com.google.gson.GsonBuilder



fun main(args: Array<String>) {

    var gameState = CaveGameState()
    val gson = GsonBuilder().setPrettyPrinting().create();

    val json = gson.toJson(gameState)
    println(json)
}
