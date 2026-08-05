package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FightDao {
    @Query("SELECT * FROM match_replays ORDER BY timestamp DESC")
    fun getAllReplays(): Flow<List<ReplayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReplay(replay: ReplayEntity): Long

    @Query("DELETE FROM match_replays WHERE id = :id")
    suspend fun deleteReplay(id: Long)

    @Query("SELECT * FROM fighter_configs")
    fun getAllFighterConfigs(): Flow<List<FighterConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFighterConfig(config: FighterConfigEntity)
}
