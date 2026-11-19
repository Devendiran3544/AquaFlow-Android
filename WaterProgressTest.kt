package com.aquaflow.waterreminder

import org.junit.Assert.assertEquals
import org.junit.Test

class WaterProgressTest {
    @Test
    fun progressIsZeroAtStart() {
        assertEquals(0, (0f / 2000f * 100).toInt())
    }

    @Test
    fun progressIsHalfAtHalfGoal() {
        assertEquals(50, (1000f / 2000f * 100).toInt())
    }
}
