package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Archer
import com.example.data.ArrowShot
import com.example.ui.ArcheryViewModel
import com.example.ui.theme.*

@Composable
fun ScorecardScreen(
    viewModel: ArcheryViewModel,
    modifier: Modifier = Modifier
) {
    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()
    val activeSessionArchers by viewModel.activeSessionArchers.collectAsStateWithLifecycle()
    val activeSessionShots by viewModel.activeSessionShots.collectAsStateWithLifecycle()
    val activeSessionEnds by viewModel.activeSessionEnds.collectAsStateWithLifecycle()
    val currentEndNum by viewModel.currentEndNum.collectAsStateWithLifecycle()

    var colorCodeCells by remember { mutableStateOf(true) }
    var noteDialogEndNum by remember { mutableStateOf<Int?>(null) }
    var currentNoteText by remember { mutableStateOf("") }

    if (activeSession == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(CharcoalBg)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.EditCalendar,
                    contentDescription = "No scorecard",
                    tint = ArcheryGold,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "NO ACTIVE SCOREBOARD",
                    style = MaterialTheme.typography.titleMedium,
                    color = CrispWhite,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    val session = activeSession!!
    val spe = session.shotsPerEnd
    val totalExpectedEnds = session.totalEnds

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CharcoalBg)
            .padding(16.dp)
    ) {
        // --- SCORECARD TITLE & TOGGLE ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "OFFICIAL SCORECARD",
                    style = MaterialTheme.typography.titleLarge,
                    color = CrispWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${session.name} — ${session.distanceName} ${session.distanceUnit}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ArcheryGold
                )
            }

            // Coloring Toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "COLOR",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (colorCodeCells) ArcheryGold else CoolGray,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Switch(
                    checked = colorCodeCells,
                    onCheckedChange = { colorCodeCells = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ArcheryGold,
                        checkedTrackColor = ArcheryGold.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.scale(0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- DYNAMIC SCROLLABLE GRID SCOREBOARD ---
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Group ends and shots for each competing archer
            items(activeSessionArchers) { archer ->
                val archerShots = activeSessionShots.filter { it.archerId == archer.id }
                val archerColor = Color(android.graphics.Color.parseColor(archer.colorHex))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    border = BorderStroke(1.dp, archerColor.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Archer Heading
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(archerColor, RoundedCornerShape(2.dp))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = archer.name.uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CrispWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            val archerTotalSum = archerShots.sumOf { it.valueNum }
                            Text(
                                text = "TOTAL: $archerTotalSum",
                                style = MaterialTheme.typography.titleMedium,
                                color = CyberGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Grid Table Header Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CharcoalBg, RoundedCornerShape(4.dp))
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "END",
                                modifier = Modifier.width(42.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = CoolGray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ARROWS",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelSmall,
                                color = CoolGray,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "ET",
                                modifier = Modifier.width(45.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = CoolGray,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "RT",
                                modifier = Modifier.width(55.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = CoolGray,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "NOTE",
                                modifier = Modifier.width(42.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = CoolGray,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Render each End Row dynamically (up to active or configured ends)
                        val maxActiveEndInShots = archerShots.maxByOrNull { it.endNumber }?.endNumber ?: 1
                        val displayedEndsCount = maxOf(currentEndNum, maxActiveEndInShots)

                        var runningTotal = 0
                        for (endIdx in 1..displayedEndsCount) {
                            val endShots = archerShots.filter { it.endNumber == endIdx }.sortedBy { it.shotNumber }
                            val endTotal = endShots.sumOf { it.valueNum }
                            runningTotal += endTotal

                            val hasNotes = activeSessionEnds.any { it.endNumber == endIdx && !it.notes.isNullOrBlank() }
                            val noteContent = activeSessionEnds.find { it.endNumber == endIdx }?.notes ?: ""

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .border(
                                        width = if (endIdx == currentEndNum) 1.dp else 0.dp,
                                        color = if (endIdx == currentEndNum) CyberGreen else Color.Transparent,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // End Index Column
                                Text(
                                    text = "#$endIdx",
                                    modifier = Modifier.width(42.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (endIdx == currentEndNum) CyberGreen else CrispWhite,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                // Arrows Sub-Matrix Row
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    // Make sure we space out columns based on shotsPerEnd
                                    for (arrowIdx in 0 until spe) {
                                        val shot = endShots.getOrNull(arrowIdx)
                                        val valStr = shot?.valueString ?: "-"
                                        val (bg, fg) = if (colorCodeCells && shot != null) {
                                            getColorForValue(shot.valueString)
                                        } else {
                                            Pair(CharcoalCard, CrispWhite)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 2.dp)
                                                .size(24.dp)
                                                .background(bg, RoundedCornerShape(4.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = valStr,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = fg
                                            )
                                        }
                                    }
                                }

                                // End Total Column (ET)
                                Text(
                                    text = "$endTotal",
                                    modifier = Modifier.width(45.dp),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CrispWhite,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                // Running Total Column (RT)
                                Text(
                                    text = "$runningTotal",
                                    modifier = Modifier.width(55.dp),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ArcheryGold,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                // End Notes badge Trigger Click Overlay
                                Box(
                                    modifier = Modifier
                                        .width(42.dp)
                                        .clickable {
                                            noteDialogEndNum = endIdx
                                            currentNoteText = noteContent
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NoteAlt,
                                        contentDescription = "Edit end notes",
                                        tint = if (hasNotes) CyberGreen else CoolGray.copy(alpha = 0.5f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            HorizontalDivider(color = Color(0xFF2C2C2C), thickness = (0.5).dp)
                        }
                    }
                }
            }
        }

        // --- Qualitative Context Notes Popup Dialog Overlay ---
        if (noteDialogEndNum != null) {
            Dialog(onDismissRequest = { noteDialogEndNum = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    border = BorderStroke(1.dp, ArcheryGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "QUALITATIVE END #${noteDialogEndNum} NOTES",
                                style = MaterialTheme.typography.labelMedium,
                                color = ArcheryGold,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { noteDialogEndNum = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = CoolGray)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = currentNoteText,
                            onValueChange = { currentNoteText = it },
                            placeholder = { Text("e.g. Heavy wind gusts from left, loose sight block pin detected") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ArcheryGold,
                                unfocusedBorderColor = Color(0xFF444444)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { noteDialogEndNum = null }) {
                                Text("CANCEL", color = CoolGray)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    // Save the text to model state persistence
                                    viewModel.saveEndNotes(currentNoteText)
                                    noteDialogEndNum = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ArcheryGold)
                            ) {
                                Text("SAVE NOTE", color = Color(0xFF121212))
                            }
                        }
                    }
                }
            }
        }
    }
}

// Score matrix helpers
fun getColorForValue(valStr: String): Pair<Color, Color> {
    val darkCharcoal = Color(0xFF121212)
    return when (valStr) {
        "X", "10", "9" -> Pair(ArcheryGold, darkCharcoal)
        "8", "7" -> Pair(ArcheryRed, Color.White)
        "6", "5" -> Pair(ArcheryBlue, Color.White)
        "4", "3" -> Pair(ArcheryBlack, Color.White)
        "2", "1" -> Pair(ArcheryWhite, darkCharcoal)
        else -> Pair(CharcoalBg, CoolGray)
    }
}
