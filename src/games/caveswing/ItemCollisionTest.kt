package games.caveswing

fun main() {
    val map = hashMapOf<ItemPosition,Item>(
            ItemPosition(10, 5) to Fruit()
    )

    val state = CaveGameInternalState()


    val test = fun (x:Int, y:Int) {
        val item = map.get(ItemPosition(x, y))
        if (item!= null) {
            println(item.alive)
            item.applyEffect(state)
            println(state.bonusScore)
        } else {
            println("No item")
        }

    }

    test.invoke(10, 4)
    test.invoke(10,5)
    test.invoke(10,5)

}

