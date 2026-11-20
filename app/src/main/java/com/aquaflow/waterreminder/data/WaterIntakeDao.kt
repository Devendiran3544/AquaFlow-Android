package com.aquaflow.waterreminder.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class DateTotalResult(
    val dateString: String,
    val total: Int
)

@Dao
interface WaterIntakeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntake(intake: WaterIntakeEntity): Long

    @Query("DELETE FROM water_intakes WHERE id = :id")
    suspend fun deleteIntakeById(id: Long)

    @Query("SELECT * FROM water_intakes WHERE dateString = :dateString ORDER BY timestamp DESC")
    fun getIntakesForDate(dateString: String): Flow<List<WaterIntakeEntity>>

    @Query("SELECT SUM(amountMl) FROM water_intakes WHERE dateString = :dateString")
    fun getTotalConsumedForDate(dateString: String): Flow<Int?>

    @Query("SELECT dateString, SUM(amountMl) as total FROM water_intakes GROUP BY dateString")
    fun getDailyTotals(): Flow<List<DateTotalResult>>

    @Query("SELECT * FROM water_intakes ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentIntakes(limit: Int = 20): Flow<List<WaterIntakeEntity>>

    @Query("SELECT SUM(amountMl) FROM water_intakes WHERE dateString = :dateString")
    suspend fun getTodayTotalSync(dateString: String): Int?

    @Query("DELETE FROM water_intakes WHERE dateString = :dateString")
    suspend fun deleteAllIntakesForDate(dateString: String)

    @Query("DELETE FROM water_intakes")
    suspend fun clearAllHistory()
}
