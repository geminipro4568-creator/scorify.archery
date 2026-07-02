package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ArcheryDatabase
import com.example.data.ArcheryRepository
import com.example.ui.ArcheryViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Database & Repository
        val database = ArcheryDatabase.getInstance(applicationContext)
        val repository = ArcheryRepository(database.archeryDao())

        // Obtain ViewModel using factory
        val viewModel: ArcheryViewModel by viewModels {
            ArcheryViewModel.provideFactory(application, repository)
        }

        setContent {
            MyApplicationTheme {
                MainAppFrame(viewModel)
            }
        }
    }
}

enum class NavigationTab(val label: String, val icon: ImageVector) {
    SETUP("SETUP", Icons.Default.Adjust),
    TARGET("TARGET", Icons.Default.TrackChanges),
    SCORECARD("SCORECARD", Icons.Default.TableChart),
    ANALYTICS("METRICS", Icons.Default.Leaderboard),
    PORTABILITY("EXPORT", Icons.Default.CloudSync)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppFrame(viewModel: ArcheryViewModel) {
    var activeTab by remember { mutableStateOf(NavigationTab.SETUP) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBg),
        topBar = {
            val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()
            val currentEndNum by viewModel.currentEndNum.collectAsStateWithLifecycle()

            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "SCORIFY",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = ArcheryGold,
                            letterSpacing = (-0.5).sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(3.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = activeSession?.targetFaceType ?: "FITA OLYMPIC",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.sp,
                                    color = CrispWhite.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = if (activeSession != null) {
                                    "${activeSession?.distanceName} ${activeSession?.distanceUnit} • End $currentEndNum/${if (activeSession?.isInfinite == true) "∞" else activeSession?.totalEnds}"
                                } else {
                                    "No Active Round"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = CoolGray,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        // Online Cyber-Sport Indicator Tag
                        Box(
                            modifier = Modifier
                                .background(CharcoalDark, RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(CyberGreen, RoundedCornerShape(3.dp))
                                )
                                Text(
                                    text = "CYBER-SPORT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.sp,
                                    color = CyberGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Dynamic Theme Toggle Button (Light/Dark Mode Switcher)
                        IconButton(
                            onClick = {
                                isDarkThemeGlobal = !isDarkThemeGlobal
                                viewModel.toggleTheme()
                            },
                            modifier = Modifier.testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isDarkThemeGlobal) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme",
                                tint = ArcheryGold
                            )
                        }

                        // Avatar AV
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(CharcoalSurface, CircleShape)
                                .border(2.dp, ArcheryGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "AV",
                                style = MaterialTheme.typography.labelSmall,
                                color = CrispWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CharcoalBg,
                    titleContentColor = CrispWhite
                )
            )
        },
        bottomBar = {
            // High-performance cyber sport styled M3 bottom bar
            NavigationBar(
                containerColor = CharcoalSurface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier
                    .border(width = (0.5).dp, color = Color(0xFF2C2C2C))
                    .testTag("scorify_navigation_bar")
            ) {
                NavigationTab.values().forEach { tab ->
                    val isSelected = activeTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { activeTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = if (isSelected) ArcheryGold else CoolGray,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = if (isSelected) ArcheryGold else CoolGray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ArcheryGold,
                            unselectedIconColor = CoolGray,
                            indicatorColor = CharcoalCard
                        )
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        // Render corresponding screen based on active selection state
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding(), bottom = paddingValues.calculateBottomPadding())
                .background(CharcoalBg)
        ) {
            when (activeTab) {
                NavigationTab.SETUP -> SetupSessionScreen(
                    viewModel = viewModel,
                    onNavigateToTarget = { activeTab = NavigationTab.TARGET }
                )
                NavigationTab.TARGET -> TargetScreen(
                    viewModel = viewModel
                )
                NavigationTab.SCORECARD -> ScorecardScreen(
                    viewModel = viewModel
                )
                NavigationTab.ANALYTICS -> AnalyticsScreen(
                    viewModel = viewModel
                )
                NavigationTab.PORTABILITY -> PortabilityScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}
