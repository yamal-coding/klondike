package com.yamal.klondike.presenter

import com.yamal.klondike.model.*
import com.yamal.klondike.view.GameView

class Presenter {

    private lateinit var view: GameView
    private lateinit var gameState: KlondikeState
    private var currentCardMovement: CardMovement? = null

    fun onCreate(view: GameView) {
        this.view = view
        gameState = KlondikeState()
        view.initGameBoard(gameState.columns)
    }

    fun requestNewCard() {
        if (gameState.hasRemainingCards()) {
            view.onNewCardRequested(
                card = gameState.popRemainingCard(),
                isLastCard = !gameState.hasRemainingCards()
            )
        } else {
            gameState.refillRemainingCards()
            view.onRemainingDeckOfCardsRefilled()
        }
    }

    fun setCardOnFlyFromColumn(column: Int, posInColumn: Int) {
        currentCardMovement = FromColumn(
            card = gameState.getCardFromColumn(column, posInColumn),
            sourceColumn = column,
            posInSourceColumn = posInColumn
        )
    }

    fun setCardOnFlyFromFlipped() {
        currentCardMovement = FromFlipped(
            card = gameState.getLastFlippedCard()
        )
    }

    fun setCardOnFlyFromGatheredDeck(cardType: CardType) {
        currentCardMovement = FromGathered(
            card = gameState.getLastGatheredCard(cardType)!!
        )
    }

    fun onCardGathered(type: CardType) {
        currentCardMovement?.let {
            handleOnCardGathered(it, type)
        }
    }

    private fun handleOnCardGathered(cardMovement: CardMovement, type: CardType) {
        val card = cardMovement.card
        val lastGatheredCard = gameState.getLastGatheredCard(type)

        if (card.type == type &&
            ((lastGatheredCard != null && card.number == lastGatheredCard.number + 1) ||
                    (lastGatheredCard == null && card.number == 0))
        ) {
            gameState.gatherCard(card)

            when (cardMovement) {
                is FromColumn -> {
                    val column = cardMovement.sourceColumn
                    val uncoveredCard = gameState.removeLastCardInColumnAndGetPrevious(column)
                    val flip = uncoveredCard?.isFlipped == false
                    uncoveredCard?.flip()
                    view.onCardsRemovedFromColumn(
                        column = column,
                        numOfRemovedCards = 1,
                        flip = flip
                    )
                }
                is FromFlipped -> {
                    view.onFlippedCardMoved(gameState.removeLastFlippedCardAndGetPrevious())
                }
            }

            view.onCardGathered(card)
            if (gameState.isFinished()) {
                view.onGameFinished()
            }
        }
    }

    fun onCardMoved(column: Int) {
        currentCardMovement?.let { cardMovement ->
            if (cardMovement is FromColumn) {
                val movedCard = cardMovement.card
                val sourceColumn = cardMovement.sourceColumn
                if (canMoveCard(gameState.getLastCardFromColumn(column), movedCard)) {
                    val cardsToMove = gameState.popCardsInColumn(
                        column = sourceColumn,
                        untilPos = cardMovement.posInSourceColumn
                    ).toTypedArray()

                    gameState.addCardsToColumn(column, *cardsToMove)

                    val newLastCardInColumn = gameState.getLastCardFromColumn(sourceColumn)
                    val flip = newLastCardInColumn?.isFlipped == false
                    newLastCardInColumn?.flip()

                    view.onCardsAddedToColumn(column, *cardsToMove)
                    view.onCardsRemovedFromColumn(
                        column = sourceColumn,
                        numOfRemovedCards = cardsToMove.size,
                        flip = flip
                    )
                }
            } else {
                addCardToColumnIfAllowed(column, cardMovement.card) { movedCard ->
                    when (cardMovement) {
                        is FromFlipped -> onFlippedCardMoved()
                        is FromGathered -> onGatheredCardMoved(movedCard.type)
                    }
                }
            }
        }
    }

    private fun onFlippedCardMoved() {
        view.onFlippedCardMoved(gameState.removeLastFlippedCardAndGetPrevious())
    }

    private fun onGatheredCardMoved(cardType: CardType) {
        view.onGatheredCardMoved(
            previousCard = gameState.removeLastGatheredCardAndGetPrevious(cardType),
            cardType = cardType
        )
    }

    private inline fun addCardToColumnIfAllowed(
        column: Int,
        movedCard: Card,
        alsoBlock: (Card) -> Unit
    ) {
        if (canMoveCard(gameState.getLastCardFromColumn(column), movedCard)) {
            gameState.addCardsToColumn(column, movedCard)
            view.onCardsAddedToColumn(column, movedCard)
            alsoBlock(movedCard)
        }
    }

    private fun canMoveCard(fixedCard: Card?, movedCard: Card): Boolean =
        (fixedCard == null && movedCard.number == 12) ||
                (fixedCard != null && fixedCard.type.color != movedCard.type.color
                        && fixedCard.number == movedCard.number + 1)
}