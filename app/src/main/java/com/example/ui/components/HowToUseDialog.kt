package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import com.example.data.SciLabRepository
import com.example.model.UserRole
import com.example.ui.theme.SciAmber
import com.example.ui.theme.SciCyan
import com.example.ui.theme.SciEmerald

@Composable
fun HowToUseDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val userProfile by SciLabRepository.userProfile.collectAsState()
    val experiments by SciLabRepository.experiments.collectAsState()
    val dataPointsMap by SciLabRepository.dataPoints.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = SciCyan.copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.HelpOutline, contentDescription = null, tint = SciCyan, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("SciLab Guide & Help", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text("Quick tips & data storage information", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Navigation Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 0.dp,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = SciCyan
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("🚀 3-Step Guide", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("👥 Roles (Student/Teacher)", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("💾 Data Storage & Access", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Content
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        0 -> ThreeStepGuideTab()
                        1 -> StudentTeacherRolesTab(userProfile.role)
                        2 -> DataStorageAndAccessTab(
                            onExportAll = {
                                val summary = buildString {
                                    appendLine("=== SCILAB COMPLETE DATA EXPORT ===")
                                    appendLine("User: ${userProfile.name} (${userProfile.email}) - Role: ${userProfile.role}")
                                    appendLine("Active Plan: ${userProfile.activePlan}")
                                    appendLine("Experiments Total: ${experiments.size}")
                                    appendLine("====================================\n")
                                    experiments.forEachIndexed { i, exp ->
                                        val pts = dataPointsMap[exp.id] ?: emptyList()
                                        appendLine("${i + 1}. [${exp.category.displayName}] ${exp.title}")
                                        appendLine("   Status: ${exp.status.label} | Grade: ${exp.teacherGrade ?: "Ungraded"}")
                                        appendLine("   Hypothesis: ${exp.hypothesis}")
                                        appendLine("   Samples Recorded: ${pts.size}")
                                        appendLine("   Lab Notes: ${exp.labNotes.take(120)}...")
                                        appendLine("------------------------------------")
                                    }
                                }
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, summary)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Export All Science Lab Records"))
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = SciCyan, contentColor = Color(0xFF0A192F)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("dismiss_guide_button")
                ) {
                    Text("Got It! Let's Experiment", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ThreeStepGuideTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GuideCard(
            stepNumber = "1",
            title = "Pick or Create an Experiment",
            description = "Tap any active experiment card on the home screen, or tap '+' to start a new one using 1-tap science presets (Pendulum, Chemistry, Photosynthesis).",
            icon = Icons.Default.Science,
            accentColor = SciCyan
        )

        GuideCard(
            stepNumber = "2",
            title = "Watch Live Telemetry Stream",
            description = "Tap 'Live Auto-Logger' or '+ Add Data Point'. The interactive canvas graph plots live sensor readings instantly in real time with zoom and touch tooltips!",
            icon = Icons.Default.ShowChart,
            accentColor = SciEmerald
        )

        GuideCard(
            stepNumber = "3",
            title = "Collaborate, Chat & AI Analysis",
            description = "Use the tabs inside any experiment to track Milestones, write collaborative Lab Notes, chat in real-time with teammates, and see AI calculate R² and trends.",
            icon = Icons.Default.AutoAwesome,
            accentColor = SciAmber
        )

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WifiOff, contentDescription = null, tint = SciAmber, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Working outdoors without internet? Offline Field Mode keeps logging data on-device and auto-syncs when you reconnect!",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun StudentTeacherRolesTab(currentRole: UserRole) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Role-Based Access Control (RBAC)",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Student Card
        Surface(
            color = if (currentRole == UserRole.STUDENT) SciCyan.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (currentRole == UserRole.STUDENT) SciCyan else Color.Transparent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.School, contentDescription = null, tint = SciCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Student Access", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SciCyan)
                    if (currentRole == UserRole.STUDENT) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(color = SciCyan, shape = RoundedCornerShape(4.dp)) {
                            Text("CURRENT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0A192F), modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                    }
                }
                Text("• Record live sensor telemetry & plot real-time graphs\n• Complete project milestones & drag tasks\n• Collaborate via group chat & shared notes\n• Play science knowledge quizzes & test skills\n• Export personal lab data to CSV / JSON / Text", fontSize = 11.sp)
            }
        }

        // Teacher Card
        Surface(
            color = if (currentRole == UserRole.TEACHER) SciAmber.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (currentRole == UserRole.TEACHER) SciAmber else Color.Transparent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = SciAmber, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Teacher / Faculty Access (Elevated)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SciAmber)
                    if (currentRole == UserRole.TEACHER) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(color = SciAmber, shape = RoundedCornerShape(4.dp)) {
                            Text("CURRENT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF332B00), modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                    }
                }
                Text("• Everything in Student, plus:\n• Teacher Portal to review all student lab cohorts\n• 1-Click Official Grading (Grade A, B, C) & Custom Rubrics\n• Endorse AI statistical telemetry evaluations\n• Class-wide progress and mastery tracking", fontSize = 11.sp)
            }
        }

        // Quick Role Switch Button
        OutlinedButton(
            onClick = {
                val nextRole = if (currentRole == UserRole.STUDENT) UserRole.TEACHER else UserRole.STUDENT
                SciLabRepository.switchUserRole(nextRole)
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("guide_switch_role_button")
        ) {
            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (currentRole == UserRole.STUDENT) "Switch to Teacher Role Now" else "Switch to Student Role Now",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun DataStorageAndAccessTab(onExportAll: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Where We Save Student & Teacher Data",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Surface(
            color = SciEmerald.copy(alpha = 0.12f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, SciEmerald.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = SciEmerald, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("1. Device-Local Encrypted Storage", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SciEmerald)
                }
                Text(
                    text = "All experiments, telemetry samples, notes, and chat logs are stored locally on your device in reactive repository storage secured with AES-256 encryption. This ensures lightning-fast performance and full offline availability.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Surface(
            color = SciCyan.copy(alpha = 0.12f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, SciCyan.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = SciCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("2. Cloud Storage & Team Sync", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SciCyan)
                }
                Text(
                    text = "When connected to the network, your experiments sync automatically. You can also tap the Cloud Sync icon at any time to immediately reconcile pending changes across team members.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = SciAmber, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("3. How to Access & Export Your Data", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SciAmber)
                }
                Text(
                    text = "You own 100% of your experimental findings. Inside each experiment, tap the Share icon to export formatted CSV spreadsheets or JSON logs. You can also tap below to export all records at once!",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Button(
            onClick = onExportAll,
            colors = ButtonDefaults.buttonColors(containerColor = SciCyan, contentColor = Color(0xFF0A192F)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("export_all_data_guide_button")
        ) {
            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Export & Share All My Data Now", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun GuideCard(
    stepNumber: String,
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                color = accentColor.copy(alpha = 0.2f),
                shape = CircleShape,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(stepNumber, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = accentColor)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
