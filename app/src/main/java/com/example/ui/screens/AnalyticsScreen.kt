package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Archer
import com.example.data.ArrowShot
import com.example.ui.ArcheryViewModel
import com.example.ui.theme.*

@Composable
fun AnalyticsScreen(
    viewModel: ArcheryViewModel,
    modifier: Modifier = Modifier
) {
    val allSessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val allArchers by viewModel.allArchers.collectAsStateWithLifecycle()
    val activeSessionShots by viewModel.activeSessionShots.collectAsStateWithLifecycle()

    val selectedFilterArcherId by viewModel.selectedFilterArcherId.collectAsStateWithLifecycle()
    val selectedFilterDistance by viewModel.selectedFilterDistance.collectAsStateWithLifecycle()

    var showArcherFilterMenu by remember { mutableStateOf(false) }
    var showDistanceFilterMenu by remember { mutableStateOf(false) }

    // Aggregate shots dynamically based on selected filters
    val filteredShots = remember(activeSessionShots, selectedFilterArcherId, selectedFilterDistance, allSessions) {
        activeSessionShots.filter { shot ->
            val matchArcher = (selectedFilterArcherId == null || shot.archerId == selectedFilterArcherId)
            val session = allSessions.find { it.id == shot.sessionId }
            val matchDistance = (selectedFilterDistance == null || session?.distanceName == selectedFilterDistance)
            matchArcher && matchDistance
        }
    }

    val totalShotsCount = filteredShots.size
    val sessionTotalScore = filteredShots.sumOf { it.valueNum }
    val averageShotValue = if (totalShotsCount > 0) sessionTotalScore.toFloat() / totalShotsCount else 0f

    // Group shots into Ends to compute End Averages
    val averageEndScore = remember(filteredShots) {
        val groupedByEnd = filteredShots.groupBy { "${it.sessionId}_${it.endNumber}_${it.archerId}" }
        if (groupedByEnd.isNotEmpty()) {
            val totalEndsSum = groupedByEnd.values.sumOf { endList -> endList.sumOf { it.valueNum } }
            totalEndsSum.toFloat() / groupedByEnd.size
        } else {
            0f
        }
    }

    // Compute Hit Distributions
    val hitDistributions = remember(filteredShots) {
        val goldCount = filteredShots.count { it.valueString in listOf("X", "10", "9") }
        val redCount = filteredShots.count { it.valueString in listOf("8", "7") }
        val blueCount = filteredShots.count { it.valueString in listOf("6", "5") }
        val blackCount = filteredShots.count { it.valueString in listOf("4", "3") }
        val whiteCount = filteredShots.count { it.valueString in listOf("2", "1") }
        val missCount = filteredShots.count { it.valueString == "M" }

        listOf(
            DistItem("Gold (X,10,9)", goldCount, ArcheryGold),
            DistItem("Red (8,7)", redCount, ArcheryRed),
            DistItem("Blue (6,5)", blueCount, ArcheryBlue),
            DistItem("Black (4,3)", blackCount, ArcheryBlack),
            DistItem("White (2,1)", whiteCount, ArcheryWhite),
            DistItem("Miss (M)", missCount, CharcoalCard)
        )
    }

    val maxShotsCountInDist = maxOf(1, hitDistributions.maxOf { it.count })

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CharcoalBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- TITLE ---
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = "Analytics",
                    tint = ArcheryGold,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ANALYTICS ENGINE",
                    style = MaterialTheme.typography.titleLarge,
                    color = CrispWhite,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Performance dashboard tracking dynamic shooting vectors & arrow point distributions.",
                style = MaterialTheme.typography.bodyMedium,
                color = CoolGray
            )
        }

        // --- FILTER MODULE CONTROLS (ddl_rounds / ddl_dists stubs) ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = BorderStroke(1.dp, Color(0xFF333333))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "DISSECT FILTER CHANNELS",
                        style = MaterialTheme.typography.labelSmall,
                        color = ArcheryGold,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Archer Filter Dropdown Selection
                        Box(modifier = Modifier.weight(1.5f)) {
                            Button(
                                onClick = { showArcherFilterMenu = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = CharcoalCard),
                                border = BorderStroke(1.dp, Color(0xFF444444)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val currentArcherName = if (selectedFilterArcherId == null) {
                                        "ALL ARCHERS"
                                    } else {
                                        allArchers.find { it.id == selectedFilterArcherId }?.name ?: "ALL ARCHERS"
                                    }
                                    Text(
                                        text = currentArcherName.take(12),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CrispWhite
                                    )
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = "Filter",
                                        tint = ArcheryGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showArcherFilterMenu,
                                onDismissRequest = { showArcherFilterMenu = false },
                                modifier = Modifier.background(CharcoalSurface)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("ALL ARCHERS", color = CrispWhite, style = MaterialTheme.typography.labelSmall) },
                                    onClick = {
                                        viewModel.setFilterArcher(null)
                                        showArcherFilterMenu = false
                                    }
                                )
                                allArchers.forEach { archer ->
                                    DropdownMenuItem(
                                        text = { Text(archer.name, color = CrispWhite, style = MaterialTheme.typography.labelSmall) },
                                        onClick = {
                                            viewModel.setFilterArcher(archer.id)
                                            showArcherFilterMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Distance Filter Dropdown Selection
                        Box(modifier = Modifier.weight(1.5f)) {
                            Button(
                                onClick = { showDistanceFilterMenu = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = CharcoalCard),
                                border = BorderStroke(1.dp, Color(0xFF444444)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedFilterDistance ?: "ALL DISTANCES",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CrispWhite
                                    )
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = "Filter",
                                        tint = ArcheryGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showDistanceFilterMenu,
                                onDismissRequest = { showDistanceFilterMenu = false },
                                modifier = Modifier.background(CharcoalSurface)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("ALL DISTANCES", color = CrispWhite, style = MaterialTheme.typography.labelSmall) },
                                    onClick = {
                                        viewModel.setFilterDistance(null)
                                        showDistanceFilterMenu = false
                                    }
                                )
                                val distances = allSessions.map { it.distanceName }.distinct()
                                distances.forEach { d ->
                                    DropdownMenuItem(
                                        text = { Text("$d DISTANCE", color = CrispWhite, style = MaterialTheme.typography.labelSmall) },
                                        onClick = {
                                            viewModel.setFilterDistance(d)
                                            showDistanceFilterMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- PERFORMANCE KPIS WIDGETS ---
        item {
            Text(
                text = "PERFORMANCE OVERALL KPIS",
                style = MaterialTheme.typography.labelLarge,
                color = ArcheryGold,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total Score
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    border = BorderStroke(1.dp, Color(0xFF333333))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("TOTAL POINTS", style = MaterialTheme.typography.labelSmall, color = CoolGray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$sessionTotalScore",
                            style = MaterialTheme.typography.displayMedium,
                            color = CyberGreen,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Average Arrow Shot
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    border = BorderStroke(1.dp, Color(0xFF333333))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("AVG ARROW", style = MaterialTheme.typography.labelSmall, color = CoolGray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%.2f", averageShotValue),
                            style = MaterialTheme.typography.displayMedium,
                            color = ArcheryGold,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Average End KPI
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = BorderStroke(1.dp, Color(0xFF333333))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("AVERAGE END SCORE", style = MaterialTheme.typography.labelSmall, color = CoolGray)
                        Text(
                            text = "Aggregated across all recorded set ends",
                            style = MaterialTheme.typography.bodySmall,
                            color = CoolGray
                        )
                    }
                    Text(
                        text = String.format("%.1f pts", averageEndScore),
                        style = MaterialTheme.typography.titleLarge,
                        color = ArcheryRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- ARROW HIT DISTRIBUTION ANALYTICAL CHART ---
        item {
            Text(
                text = "ARROW POINT DENSITY DISTRIBUTION",
                style = MaterialTheme.typography.labelLarge,
                color = ArcheryGold,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = BorderStroke(1.dp, Color(0xFF333333))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    hitDistributions.forEach { dist ->
                        val barPercent = if (totalShotsCount > 0) dist.count.toFloat() / totalShotsCount else 0f
                        val labelColor = if (dist.color == ArcheryBlack) CrispWhite else dist.color

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dist.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = CrispWhite,
                                modifier = Modifier.weight(1.2f)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Horizontal Bar Chart
                            Box(
                                modifier = Modifier
                                    .weight(2f)
                                    .height(16.dp)
                                    .background(CharcoalBg, RoundedCornerShape(4.dp))
                            ) {
                                if (barPercent > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(barPercent)
                                            .background(
                                                color = if (dist.color == ArcheryBlack) Color.DarkGray else dist.color,
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "${dist.count} (${String.format("%.0f", barPercent * 100)}%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = labelColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        }
    }
}

data class DistItem(
    val label: String,
    val count: Int,
    val color: Color
)
