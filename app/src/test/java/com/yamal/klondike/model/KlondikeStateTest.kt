package com.yamal.klondike.model

import org.junit.Test

import org.junit.Assert.*

class KlondikeStateTest {

    @Test
    fun `game has finished when all cards are gathered`() {
        val state = KlondikeState()

        CardType.values().forEach { cardType ->
            for (i in 0..12) {
                state.gatherCard(Card(i, cardType))
            }
        }

        assertTrue(state.isFinished())
    }

    @Test
    fun `game has not finished when all cards are not  gathered`() {
        val state = KlondikeState()

        assertFalse(state.isFinished())
    }
}
