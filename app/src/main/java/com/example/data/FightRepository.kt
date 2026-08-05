package com.example.data

import kotlinx.coroutines.flow.Flow

class FightRepository(private val fightDao: FightDao) {
    val allReplays: Flow<List<ReplayEntity>> = fightDao.getAllReplays()
    val allConfigs: Flow<List<FighterConfigEntity>> = fightDao.getAllFighterConfigs()

    suspend fun saveReplay(replay: ReplayEntity): Long = fightDao.insertReplay(replay)
    suspend fun deleteReplay(id: Long) = fightDao.deleteReplay(id)
    suspend fun saveConfig(config: FighterConfigEntity) = fightDao.insertFighterConfig(config)
}
