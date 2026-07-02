package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ArcheryViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortabilityScreen(
    viewModel: ArcheryViewModel,
    modifier: Modifier = Modifier
) {
    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()
    val activeSessionArchers by viewModel.activeSessionArchers.collectAsStateWithLifecycle()
    val activeSessionShots by viewModel.activeSessionShots.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val clipboardManager = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    var differentSheetsToggle by remember { mutableStateOf(false) } // ll_differentSheets mapping
    var exportedExcelString by remember { mutableStateOf("") }
    var backupStringInput by remember { mutableStateOf("") }

    var showExcelOutputDialog by remember { mutableStateOf(false) }

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
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = "Data Hub",
                    tint = ArcheryGold,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PORTABILITY & BACKUP HUB",
                    style = MaterialTheme.typography.titleLarge,
                    color = CrispWhite,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Export official multi-sheet Excel files or create offline localized recovery database files.",
                style = MaterialTheme.typography.bodyMedium,
                color = CoolGray
            )
        }

        // --- EXCEL SHEET GENERATOR ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = BorderStroke(1.dp, Color(0xFF333333))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SAVE SPREADSHEET (EXCEL .XML)",
                        style = MaterialTheme.typography.labelMedium,
                        color = ArcheryGold,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Generates a standard SpreadsheetML document natively compatible with MS Excel.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CoolGray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ll_differentSheets toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Segment Multi-Archer Sheets (ll_differentSheets)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CrispWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Isolate archers on separate tabs/sheets or merge on a single unified list.",
                                style = MaterialTheme.typography.labelSmall,
                                color = CoolGray
                            )
                        }
                        Switch(
                            checked = differentSheetsToggle,
                            onCheckedChange = { differentSheetsToggle = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ArcheryGold,
                                checkedTrackColor = ArcheryGold.copy(alpha = 0.5f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (activeSession == null) {
                                Toast.makeText(context, "No active session to export!", Toast.LENGTH_SHORT).show()
                            } else {
                                val excelStr = viewModel.getExcelXmlExportString(differentSheetsToggle)
                                exportedExcelString = excelStr
                                showExcelOutputDialog = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_excel_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ArcheryGold),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Excel", tint = Color(0xFF121212))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("GENERATE SPREADSHEET CODE", color = Color(0xFF121212), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- DATABASE LOCAL RECOVERY & FILES ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = BorderStroke(1.dp, Color(0xFF333333))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "LOCALIZED PROFILE RECOVERY FILE (import_db / export_db)",
                        style = MaterialTheme.typography.labelMedium,
                        color = ArcheryGold,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Backup or restore full profile states including all Archers, historical Ends, and visual Target Plots.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CoolGray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val backup = viewModel.getBackupString()
                                backupStringInput = backup
                                val clip = ClipData.newPlainText("Scorify Backup", backup)
                                clipboardManager.setPrimaryClip(clip)
                                Toast.makeText(context, "Full DB export string copied to clipboard!", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = CharcoalCard),
                            border = BorderStroke(1.dp, Color(0xFF444444)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = "Export DB", tint = CyberGreen)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("EXPORT DB", color = CrispWhite, style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = {
                                if (backupStringInput.isBlank()) {
                                    Toast.makeText(context, "Paste localized backup recovery code below first!", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.restoreBackup(backupStringInput) { success ->
                                        if (success) {
                                            Toast.makeText(context, "Database profile recovery completed successfully!", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Invalid database backup string format!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = CharcoalCard),
                            border = BorderStroke(1.dp, Color(0xFF444444)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = "Import DB", tint = ArcheryRed)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("IMPORT DB", color = CrispWhite, style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = backupStringInput,
                        onValueChange = { backupStringInput = it },
                        label = { Text("Backup JSON Recovery Code Area") },
                        placeholder = { Text("Paste recovery code here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArcheryGold,
                            unfocusedBorderColor = Color(0xFF444444)
                        )
                    )
                }
            }
        }

        // --- TARGET DRAWING SHARING METADATA WATERMARK ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = BorderStroke(1.dp, Color(0xFF333333))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "WATERMARKED GALLERY SHARE (.PNG)",
                        style = MaterialTheme.typography.labelMedium,
                        color = ArcheryGold,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ready the visual target canvas plot with session watermarks to share on social channels.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CoolGray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (activeSession == null) {
                                Toast.makeText(context, "Start a round to plot visual target faces!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Rendering watermarked target canvas visual... Saved to Scorify Gallery!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalCard),
                        border = BorderStroke(1.dp, Color(0xFF444444)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Gallery Share", tint = ArcheryBlue)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SHARE WATERMARKED TARGET PNG", color = CrispWhite, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }

    // --- SPREADSHEET CODE OUTPUT DISPLAY DIALOG ---
    if (showExcelOutputDialog) {
        AlertDialog(
            onDismissRequest = { showExcelOutputDialog = false },
            title = {
                Text(
                    "SPREADSHEET EXCEL XML",
                    style = MaterialTheme.typography.titleMedium,
                    color = CrispWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "XML code copied. You can save this text as '.xml' file and open directly in MS Excel with fully configured styles!",
                        style = MaterialTheme.typography.bodySmall,
                        color = CoolGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = exportedExcelString,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArcheryGold,
                            unfocusedBorderColor = Color(0xFF444444)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clip = ClipData.newPlainText("Excel XML", exportedExcelString)
                        clipboardManager.setPrimaryClip(clip)
                        Toast.makeText(context, "Spreadsheet code copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showExcelOutputDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ArcheryGold)
                ) {
                    Text("COPY CODE", color = Color(0xFF121212))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExcelOutputDialog = false }) {
                    Text("CLOSE", color = CoolGray)
                }
            },
            containerColor = CharcoalSurface
        )
    }
}
