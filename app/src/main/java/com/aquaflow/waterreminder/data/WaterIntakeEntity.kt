package com.aquaflow.waterreminder.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_intakes")
data class WaterIntakeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amountMl: Int,
    val timestamp: Long,
    val dateString: String
)
