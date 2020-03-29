package com.yamal.klondike.view

import android.content.ClipData
import android.content.ClipDescription
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.yamal.klondike.R
import com.yamal.klondike.model.Card
import com.yamal.klondike.model.CardType
import com.yamal.klondike.presenter.Presenter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), GameView {

    private val presenter = Presenter()
    private lateinit var gatheredDecks: Map<CardType, KlondikeCardView>
    private lateinit var columnsLayouts: List<FrameLayout>
    private val cardsByColumn: Map<Int, MutableList<KlondikeCardView>> =
        mapOf(
            0 to mutableListOf(), 1 to mutableListOf(), 2 to mutableListOf(),
            3 to mutableListOf(), 4 to mutableListOf(), 5 to mutableListOf(),
            6 to mutableListOf()
        )
    private var cardOverlapMargin: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initUI()

        presenter.onCreate(this)
    }

    private fun initUI() {
        cardOverlapMargin = resources.getDimension(R.dimen.card_overlap_margin).toInt()

        remaining_cards.setAsBack()
        remaining_cards.setOnClickListener {
            presenter.requestNewCard()
        }

        flipped_cards.setAsEmpty()
        flipped_cards.setupDragAndDrop {
            presenter.setCardOnFlyFromFlipped()
        }

        gatheredDecks = mapOf(
            CardType.CLUBS to last_gathered_club,
            CardType.DIAMONDS to last_gathered_diamond,
            CardType.HEARTS to last_gathered_heart,
            CardType.SPADES to last_gathered_spade
        )
        gatheredDecks.forEach { (type, cardView) ->
            cardView.setAsEmpty()
            cardView.setupDragAndDropAsGatherCard(type)
        }
    }

    private fun KlondikeCardView.setupDragAndDropAsGatherCard(cardType: CardType) {
        setupDragAndDrop {
            if (!isEmptyCard) {
                presenter.setCardOnFlyFromGatheredDeck(cardType)
            }
        }
        setOnDragListener { _, event ->
            if (event.action == DragEvent.ACTION_DROP) {
                presenter.onCardGathered(cardType)
            }
            true // TODO
        }
    }

    override fun initGameBoard(initialColumns: List<List<Card>>) {
        columnsLayouts = listOf(first_column, second_column, third_column,
            fourth_column, fifth_column, sixth_column, seventh_column)

        for (i in 0..6) {
            fillColumn(columnsLayouts[i], i, initialColumns[i])
        }
    }

    private fun fillColumn(layout: FrameLayout, numOfColumn: Int, column: List<Card>) {
        var offset = 0
        val cardByColumn = cardsByColumn[numOfColumn]

        column.asSequence().withIndex().forEach {
            val card = KlondikeCardView.newColumnCard(this, numOfColumn, it.index, it.value)

            if (it.index == column.size - 1){
                card.setupDragAndDropForColumnCard(isLastCard = true)
            }

            cardByColumn?.add(card)
            layout.addView(card, getCardLayoutParams(offset))

            offset += cardOverlapMargin
        }
    }

    private fun getCardLayoutParams(margin: Int): FrameLayout.LayoutParams {
        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, margin, 0, 0)

        return layoutParams
    }

    private fun KlondikeCardView.setupDragAndDropForColumnCard(isLastCard: Boolean) {
        setupDragAndDrop { cardOnFly ->
            presenter.setCardOnFlyFromColumn(cardOnFly.column, cardOnFly.posInColumn)
        }
        if (isLastCard) {
            setOnDragListener()
        }
    }

    private fun KlondikeCardView.setOnDragListener() {
        setOnDragListener { _, event ->
            if (event.action == DragEvent.ACTION_DROP) {
                presenter.onCardMoved(column)
            }
            true // TODO
        }
    }

    private fun KlondikeCardView.setupDragAndDrop(setCardOnFly: (KlondikeCardView) -> Unit) {
        setOnLongClickListener { view ->
            val cardOnFly = view as KlondikeCardView
            setCardOnFly(cardOnFly)

            cardOnFly.startDragAndDrop(
                ClipData(
                    cardOnFly.tag as? CharSequence,
                    arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                    ClipData.Item(cardOnFly.tag as? CharSequence)
                ),
                View.DragShadowBuilder(cardOnFly),
                null,
                0
            )

            true
        }
    }

    override fun onRemainingDeckOfCardsRefilled() {
        remaining_cards.setAsBack()
        flipped_cards.setAsEmpty()
    }

    override fun onNewCardRequested(card: Card, isLastCard: Boolean) {
        if (isLastCard) {
            remaining_cards.setAsEmpty()
        }
        flipped_cards.setCard(card)
    }

    override fun onFlippedCardMoved(previousFlippedCard: Card?) {
        previousFlippedCard?.let(flipped_cards::setCard) ?: flipped_cards.setAsEmpty()
    }

    override fun onCardsRemovedFromColumn(column: Int, numOfRemovedCards: Int, flip: Boolean) {
        cardsByColumn[column]?.takeIf { it.isNotEmpty() }?.let { cardsInColumn ->
            for (i in 1..numOfRemovedCards) {
                columnsLayouts[column].removeView(cardsInColumn[cardsInColumn.size - 1])
                cardsInColumn.removeAt(cardsInColumn.size - 1)
            }

            if (cardsInColumn.isEmpty()) {
                val emptyCard = KlondikeCardView.newEmptyColumnView(this, column)
                emptyCard.setOnDragListener()
                cardsInColumn.add(emptyCard)
                columnsLayouts[column].addView(emptyCard, getCardLayoutParams(0))
            } else  {
                cardsInColumn[cardsInColumn.size - 1].run {
                    if (flip) {
                        flip()
                        setupDragAndDropForColumnCard(isLastCard = true)
                    } else {
                        setOnDragListener()
                    }
                }
            }
        }
    }

    override fun onCardGathered(card: Card) {
        gatheredDecks[card.type]?.run {
            setCard(card)
        }
    }

    override fun onGatheredCardMoved(previousCard: Card?, cardType: CardType) {
        previousCard?.let {
            gatheredDecks[cardType]?.setCard(it)
        } ?: gatheredDecks[cardType]?.setAsEmpty()
    }

    override fun onCardsAddedToColumn(column: Int, vararg movedCards: Card) {
        cardsByColumn[column]?.let { cardsInColumn ->
            val lastCardInColumn = cardsInColumn.last()

            if (lastCardInColumn.isEmptyCard) {
                cardsInColumn.removeAt(cardsInColumn.size - 1)
                columnsLayouts[column].removeView(lastCardInColumn)
            } else {
                lastCardInColumn.setOnDragListener(null)
            }

            var offset = cardOverlapMargin * cardsInColumn.size
            for (i in movedCards.size - 1 downTo 0) {
                val newCardView =
                    KlondikeCardView.newColumnCard(this, column, cardsInColumn.size, movedCards[i])
                newCardView.setupDragAndDropForColumnCard(isLastCard = i == 0)

                columnsLayouts[column].addView(newCardView, getCardLayoutParams(offset))
                cardsInColumn.add(newCardView)
                offset += cardOverlapMargin
            }
        }
    }

    override fun onGameFinished() {
        AlertDialog.Builder(this)
            .setTitle(R.string.game_finished_dialog_title)
            .setMessage(R.string.game_finished_dialog_message)
            .create()
            .show()
    }
}