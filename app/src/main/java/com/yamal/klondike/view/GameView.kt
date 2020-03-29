package com.yamal.klondike.view

import com.yamal.klondike.model.Card
import com.yamal.klondike.model.CardType

interface GameView {
    fun initGameBoard(initialColumns: List<List<Card>>)
    fun onRemainingDeckOfCardsRefilled()
    fun onNewCardRequested(card: Card, isLastCard: Boolean)
    fun onFlippedCardMoved(previousFlippedCard: Card?)
    fun onCardGathered(card: Card)
    fun onGameFinished()
    fun onGatheredCardMoved(previousCard: Card?, cardType: CardType)
    fun onCardsRemovedFromColumn(column: Int, numOfRemovedCards: Int, flip: Boolean)
    fun onCardsAddedToColumn(column: Int, vararg movedCards: Card)
}