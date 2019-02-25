package games.citygame

import math.Vector2d

data class CityPop(var citizens: Int=10000, var insurgents: Int=1000, var soldiers: Int=1000)


var indexCount: Int = 0

data class CityModel(var name: String, val location:Vector2d = Vector2d(), val population: CityPop=CityPop())




{

    var index: Int = indexCount++

    fun deepCopy() : CityModel {
        // println("Copying: $population")
        return CityModel(name = this.name, population = population.copy())
    }

}



data class CityGraph(var cities: List<CityModel>) {

//    init {
//        cities
//    }



}

interface WorldModel {

}


class GameState {

}

fun main(args: Array<String>) {

    val m1 = CityModel(name="Colchester")

    println(m1)


    val m2 = m1.deepCopy()
    m2.name = "Chelmsford"

    m2.population.citizens = 0

    println()

    println(m1)
    println(m2)

}


