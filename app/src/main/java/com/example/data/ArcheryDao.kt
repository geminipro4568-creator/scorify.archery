package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ArcheryDao {
    // Archer queries
    @Query("SELECT * FROM archers ORDER BY name ASC")
    fun getAllArchers(): Flow<List<Archer>>

    @Query("SELECT * FROM archers WHERE id = :id")
    suspend fun getArcherById(id: Int): Archer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArcher(archer: Archer): Long

    @Update
    suspend fun updateArcher(archer: Archer)

    @Delete
    suspend fun deleteArcher(archer: Archer)

    // ArcherySession queries
    @Query("SELECT * FROM archery_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<ArcherySession>>

    @Query("SELECT * FROM archery_sessions WHERE id = :id")
    fun getSessionByIdFlow(id: Int): Flow<ArcherySession?>

    @Query("SELECT * FROM archery_sessions WHERE id = :id")
    suspend fun getSessionById(id: Int): ArcherySession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ArcherySession): Long

    @Update
    suspend fun updateSession(session: ArcherySession)

    @Delete
    suspend fun deleteSession(session: ArcherySession)

    // EndSession queries
    @Query("SELECT * FROM end_sessions WHERE sessionId = :sessionId ORDER BY endNumber ASC")
    fun getEndsForSession(sessionId: Int): Flow<List<EndSession>>

    @Query("SELECT * FROM end_sessions WHERE sessionId = :sessionId AND endNumber = :endNumber LIMIT 1")
    suspend fun getEndByNumber(sessionId: Int, endNumber: Int): EndSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnd(end: EndSession): Long

    @Update
    suspend fun updateEnd(end: EndSession)

    // ArrowShot queries
    @Query("SELECT * FROM arrow_shots WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getShotsForSession(sessionId: Int): Flow<List<ArrowShot>>

    @Query("SELECT * FROM arrow_shots WHERE sessionId = :sessionId AND endNumber = :endNumber ORDER BY shotNumber ASC, timestamp ASC")
    fun getShotsForEndFlow(sessionId: Int, endNumber: Int): Flow<List<ArrowShot>>

    @Query("SELECT * FROM arrow_shots WHERE sessionId = :sessionId AND endNumber = :endNumber ORDER BY shotNumber ASC, timestamp ASC")
    suspend fun getShotsForEnd(sessionId: Int, endNumber: Int): List<ArrowShot>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShot(shot: ArrowShot): Long

    @Update
    suspend fun updateShot(shot: ArrowShot)

    @Delete
    suspend fun deleteShot(shot: ArrowShot)

    @Query("DELETE FROM arrow_shots WHERE sessionId = :sessionId AND endNumber = :endNumber AND archerId = :archerId")
    suspend fun clearEndForArcher(sessionId: Int, endNumber: Int, archerId: Int)

    @Query("DELETE FROM arrow_shots WHERE id = :id")
    suspend fun deleteShotById(id: Int)

    @Query("SELECT * FROM arrow_shots WHERE sessionId = :sessionId AND endNumber = :endNumber AND archerId = :archerId ORDER BY shotNumber DESC LIMIT 1")
    suspend fun getLastShotForArcherInEnd(sessionId: Int, endNumber: Int, archerId: Int): ArrowShot?
}
