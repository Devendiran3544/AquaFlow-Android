package com.aquaflow.waterreminder.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [WaterIntakeEntity::class, UserSettingsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AquaFlowDatabase : RoomDatabase() {
    abstract fun waterIntakeDao(): WaterIntakeDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AquaFlowDatabase? = null

        fun getDatabase(context: Context): AquaFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AquaFlowDatabase::class.java,
                    "aquaflow_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
