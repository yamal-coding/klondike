package com.yamal.klondike.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.yamal.klondike.R
import com.yamal.klondike.model.Card
import com.yamal.klondike.model.CardType

@SuppressLint("ViewConstructor")
class KlondikeCardView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null
) : FrameLayout(context, attributeSet) {

    private val cardFrontLayout: TextView
    private val cardBackLayout: View
    private val emptyLayout: View
    private var isFlipped: Boolean = true

    var isEmptyCard: Boolean = false
    var column: Int = -1
    var posInColumn: Int = -1

    init {
        val rootLayout = inflate(context, R.layout.klondike_card, this)
        cardFrontLayout = rootLayout.findViewById(R.id.card_front)
        cardBackLayout = rootLayout.findViewById(R.id.card_back)
        emptyLayout = rootLayout.findViewById(R.id.empty_card)
    }

    fun flip() {
        if (isFlipped) {
            cardFrontLayout.visibility = View.GONE
            cardBackLayout.visibility = View.VISIBLE
        } else {
            cardFrontLayout.visibility = View.VISIBLE
            cardBackLayout.visibility = View.GONE
        }
    }

    fun setAsEmpty() {
        isEmptyCard = true
        cardFrontLayout.visibility = View.GONE
        cardBackLayout.visibility = View.GONE
        emptyLayout.visibility = View.VISIBLE
    }

    fun setAsBack() {
        isEmptyCard = false
        cardFrontLayout.visibility = View.GONE
        emptyLayout.visibility = View.GONE
        cardBackLayout.visibility = View.VISIBLE
    }

    @SuppressLint("SetTextI18n")
    fun setCard(card: Card) {
        isEmptyCard = false
        cardFrontLayout.setTextColor(
            if (card.type == CardType.DIAMONDS || card.type == CardType.HEARTS) {
                Color.RED
            } else {
                Color.BLACK
            }
        )
        cardFrontLayout.text = "${card.number}${card.type}"
        isFlipped = card.isFlipped
        emptyLayout.visibility = View.GONE

        if (isFlipped) {
            cardFrontLayout.visibility = View.VISIBLE
            cardBackLayout.visibility = View.GONE
        } else {
            cardFrontLayout.visibility = View.GONE
            cardBackLayout.visibility = View.VISIBLE
        }
    }

    companion object {
        fun newEmptyColumnView(context: Context, column: Int): KlondikeCardView =
            KlondikeCardView(context).apply {
                this.column = column
                setAsEmpty()
            }

        fun newColumnCard(
            context: Context,
            column: Int,
            posInColumn: Int,
            card: Card
        ): KlondikeCardView =
            KlondikeCardView(context).apply {
                this.column = column
                this.posInColumn = posInColumn
                setCard(card)
            }
    }

}