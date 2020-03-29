package com.yamal.klondike.model

import java.util.*

enum class CardColor {
    RED, BLACK
}

enum class CardType(private val label: String, val color: CardColor) {
    CLUBS("T", CardColor.BLACK),
    DIAMONDS("D", CardColor.RED),
    SPADES("S", CardColor.BLACK),
    HEARTS("C", CardColor.RED);

    override fun toString(): String = label
}

data class Card(
    val number: Int,
    val type: CardType
) {
    var isFlipped: Boolean = false
        private set

    fun flip(): Card = apply {
        if (!isFlipped) {
            isFlipped = true
        }
    }

    override fun toString(): String = "$number $type $isFlipped"
}

sealed class CardMovement(
    open val card: Card
)

data class FromColumn(
    override val card: Card,
    val sourceColumn: Int,
    val posInSourceColumn: Int
) : CardMovement(card)

data class FromGathered(
    override val card: Card
) : CardMovement(card)

data class FromFlipped(
    override val card: Card
) : CardMovement(card)

class KlondikeState {

    private val remainingCards = shuffledDeckOfCards()
    private val flippedCards: Deque<Card> = LinkedList<Card>()

    private val gatheredDecks = mapOf(
        CardType.CLUBS to  Stack<Card>(),
        CardType.DIAMONDS to  Stack(),
        CardType.SPADES to  Stack(),
        CardType.HEARTS to  Stack()
    )

    val columns: List<LinkedList<Card>> = listOf(
        LinkedList(), LinkedList(), LinkedList(),
        LinkedList(), LinkedList(), LinkedList(),
        LinkedList()
    )

    init {
        initGame()
    }

    private fun initGame() {
        for (i in 0..6) {
            for (j in 0..i) {
                columns[i].add(remainingCards.poll()!!)
            }
            columns[i].add(remainingCards.poll()!!.flip())
        }
    }

    fun hasRemainingCards(): Boolean = remainingCards.isNotEmpty()

    fun popRemainingCard(): Card {
        val card = remainingCards.pop().flip()
        flippedCards.add(card)
        return card
    }

    fun refillRemainingCards() {
        for (i in 1..flippedCards.size) {
            remainingCards.add(flippedCards.poll())
        }
    }

    fun getLastGatheredCard(type: CardType): Card? =
        if (gatheredDecks[type]?.isNotEmpty() == true) {
            gatheredDecks[type]?.peek()
        } else {
            null
        }

    fun gatherCard(card: Card) {
        gatheredDecks[card.type]?.push(card)
    }

    fun getCardFromColumn(column: Int, posInColumn: Int): Card =
        columns[column][posInColumn]

    fun getLastCardFromColumn(column: Int): Card? =
        columns[column].peekLast()

    fun removeLastFlippedCardAndGetPrevious(): Card? {
        flippedCards.pollLast()
        return flippedCards.peekLast()
    }

    fun removeLastGatheredCardAndGetPrevious(type: CardType): Card? {
        gatheredDecks[type]?.pop()
        return gatheredDecks[type]?.peek()
    }

    fun getLastFlippedCard(): Card = flippedCards.peekLast()!!

    fun removeLastCardInColumnAndGetPrevious(column: Int): Card? {
        columns[column].pollLast()
        return columns[column].peekLast()
    }

    fun addCardsToColumn(column: Int, vararg cards: Card) {
        for (i in cards.size - 1 downTo 0) {
            columns[column].add(cards[i])
        }
    }

    fun popCardsInColumn(column: Int, untilPos: Int): List<Card> {
        val cards = mutableListOf<Card>()

        for (i in 1..columns[column].size - untilPos) {
            cards.add(columns[column].pollLast()!!)
        }

        return cards
    }

    fun isFinished(): Boolean =
        gatheredDecks.all { it.value.size == 13 }

    companion object {
        fun shuffledDeckOfCards(): Deque<Card> { // TODO shuffle for real
            val cards = LinkedList<Card>()
            for (type in CardType.values()) {
                for (i in 0..12) {
                    cards.push(Card(i, type))
                }
            }
            return cards
        }
    }
}