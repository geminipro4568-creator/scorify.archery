package com.example.data

import kotlinx.coroutines.flow.Flow

class ArcheryRepository(private val dao: ArcheryDao) {
    // Archers
    fun getAllArchers(): Flow<List<Archer>> = dao.getAllArchers()
    suspend fun getArcherById(id: Int): Archer? = dao.getArcherById(id)
    suspend fun insertArcher(archer: Archer): Long = dao.insertArcher(archer)
    suspend fun updateArcher(archer: Archer) = dao.updateArcher(archer)
    suspend fun deleteArcher(archer: Archer) = dao.deleteArcher(archer)

    // Sessions
    fun getAllSessions(): Flow<List<ArcherySession>> = dao.getAllSessions()
    fun getSessionByIdFlow(id: Int): Flow<ArcherySession?> = dao.getSessionByIdFlow(id)
    suspend fun getSessionById(id: Int): ArcherySession? = dao.getSessionById(id)
    suspend fun insertSession(session: ArcherySession): Long = dao.insertSession(session)
    suspend fun updateSession(session: ArcherySession) = dao.updateSession(session)
    suspend fun deleteSession(session: ArcherySession) = dao.deleteSession(session)

    // Ends
    fun getEndsForSession(sessionId: Int): Flow<List<EndSession>> = dao.getEndsForSession(sessionId)
    suspend fun getEndByNumber(sessionId: Int, endNumber: Int): EndSession? = dao.getEndByNumber(sessionId, endNumber)
    suspend fun insertEnd(end: EndSession): Long = dao.insertEnd(end)
    suspend fun updateEnd(end: EndSession) = dao.updateEnd(end)

    // Shots
    fun getShotsForSession(sessionId: Int): Flow<List<ArrowShot>> = dao.getShotsForSession(sessionId)
    fun getShotsForEndFlow(sessionId: Int, endNumber: Int): Flow<List<ArrowShot>> = dao.getShotsForEndFlow(sessionId, endNumber)
    suspend fun getShotsForEnd(sessionId: Int, endNumber: Int): List<ArrowShot> = dao.getShotsForEnd(sessionId, endNumber)
    suspend fun insertShot(shot: ArrowShot): Long = dao.insertShot(shot)
    suspend fun updateShot(shot: ArrowShot) = dao.updateShot(shot)
    suspend fun deleteShot(shot: ArrowShot) = dao.deleteShot(shot)
    suspend fun clearEndForArcher(sessionId: Int, endNumber: Int, archerId: Int) = dao.clearEndForArcher(sessionId, endNumber, archerId)
    suspend fun deleteShotById(id: Int) = dao.deleteShotById(id)
    suspend fun getLastShotForArcherInEnd(sessionId: Int, endNumber: Int, archerId: Int): ArrowShot? = dao.getLastShotForArcherInEnd(sessionId, endNumber, archerId)
}
