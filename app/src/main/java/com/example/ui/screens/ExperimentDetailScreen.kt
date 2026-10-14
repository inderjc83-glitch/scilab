package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.analytics.ScienceFeedbackEngine
import com.example.data.SciLabRepository
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.SciAmber
import com.example.ui.theme.SciCyan
import com.example.ui.theme.SciEmerald
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperimentDetailScreen(
    experimentId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val experiments by SciLabRepository.experiments.collectAsState()
    val dataPointsMap by SciLabRepository.dataPoints.collectAsState()
    val chatMessagesMap by SciLabRepository.chatMessages.collectAsState()
    val projectTasksMap by SciLabRepository.projectTasks.collectAsState()
    val isLiveStreaming by SciLabRepository.isLiveStreaming.collectAsState()
    val userProfile by SciLabRepository.userProfile.collectAsState()

    val experiment = experiments.find { it.id == experimentId } ?: experiments.firstOrNull()
    val dataPoints = if (experiment != null) dataPointsMap[experiment.id] ?: emptyList() else emptyList()
    val chatMessages = if (experiment != null) chatMessagesMap[experiment.id] ?: emptyList() else emptyList()
    val tasks = if (experiment != null) projectTasksMap[experiment.id] ?: emptyList() else emptyList()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showAddPointDialog by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    // Manual Data Point Dialog State
    var inputParamName by remember { mutableStateOf(dataPoints.firstOrNull()?.paramName ?: "Temperature (°C)") }
    var inputValue by remember { mutableStateOf("") }
    var inputUnit by remember { mutableStateOf(dataPoints.firstOrNull()?.unit ?: "°C") }
    var inputNotes by remember { mutableStateOf("") }

    // Chat input
    var chatInputText by remember { mutableStateOf("") }

    // Lab notes edit state
    var labNotesText by remember(experiment?.id) { mutableStateOf(experiment?.labNotes ?: "") }

    if (experiment == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Experiment not found.")
        }
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = experiment.title,
                            maxLines = 1,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${experiment.category.displayName} • ${experiment.collaborators.joinToString(", ")}",
                            maxLines = 1,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSecurityDialog = true }) {
                        Icon(Icons.Default.Shield, contentDescription = "Security Audit", tint = SciEmerald)
                    }
                    IconButton(onClick = { showExportDialog = true }, modifier = Modifier.testTag("export_data_button")) {
                        Icon(Icons.Default.Share, contentDescription = "Export Findings", tint = SciCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Workspace Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = SciCyan
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("📈 Telemetry (${dataPoints.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("📋 Milestones (${tasks.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("📝 Lab Notes", fontSize = 12.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("💬 Chat (${chatMessages.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    text = { Text("🤖 AI Analysis", fontSize = 12.sp, fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Telemetry Graph & Data Table
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))

                            // Interactive Live Graph
                            val activeParam = dataPoints.lastOrNull()?.paramName ?: "Primary Telemetry"
                            val activeUnit = dataPoints.lastOrNull()?.unit ?: "units"

                            SciInteractiveGraph(
                                dataPoints = dataPoints,
                                paramName = activeParam,
                                unit = activeUnit,
                                isStreaming = isLiveStreaming
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action buttons: Live Auto Logger, 1-Tap Quick Sample, and Custom Point
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        SciLabRepository.toggleLiveStreaming(
                                            experimentId = experiment.id,
                                            paramName = activeParam,
                                            unit = activeUnit
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isLiveStreaming) SciAmber else SciEmerald,
                                        contentColor = Color(0xFF0A192F)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(42.dp)
                                        .testTag("toggle_live_sampler_button")
                                ) {
                                    Icon(
                                        if (isLiveStreaming) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isLiveStreaming) "Pause Stream" else "Live Auto-Logger", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                FilledTonalButton(
                                    onClick = {
                                        val pt = SciLabRepository.quickAddSample(experiment.id)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Sample logged: ${String.format("%.2f", pt.value)} ${pt.unit}!")
                                        }
                                    },
                                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = SciCyan.copy(alpha = 0.2f), contentColor = SciCyan),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .weight(1.1f)
                                        .height(42.dp)
                                        .testTag("quick_add_sample_detail_button")
                                ) {
                                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+1 Sample", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { showAddPointDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .weight(0.9f)
                                        .height(42.dp)
                                        .testTag("add_data_point_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Custom", fontSize = 12.sp)
                                }
                            }
                        }

                        // Data Table Header with Quick Export
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Data Logs (${dataPoints.size} samples)",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Checksum: ${experiment.encryptionChecksum.take(16)}...",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = SciEmerald
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    TextButton(
                                        onClick = { showExportDialog = true },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(Icons.Default.FileDownload, contentDescription = null, tint = SciCyan, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Export CSV", fontSize = 11.sp, color = SciCyan)
                                    }

                                    if (dataPoints.isNotEmpty()) {
                                        TextButton(
                                            onClick = { showClearConfirmDialog = true },
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("Clear", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }

                        if (dataPoints.isEmpty()) {
                            item {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.Default.ShowChart, contentDescription = null, tint = SciCyan, modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("No telemetry points logged yet.", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                        Text("Tap '+1 Sample' or 'Live Auto-Logger' above to start!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        } else {
                            items(dataPoints.reversed()) { pt ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${pt.paramName}: ${String.format("%.3f", pt.value)} ${pt.unit}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (pt.notes.isNotEmpty()) {
                                                Text(
                                                    text = pt.notes,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Surface(
                                            color = SciCyan.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "t = ${pt.timeOffsetSec.toInt()}s",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SciCyan,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                }

                1 -> {
                    // Project Milestones Timeline with Progress Indicator
                    val verifiedCount = tasks.count { it.status == TaskStatus.VERIFIED }
                    val progressFraction = if (tasks.isNotEmpty()) verifiedCount.toFloat() / tasks.size else 0f

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Progress Summary Card
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Cohort Milestones", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        "$verifiedCount of ${tasks.size} Verified (${(progressFraction * 100).toInt()}%)",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = SciEmerald
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { progressFraction },
                                    color = SciEmerald,
                                    trackColor = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                )
                            }
                        }

                        DragAndDropTaskBoard(
                            tasks = tasks,
                            onStatusChange = { taskId, next ->
                                SciLabRepository.updateTaskStatus(experiment.id, taskId, next)
                            },
                            onAddTask = { title, assignee, due ->
                                SciLabRepository.addTask(experiment.id, title, assignee, due)
                            }
                        )
                    }
                }

                2 -> {
                    // Lab Notes Collaborative Editor with Quick Template Chips
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Collaborative Research Notes", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("AES-256 encrypted • Cloud synced", fontSize = 11.sp, color = SciEmerald)
                            }
                            Button(
                                onClick = {
                                    SciLabRepository.updateLabNotes(experiment.id, labNotesText)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Research notes saved & encrypted!")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SciCyan, contentColor = Color(0xFF0A192F)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("save_notes_button")
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save Notes")
                            }
                        }

                        // 1-Tap Template Insert Chips
                        Text("Insert Section Templates:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                TemplateInsertChip("+ Hypothesis") {
                                    labNotesText += "\n\n### Hypothesis\nWe predict that the dependent variable will change proportionally to the independent variable under standard conditions."
                                }
                            }
                            item {
                                TemplateInsertChip("+ Apparatus") {
                                    labNotesText += "\n\n### Apparatus & Materials\n- Digital Sensor Probe\n- Calibrated photogate laser\n- Optical stand & stopcock"
                                }
                            }
                            item {
                                TemplateInsertChip("+ Procedure") {
                                    labNotesText += "\n\n### Procedure\n1. Zero all digital instruments.\n2. Record baseline reading at t=0s.\n3. Sample every 10 seconds across 8 trials."
                                }
                            }
                            item {
                                TemplateInsertChip("+ Conclusion") {
                                    labNotesText += "\n\n### Conclusion\nThe empirical data strongly supports our hypothesis within acceptable 95% confidence intervals."
                                }
                            }
                        }

                        OutlinedTextField(
                            value = labNotesText,
                            onValueChange = { labNotesText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .testTag("lab_notes_editor"),
                            placeholder = { Text("Record apparatus settings, observations, reagent stoichiometry...") }
                        )
                    }
                }

                3 -> {
                    // Group Peer Chat with 1-Tap Quick Responses
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(chatMessages) { msg ->
                                val isMe = msg.senderName == userProfile.name
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                                ) {
                                    Surface(
                                        color = if (isMe) SciCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.widthIn(max = 280.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = msg.senderName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = if (msg.senderRole == UserRole.TEACHER) SciAmber else SciCyan
                                                )
                                                if (msg.senderRole == UserRole.TEACHER) {
                                                    Surface(color = SciAmber.copy(alpha = 0.3f), shape = RoundedCornerShape(4.dp)) {
                                                        Text("Teacher", fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(msg.messageText, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }
                            }
                        }

                        // Quick Reply Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            item {
                                QuickChatChip("Data looking steady! 📈") {
                                    SciLabRepository.sendChatMessage(experiment.id, "Data looking steady! 📈")
                                }
                            }
                            item {
                                QuickChatChip("Sensor calibrated! ✅") {
                                    SciLabRepository.sendChatMessage(experiment.id, "Sensor calibrated! ✅")
                                }
                            }
                            item {
                                QuickChatChip("R² is looking high! 🎯") {
                                    SciLabRepository.sendChatMessage(experiment.id, "R² is looking high! 🎯")
                                }
                            }
                            item {
                                QuickChatChip("Ready for teacher review! 📝") {
                                    SciLabRepository.sendChatMessage(experiment.id, "Ready for teacher review! 📝")
                                }
                            }
                        }

                        // Send Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = chatInputText,
                                onValueChange = { chatInputText = it },
                                placeholder = { Text("Message research group...") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chat_input_field"),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (chatInputText.isNotBlank()) {
                                        SciLabRepository.sendChatMessage(experiment.id, chatInputText)
                                        chatInputText = ""
                                    }
                                },
                                modifier = Modifier
                                    .background(SciCyan, CircleShape)
                                    .testTag("send_chat_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color(0xFF0A192F))
                            }
                        }
                    }
                }

                4 -> {
                    // AI Automated Analysis & Teacher Notes
                    val feedbackResult = remember(dataPoints) {
                        ScienceFeedbackEngine.analyzeExperimentData(dataPoints, experiment.hypothesis)
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SciCyan, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Automated Statistical Telemetry Analysis", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Correlation (R²):", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(String.format("%.3f", feedbackResult.rSquared), fontWeight = FontWeight.Bold, color = SciEmerald)
                                    }

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Mean Telemetry:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(String.format("%.2f", feedbackResult.meanValue), fontWeight = FontWeight.Bold)
                                    }

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Sample Variance:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(String.format("%.3f", feedbackResult.variance), fontWeight = FontWeight.Bold)
                                    }

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Trend Classification:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(feedbackResult.trendDescription, fontSize = 12.sp, color = SciAmber, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }

                        item {
                            Surface(
                                color = SciEmerald.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Scientific Verdict:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SciEmerald)
                                    Text(feedbackResult.scientificVerdict, fontSize = 13.sp)
                                }
                            }
                        }

                        item {
                            Text("Recommendations & Calibration Notes:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        items(feedbackResult.recommendations) { rec ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(10.dp)) {
                                    Text("•", color = SciCyan, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(rec, fontSize = 12.sp)
                                }
                            }
                        }

                        item {
                            // Copy to Notes action button
                            OutlinedButton(
                                onClick = {
                                    val summaryToAdd = "\n\n### Automated Telemetry Analysis\n- Verdict: ${feedbackResult.scientificVerdict}\n- R²: ${String.format("%.3f", feedbackResult.rSquared)}\n- Mean: ${String.format("%.2f", feedbackResult.meanValue)}"
                                    labNotesText += summaryToAdd
                                    SciLabRepository.updateLabNotes(experiment.id, labNotesText)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("AI Analysis appended to Lab Notes!")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Append AI Analysis into Lab Notes", fontSize = 12.sp)
                            }
                        }

                        if (experiment.teacherNotes != null) {
                            item {
                                Surface(
                                    color = SciAmber.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Instructor Feedback Notes:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SciAmber)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(experiment.teacherNotes!!, fontSize = 12.sp)
                                        if (experiment.teacherGrade != null) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Final Grade: ${experiment.teacherGrade}", fontWeight = FontWeight.Bold, color = SciEmerald)
                                        }
                                    }
                                }
                            }
                        }

                        // Role-specific evaluation action
                        item {
                            if (userProfile.role == com.example.model.UserRole.TEACHER || userProfile.role == com.example.model.UserRole.ADMIN) {
                                Button(
                                    onClick = {
                                        SciLabRepository.applyAutomatedFeedback(
                                            experiment.id,
                                            "${feedbackResult.scientificVerdict} (R² = ${String.format("%.3f", feedbackResult.rSquared)})"
                                        )
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Telemetry verdict endorsed & applied!")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SciEmerald, contentColor = Color(0xFF003915)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("apply_telemetry_verdict_button")
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Teacher Action: Endorse & Apply Telemetry Verdict", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = SciCyan, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Student Access: Official evaluations and rubric grading can only be modified by faculty accounts.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Point Dialog
    if (showAddPointDialog) {
        AlertDialog(
            onDismissRequest = { showAddPointDialog = false },
            title = { Text("Log Trial Data Point", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = inputParamName,
                        onValueChange = { inputParamName = it },
                        label = { Text("Parameter (e.g. Temperature)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { inputValue = it },
                        label = { Text("Numeric Value (e.g. 24.8)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("numeric_value_input")
                    )
                    OutlinedTextField(
                        value = inputUnit,
                        onValueChange = { inputUnit = it },
                        label = { Text("Unit (e.g. °C, kPa, m/s)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputNotes,
                        onValueChange = { inputNotes = it },
                        label = { Text("Trial Conditions / Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = inputValue.toFloatOrNull()
                        if (parsed != null && inputParamName.isNotBlank()) {
                            SciLabRepository.addDataPoint(
                                experimentId = experiment.id,
                                paramName = inputParamName.trim(),
                                value = parsed,
                                unit = inputUnit.trim(),
                                notes = inputNotes.trim()
                            )
                            inputValue = ""
                            inputNotes = ""
                            showAddPointDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SciCyan, contentColor = Color(0xFF0A192F)),
                    modifier = Modifier.testTag("save_data_point_confirm")
                ) {
                    Text("Add Sample")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPointDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear Confirmation Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear Telemetry Points?") },
            text = { Text("This will reset all sampled telemetry for this lab run so you can begin fresh observations.") },
            confirmButton = {
                Button(
                    onClick = {
                        SciLabRepository.clearDataPoints(experiment.id)
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset Points")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Export Dialog
    if (showExportDialog) {
        ExportShareDialog(
            experiment = experiment,
            dataPoints = dataPoints,
            onDismiss = { showExportDialog = false }
        )
    }

    // Security Dialog
    if (showSecurityDialog) {
        SecurityMfaDialog(
            isMfaEnabled = userProfile.isMfaEnabled,
            onToggleMfa = { SciLabRepository.toggleMfa(it) },
            onDismiss = { showSecurityDialog = false }
        )
    }
}

@Composable
private fun TemplateInsertChip(label: String, onClick: () -> Unit) {
    Surface(
        color = SciCyan.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, SciCyan.copy(alpha = 0.35f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = SciCyan,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun QuickChatChip(text: String, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}
