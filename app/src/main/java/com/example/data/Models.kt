package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "archers")
data class Archer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val colorHex: String // e.g., "#FFD700" (Gold), "#FF3333" (Red), "#3366FF" (Blue)
)

@Entity(tableName = "archery_sessions")
data class ArcherySession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val presetType: String, // "FITA_OLYMPIC", "FITA_FIELD", "IMPERIAL", "CUSTOM"
    val distanceName: String, // e.g. "70" or "50"
    val distanceUnit: String, // "meters" or "yards"
    val targetSizeCm: Double, // e.g. 122.0
    val totalEnds: Int, // e.g. 6 or 12
    val shotsPerEnd: Int, // e.g. 6 or 3
    val targetFaceType: String, // "TEN_RING", "FIVE_RING", "IMPERIAL", "FIELD"
    val isInfinite: Boolean = false, // Draw Until infinite scoring
    val isCompleted: Boolean = false,
    val archerIdsString: String, // Comma-separated archer IDs, e.g. "1,2"
    val timestamp: Long = System.currentTimeMillis()
) {
    val archerIds: List<Int>
        get() = archerIdsString.split(",").mapNotNull { it.trim().toIntOrNull() }
}

@Entity(tableName = "end_sessions")
data class EndSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: Int,
    val endNumber: Int,
    val notes: String? = null
)

@Entity(tableName = "arrow_shots")
data class ArrowShot(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: Int,
    val endNumber: Int,
    val shotNumber: Int,
    val archerId: Int,
    val valueString: String, // "X", "10", "9", "8", "7", "6", "5", "4", "3", "2", "1", "M"
    val valueNum: Int, // 10, 10, 9, 8, etc. (X is 10)
    val posX: Float? = null, // Plot coordinates relative to target center (-1.0 to 1.0)
    val posY: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
)
