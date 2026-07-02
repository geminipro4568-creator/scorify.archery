package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ArcheryViewModel(
    application: Application,
    private val repository: ArcheryRepository
) : AndroidViewModel(application) {

    // --- ARCHER STATE ---
    val allArchers: StateFlow<List<Archer>> = repository.getAllArchers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- SESSIONS ---
    val allSessions: StateFlow<List<ArcherySession>> = repository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- ACTIVE SESSION RUNTIME STATE ---
    private val _activeSessionId = MutableStateFlow<Int?>(null)
    val activeSessionId: StateFlow<Int?> = _activeSessionId.asStateFlow()

    val activeSession: StateFlow<ArcherySession?> = _activeSessionId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.getSessionByIdFlow(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _currentEndNum = MutableStateFlow(1)
    val currentEndNum: StateFlow<Int> = _currentEndNum.asStateFlow()

    private val _activeArcherIdx = MutableStateFlow(0)
    val activeArcherIdx: StateFlow<Int> = _activeArcherIdx.asStateFlow()

    // Input Lock state (prevent accidental adjustments)
    private val _isInputLocked = MutableStateFlow(false)
    val isInputLocked: StateFlow<Boolean> = _isInputLocked.asStateFlow()

    // All shots in this session
    val activeSessionShots: StateFlow<List<ArrowShot>> = _activeSessionId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getShotsForSession(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All ends (for notes, etc.)
    val activeSessionEnds: StateFlow<List<EndSession>> = _activeSessionId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getEndsForSession(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- CONVENIENCE STATE DERIVATIONS ---
    // Archers actively competing in this session
    val activeSessionArchers: StateFlow<List<Archer>> = combine(activeSession, allArchers) { session, archers ->
        if (session == null) emptyList()
        else {
            val ids = session.archerIds
            archers.filter { it.id in ids }.sortedBy { ids.indexOf(it.id) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Shots for the current end across all archers in session
    val currentEndShots: StateFlow<List<ArrowShot>> = combine(activeSessionShots, currentEndNum) { shots, endNum ->
        shots.filter { it.endNumber == endNum }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Archer
    val activeArcher: StateFlow<Archer?> = combine(activeSessionArchers, activeArcherIdx) { archers, idx ->
        if (archers.isEmpty() || idx !in archers.indices) null
        else archers[idx]
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- ANALYTICS FILTER STATE ---
    private val _selectedFilterArcherId = MutableStateFlow<Int?>(null)
    val selectedFilterArcherId: StateFlow<Int?> = _selectedFilterArcherId.asStateFlow()

    private val _selectedFilterDistance = MutableStateFlow<String?>(null)
    val selectedFilterDistance: StateFlow<String?> = _selectedFilterDistance.asStateFlow()

    // --- INITIALIZE DEFAULT ARCHERS ---
    init {
        viewModelScope.launch {
            repository.getAllArchers().first().let { archers ->
                if (archers.isEmpty()) {
                    repository.insertArcher(Archer(name = "Robin Hood", colorHex = "#FFD700"))
                    repository.insertArcher(Archer(name = "Artemis", colorHex = "#FF3333"))
                    repository.insertArcher(Archer(name = "Katniss", colorHex = "#3366FF"))
                }
            }
        }
    }

    // --- ACTIONS ---

    fun selectSession(sessionId: Int?) {
        _activeSessionId.value = sessionId
        _currentEndNum.value = 1
        _activeArcherIdx.value = 0
        _isInputLocked.value = false
    }

    fun setFilterArcher(archerId: Int?) {
        _selectedFilterArcherId.value = archerId
    }

    fun setFilterDistance(distance: String?) {
        _selectedFilterDistance.value = distance
    }

    fun toggleInputLock() {
        _isInputLocked.value = !_isInputLocked.value
    }

    fun createArcher(name: String, colorHex: String) {
        viewModelScope.launch {
            repository.insertArcher(Archer(name = name, colorHex = colorHex))
        }
    }

    fun createSession(
        name: String,
        presetType: String,
        distanceName: String,
        distanceUnit: String,
        targetSizeCm: Double,
        totalEnds: Int,
        shotsPerEnd: Int,
        targetFaceType: String,
        isInfinite: Boolean,
        selectedArcherIds: List<Int>
    ) {
        viewModelScope.launch {
            if (selectedArcherIds.isEmpty()) return@launch
            val session = ArcherySession(
                name = name.ifBlank { "Session Custom" },
                presetType = presetType,
                distanceName = distanceName,
                distanceUnit = distanceUnit,
                targetSizeCm = targetSizeCm,
                totalEnds = totalEnds,
                shotsPerEnd = shotsPerEnd,
                targetFaceType = targetFaceType,
                isInfinite = isInfinite,
                archerIdsString = selectedArcherIds.joinToString(",")
            )
            val sessionId = repository.insertSession(session).toInt()
            selectSession(sessionId)
        }
    }

    fun recordShot(valueString: String, valueNum: Int, posX: Float? = null, posY: Float? = null) {
        val session = activeSession.value ?: return
        if (isInputLocked.value) return
        val archer = activeArcher.value ?: return
        val endNum = currentEndNum.value

        viewModelScope.launch {
            // Find existing shots for this archer in this end to determine shot index/count
            val currentShots = activeSessionShots.value.filter {
                it.endNumber == endNum && it.archerId == archer.id
            }

            // Cap the shots unless it is a "Draw Until" / infinite custom round or we haven't reached the limit
            if (!session.isInfinite && currentShots.size >= session.shotsPerEnd) {
                // If this archer already completed their shots, rotate to next or do nothing
                rotateArcher()
                return@launch
            }

            val nextShotNum = currentShots.size + 1
            val shot = ArrowShot(
                sessionId = session.id,
                endNumber = endNum,
                shotNumber = nextShotNum,
                archerId = archer.id,
                valueString = valueString,
                valueNum = valueNum,
                posX = posX,
                posY = posY
            )
            repository.insertShot(shot)

            // Auto-rotate if archer is done
            val updatedShotsCount = nextShotNum
            val maxShots = if (session.isInfinite) 999999 else session.shotsPerEnd
            if (updatedShotsCount >= maxShots) {
                rotateArcher()
            }
        }
    }

    fun rotateArcher() {
        val archers = activeSessionArchers.value
        if (archers.isEmpty()) return
        val nextIdx = (_activeArcherIdx.value + 1) % archers.size
        _activeArcherIdx.value = nextIdx
    }

    fun selectArcherByIndex(idx: Int) {
        val archers = activeSessionArchers.value
        if (idx in archers.indices) {
            _activeArcherIdx.value = idx
        }
    }

    fun undoLastShot() {
        val session = activeSession.value ?: return
        if (isInputLocked.value) return
        val archer = activeArcher.value ?: return
        val endNum = currentEndNum.value

        viewModelScope.launch {
            val lastShot = repository.getLastShotForArcherInEnd(session.id, endNum, archer.id)
            if (lastShot != null) {
                repository.deleteShot(lastShot)
            } else {
                // If current archer has no shots, check if previous archer in rotation has shots, and switch to them
                val archers = activeSessionArchers.value
                if (archers.size > 1) {
                    val prevIdx = (_activeArcherIdx.value - 1 + archers.size) % archers.size
                    val prevArcher = archers[prevIdx]
                    val prevArcherLastShot = repository.getLastShotForArcherInEnd(session.id, endNum, prevArcher.id)
                    if (prevArcherLastShot != null) {
                        _activeArcherIdx.value = prevIdx
                        repository.deleteShot(prevArcherLastShot)
                    }
                }
            }
        }
    }

    fun clearEnd() {
        val session = activeSession.value ?: return
        if (isInputLocked.value) return
        val archer = activeArcher.value ?: return
        val endNum = currentEndNum.value

        viewModelScope.launch {
            repository.clearEndForArcher(session.id, endNum, archer.id)
        }
    }

    fun nextEnd() {
        val session = activeSession.value ?: return
        val maxEnds = if (session.isInfinite) 999999 else session.totalEnds
        if (_currentEndNum.value < maxEnds) {
            _currentEndNum.value += 1
            _activeArcherIdx.value = 0 // reset rotation to first archer
        }
    }

    fun prevEnd() {
        if (_currentEndNum.value > 1) {
            _currentEndNum.value -= 1
            _activeArcherIdx.value = 0 // reset rotation
        }
    }

    fun saveEndNotes(notes: String) {
        val session = activeSession.value ?: return
        val endNum = currentEndNum.value
        viewModelScope.launch {
            val endSession = repository.getEndByNumber(session.id, endNum)
            if (endSession == null) {
                repository.insertEnd(EndSession(sessionId = session.id, endNumber = endNum, notes = notes))
            } else {
                repository.updateEnd(endSession.copy(notes = notes))
            }
        }
    }

    fun endNotesForCurrentEnd(): String {
        val endNum = currentEndNum.value
        return activeSessionEnds.value.find { it.endNumber == endNum }?.notes ?: ""
    }

    fun markSessionCompleted() {
        val session = activeSession.value ?: return
        viewModelScope.launch {
            repository.updateSession(session.copy(isCompleted = true))
        }
    }

    // --- PORTABILITY & SHARING UTILS ---

    fun getExcelXmlExportString(differentSheets: Boolean): String {
        val session = activeSession.value ?: return ""
        val archers = activeSessionArchers.value
        val shots = activeSessionShots.value
        return DataExporter.exportToExcelXml(session, archers, shots, differentSheets)
    }

    fun getBackupString(): String {
        val archers = allArchers.value
        val sessions = allSessions.value
        // We'll run blocking scope or collect database tables using non-flow helpers or just simple query lists
        // But since we want to be fully asynchronous, we can fetch all and serialize
        // To keep it safe, let's fetch in viewModelScope and return
        // We'll do this on thread. For simplicity, we can do it asynchronously
        return DataExporter.exportDb(archers, sessions, emptyList(), activeSessionShots.value)
    }

    fun restoreBackup(backupJson: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = DataExporter.importDb(backupJson, repository)
            onComplete(result)
        }
    }

    // --- THEME STATE (Toggle between Dark and Light mode) ---
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    // --- FACTORY PROVIDER ---
    companion object {
        fun provideFactory(
            application: Application,
            repository: ArcheryRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ArcheryViewModel(application, repository) as T
            }
        }
    }
}
