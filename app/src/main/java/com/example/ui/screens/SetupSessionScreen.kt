package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Archer
import com.example.ui.ArcheryViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupSessionScreen(
    viewModel: ArcheryViewModel,
    onNavigateToTarget: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allArchers by viewModel.allArchers.collectAsStateWithLifecycle()
    val allSessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()

    // Form States
    var roundName by remember { mutableStateOf("") }
    var presetType by remember { mutableStateOf("FITA_OLYMPIC") }
    var distanceName by remember { mutableStateOf("70") }
    var distanceUnit by remember { mutableStateOf("meters") }
    var targetSizeCm by remember { mutableStateOf("122") }
    var totalEnds by remember { mutableStateOf("12") }
    var shotsPerEnd by remember { mutableStateOf("6") }
    var targetFaceType by remember { mutableStateOf("TEN_RING") }
    var isInfinite by remember { mutableStateOf(false) }

    val selectedArcherIds = remember { mutableStateListOf<Int>() }

    // Quick Add Archer Dialog States
    var showAddArcherDialog by remember { mutableStateOf(false) }
    var newArcherName by remember { mutableStateOf("") }
    var newArcherColor by remember { mutableStateOf("#FFD700") } // Default gold

    val presetRounds = listOf(
        PresetOption("FITA Olympic Preset", "FITA_OLYMPIC", "70", "meters", 122.0, 12, 6, "TEN_RING", false),
        PresetOption("FITA Field Preset", "FITA_FIELD", "50", "meters", 80.0, 8, 3, "FIVE_RING", false),
        PresetOption("Imperial York Preset", "IMPERIAL", "100", "yards", 122.0, 12, 6, "IMPERIAL", false),
        PresetOption("Draw Until (Infinite)", "CUSTOM", "30", "meters", 60.0, 99, 6, "TEN_RING", true)
    )

    // Sync selected archers when default loads
    LaunchedEffect(allArchers) {
        if (selectedArcherIds.isEmpty() && allArchers.isNotEmpty()) {
            selectedArcherIds.add(allArchers.first().id)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CharcoalBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- ACTIVE SESSION QUICK ACTION ---
        if (activeSession != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    border = BorderStroke(1.dp, CyberGreen)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ACTIVE ROUND RUNNING",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberGreen,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = activeSession!!.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = CrispWhite
                            )
                        }
                        Button(
                            onClick = onNavigateToTarget,
                            colors = ButtonDefaults.buttonColors(containerColor = CyberGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("GO TO TARGET", color = Color(0xFF121212), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        // --- TITLE ---
        item {
            Text(
                text = "SCORIFY ARCHERY CONFIG",
                style = MaterialTheme.typography.displayMedium,
                color = CrispWhite,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Select an Olympic/Field preset or customize your infinite Draw Until target structures.",
                style = MaterialTheme.typography.bodyMedium,
                color = CoolGray
            )
        }

        // --- PRESET QUICK OPTIONS ---
        item {
            Text(
                text = "SELECT STANDARD PRESETS",
                style = MaterialTheme.typography.labelLarge,
                color = ArcheryGold,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presetRounds.take(3).forEach { option ->
                    val isSelected = presetType == option.presetType && !isInfinite
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                presetType = option.presetType
                                roundName = option.name
                                distanceName = option.distance
                                distanceUnit = option.unit
                                targetSizeCm = option.sizeCm.toString()
                                totalEnds = option.ends.toString()
                                shotsPerEnd = option.spe.toString()
                                targetFaceType = option.faceType
                                isInfinite = option.isInfinite
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) CharcoalSurface else CharcoalCard
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) ArcheryGold else Color(0xFF333333)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = option.name,
                                tint = if (isSelected) ArcheryGold else CoolGray,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = option.name.split(" ")[0],
                                style = MaterialTheme.typography.labelSmall,
                                color = CrispWhite,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "${option.distance}${option.unit.take(1)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CoolGray,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // --- CUSTOM ROUND ENGINE FORMS ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = BorderStroke(1.dp, Color(0xFF333333))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CUSTOM ROUND PARAMETERS",
                        style = MaterialTheme.typography.labelLarge,
                        color = ArcheryGold,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Round Name
                    OutlinedTextField(
                        value = roundName,
                        onValueChange = { roundName = it },
                        label = { Text("Round Name") },
                        placeholder = { Text("e.g. Backyard Match") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("round_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArcheryGold,
                            unfocusedBorderColor = Color(0xFF444444),
                            focusedLabelColor = ArcheryGold
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row of parameters
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = distanceName,
                            onValueChange = { distanceName = it },
                            label = { Text("Distance") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("distance_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ArcheryGold,
                                unfocusedBorderColor = Color(0xFF444444)
                            )
                        )

                        // Unit Select
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .padding(top = 8.dp)
                                .background(CharcoalCard, RoundedCornerShape(4.dp))
                                .border(1.dp, Color(0xFF444444), RoundedCornerShape(4.dp))
                                .clickable {
                                    distanceUnit = if (distanceUnit == "meters") "yards" else "meters"
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = distanceUnit.uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                color = CrispWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = targetSizeCm,
                            onValueChange = { targetSizeCm = it },
                            label = { Text("Target Size (cm)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ArcheryGold,
                                unfocusedBorderColor = Color(0xFF444444)
                            )
                        )

                        // Face Selector
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .padding(top = 8.dp)
                                .background(CharcoalCard, RoundedCornerShape(4.dp))
                                .border(1.dp, Color(0xFF444444), RoundedCornerShape(4.dp))
                                .clickable {
                                    targetFaceType = when (targetFaceType) {
                                        "TEN_RING" -> "FIVE_RING"
                                        "FIVE_RING" -> "IMPERIAL"
                                        "IMPERIAL" -> "FIELD"
                                        else -> "TEN_RING"
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = targetFaceType.replace("_", " "),
                                style = MaterialTheme.typography.labelSmall,
                                color = ArcheryGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = totalEnds,
                            onValueChange = { totalEnds = it },
                            label = { Text("Total Ends") },
                            modifier = Modifier.weight(1f),
                            enabled = !isInfinite,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ArcheryGold,
                                unfocusedBorderColor = Color(0xFF444444)
                            )
                        )

                        OutlinedTextField(
                            value = shotsPerEnd,
                            onValueChange = { shotsPerEnd = it },
                            label = { Text("Shots Per End") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ArcheryGold,
                                unfocusedBorderColor = Color(0xFF444444)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Draw Until Stub Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Infinite Draw Until Scoring",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CrispWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Shoot infinitely until closed manually. Overrides preset end limits.",
                                style = MaterialTheme.typography.labelSmall,
                                color = CoolGray
                            )
                        }
                        Switch(
                            checked = isInfinite,
                            onCheckedChange = { isInfinite = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ArcheryGold,
                                checkedTrackColor = ArcheryGold.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        }

        // --- MULTI-ARCHER CONCURRENT REGISTRATION ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SESSION ARCHERS ROTATION",
                    style = MaterialTheme.typography.labelLarge,
                    color = ArcheryGold,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { showAddArcherDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalCard),
                    border = BorderStroke(1.dp, Color(0xFF444444)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Archer", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ADD NEW", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Archers selection grid / checklist
        items(allArchers) { archer ->
            val isSelected = archer.id in selectedArcherIds
            val archerColor = Color(android.graphics.Color.parseColor(archer.colorHex))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CharcoalSurface, RoundedCornerShape(8.dp))
                    .border(
                        1.dp,
                        if (isSelected) archerColor else Color(0xFF333333),
                        RoundedCornerShape(8.dp)
                    )
                    .clickable {
                        if (isSelected) {
                            if (selectedArcherIds.size > 1) {
                                selectedArcherIds.remove(archer.id)
                            }
                        } else {
                            selectedArcherIds.add(archer.id)
                        }
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(archerColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = archer.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = CrispWhite,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }

                if (isSelected) {
                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = archerColor)
                }
            }
        }

        // --- SUBMIT / START SESSION ---
        item {
            Button(
                onClick = {
                    viewModel.createSession(
                        name = roundName.ifBlank { "Session ${presetType.replace("_", " ")}" },
                        presetType = presetType,
                        distanceName = distanceName,
                        distanceUnit = distanceUnit,
                        targetSizeCm = targetSizeCm.toDoubleOrNull() ?: 122.0,
                        totalEnds = if (isInfinite) 99 else (totalEnds.toIntOrNull() ?: 12),
                        shotsPerEnd = shotsPerEnd.toIntOrNull() ?: 6,
                        targetFaceType = targetFaceType,
                        isInfinite = isInfinite,
                        selectedArcherIds = selectedArcherIds.toList()
                    )
                    onNavigateToTarget()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("launch_round_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ArcheryGold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "LAUNCH ACTIVE SESSION",
                    color = Color(0xFF121212),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // --- QUICK ADD ARCHER DIALOG ---
    if (showAddArcherDialog) {
        AlertDialog(
            onDismissRequest = { showAddArcherDialog = false },
            title = {
                Text(
                    "REGISTER NEW ARCHER",
                    style = MaterialTheme.typography.titleMedium,
                    color = CrispWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newArcherName,
                        onValueChange = { newArcherName = it },
                        label = { Text("Archer Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArcheryGold,
                            unfocusedBorderColor = Color(0xFF444444)
                        )
                    )

                    Text(
                        "ASSIGN IDENTIFICATION COLOR",
                        style = MaterialTheme.typography.labelSmall,
                        color = CoolGray
                    )

                    val colorOptions = listOf("#FFD700", "#FF3333", "#3366FF", "#00FF66", "#FF9500", "#FFFFFF")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        colorOptions.forEach { hex ->
                            val c = Color(android.graphics.Color.parseColor(hex))
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(c, CircleShape)
                                    .border(
                                        width = if (newArcherColor == hex) 3.dp else 1.dp,
                                        color = if (newArcherColor == hex) CrispWhite else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { newArcherColor = hex }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newArcherName.isNotBlank()) {
                            viewModel.createArcher(newArcherName, newArcherColor)
                            newArcherName = ""
                            showAddArcherDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ArcheryGold)
                ) {
                    Text("REGISTER", color = Color(0xFF121212))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddArcherDialog = false }) {
                    Text("CANCEL", color = CoolGray)
                }
            },
            containerColor = CharcoalSurface
        )
    }
}

data class PresetOption(
    val name: String,
    val presetType: String,
    val distance: String,
    val unit: String,
    val sizeCm: Double,
    val ends: Int,
    val spe: Int,
    val faceType: String,
    val isInfinite: Boolean
)
