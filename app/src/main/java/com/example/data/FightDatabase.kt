package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ReplayEntity::class, FighterConfigEntity::class], version = 1, exportSchema = false)
abstract class FightDatabase : RoomDatabase() {
    abstract fun fightDao(): FightDao

    companion object {
        @Volatile
        private var INSTANCE: FightDatabase? = null

        fun getDatabase(context: Context): FightDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FightDatabase::class.java,
                    "fight_engine_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
