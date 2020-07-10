package games.cards

// just playing around with some Kotlin

fun main() {
    val deck = CardDeck().shuffle()
    deck.cards.forEach{ t -> println(t.unicode()) }
    deck.cards.sortBy { it.rank }
    deck.cards.forEach{ println(it) }
    val sorted = deck.cards.sortedWith(compareBy(Card::rank, Card::suit))
    sorted.forEach { println(it) }
}

enum class Suit {Spades, Hearts, Diamonds, Clubs}
enum class Rank {Ace, Two, Three, Four, Five, Six, Seven, Eight, Nine, Ten, Jack, Queen, King}

data class Card(val suit:Suit, val rank:Rank)  {
    companion object {
        // for unicode function below - took some Googling to find these
        // playing card codes
        val offsets = arrayOf(0x1F0A1, 0x1F0B1, 0x1F0C1, 0x1F0D1)
    }
    fun unicode() : CharArray {
        // enables printing of a card image (glyph) for each card
        // this increment trick is needed because the Unicode playing card
        // set includes a "Knight" card between the Jack and the Queen
        // and we want to exclude that here
        val ix = if (rank.ordinal < Rank.Queen.ordinal) rank.ordinal else 1 + rank.ordinal
        return Character.toChars( offsets[suit.ordinal] + ix )
    }
}

class CardDeck () {
    val cards = ArrayList<Card>()
    init {
        Suit.values().forEach { t ->
            Rank.values().forEach { u -> cards.add(Card(t, u)) }
        }
    }
    fun shuffle() : CardDeck {
        cards.shuffle()
        return this
    }
}
