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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SciLabRepository
import com.example.model.ExperimentStatus
import com.example.model.ScienceCategory
import com.example.model.ScienceExperiment
import com.example.ui.components.HowToUseDialog
import com.example.ui.theme.SciAmber
import com.example.ui.theme.SciCyan
import com.example.ui.theme.SciEmerald
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    onSelectExperiment: (String) -> Unit,
    onOpenUpgrade: () -> Unit,
    modifier: Modifier = Modifier
) {
    val experiments by SciLabRepository.experiments.collectAsState()
    val dataPointsMap by SciLabRepository.dataPoints.collectAsState()
    val userProfile by SciLabRepository.userProfile.collectAsState()
    val isOfflineMode by SciLabRepository.isOfflineFieldMode.collectAsState()
    val pendingSync by SciLabRepository.pendingSyncCount.collectAsState()
    val isSyncing by SciLabRepository.isCloudSyncing.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<ScienceCategory?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showExpirationDialog by remember { mutableStateOf(false) }
    var showHowToUseDialog by remember { mutableStateOf(false) }

    // Dialog state for new experiment
    var newTitle by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf(ScienceCategory.PHYSICS) }
    var newHypothesis by remember { mutableStateOf("") }
    var isNewFieldMode by remember { mutableStateOf(false) }

    val filteredExperiments = experiments.filter { exp ->
        val matchesQuery = exp.title.contains(searchQuery, ignoreCase = true) ||
                exp.hypothesis.contains(searchQuery, ignoreCase = true) ||
                exp.category.displayName.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategoryFilter == null || exp.category == selectedCategoryFilter
        matchesQuery && matchesCategory
    }

    val activeExp = experiments.firstOrNull()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = SciCyan,
                contentColor = Color(0xFF0A192F),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Lab", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("create_experiment_fab")
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))

                // Welcome & Quick Action Card
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, SciCyan.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Hello, ${userProfile.name.split(" ").firstOrNull() ?: "Scientist"}! 👋",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Real-time science telemetry & collaborative research",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                color = if (userProfile.role == com.example.model.UserRole.STUDENT) SciCyan.copy(alpha = 0.2f) else SciAmber.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.clickable {
                                    val next = if (userProfile.role == com.example.model.UserRole.STUDENT) com.example.model.UserRole.TEACHER else com.example.model.UserRole.STUDENT
                                    SciLabRepository.switchUserRole(next)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (userProfile.role == com.example.model.UserRole.STUDENT) Icons.Default.School else Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = if (userProfile.role == com.example.model.UserRole.STUDENT) SciCyan else SciAmber,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = userProfile.role.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (userProfile.role == com.example.model.UserRole.STUDENT) SciCyan else SciAmber
                                    )
                                }
                            }
                        }

                        // 1-Tap Quick Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Quick Open Active Lab
                            Button(
                                onClick = {
                                    if (activeExp != null) {
                                        onSelectExperiment(activeExp.id)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SciCyan, contentColor = Color(0xFF0A192F)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("quick_open_active_lab_button")
                            ) {
                                Icon(Icons.Default.ShowChart, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Live Lab", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // 1-Tap Quick Sample
                            FilledTonalButton(
                                onClick = {
                                    if (activeExp != null) {
                                        val sample = SciLabRepository.quickAddSample(activeExp.id)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Sample logged: ${String.format("%.2f", sample.value)} ${sample.unit}")
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("quick_add_sample_button")
                            ) {
                                Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = SciAmber, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+1 Sample", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // Guide & Help
                            OutlinedButton(
                                onClick = { showHowToUseDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("quick_open_guide_button")
                            ) {
                                Icon(Icons.Default.HelpOutline, contentDescription = null, tint = SciCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Guide", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Role Access Indicator
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (userProfile.role == com.example.model.UserRole.TEACHER) Icons.Default.School else Icons.Default.Person,
                                contentDescription = null,
                                tint = if (userProfile.role == com.example.model.UserRole.TEACHER) SciAmber else SciCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (userProfile.role) {
                                    com.example.model.UserRole.STUDENT -> "Student View: Telemetry & Quizzes"
                                    com.example.model.UserRole.TEACHER -> "Teacher View: Official Grading & Rubrics"
                                    com.example.model.UserRole.ADMIN -> "Admin View: System Logs & Settings"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            color = if (userProfile.role == com.example.model.UserRole.STUDENT) SciCyan.copy(alpha = 0.2f) else SciAmber.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (userProfile.role == com.example.model.UserRole.STUDENT) "STUDENT ACCESS" else "ELEVATED ACCESS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (userProfile.role == com.example.model.UserRole.STUDENT) SciCyan else SciAmber,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 2 Days Prior Subscription Expiration Notice Banner
                if (userProfile.subscriptionDaysRemaining <= 2) {
                    Surface(
                        color = SciAmber.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, SciAmber),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showExpirationDialog = true }
                            .testTag("subscription_expiring_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = SciAmber, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Subscription Completes in ${userProfile.subscriptionDaysRemaining} Days",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = SciAmber
                                )
                                Text(
                                    text = "Automated reminder email sent to ${userProfile.email}. Tap to inspect mail or renew.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Button(
                                onClick = onOpenUpgrade,
                                colors = ButtonDefaults.buttonColors(containerColor = SciAmber, contentColor = Color(0xFF332B00)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Renew", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Trial Period Alert Banner
                if (!userProfile.isSubscribed) {
                    Surface(
                        color = SciAmber.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SciAmber.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenUpgrade() }
                            .testTag("trial_banner_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = SciAmber, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "1-Week Trial Active: ${userProfile.trialDaysRemaining} days remaining",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Tap to unlock unlimited cloud storage, quizzes & payment integration.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = onOpenUpgrade,
                                colors = ButtonDefaults.buttonColors(containerColor = SciAmber, contentColor = Color(0xFF332B00)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Upgrade", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Cloud Sync & Field Mode Ribbon
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = if (isOfflineMode) SciAmber.copy(alpha = 0.2f) else SciEmerald.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (isOfflineMode) "OFFLINE FIELD MODE" else "CLOUD SYNC ACTIVE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOfflineMode) SciAmber else SciEmerald,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                            if (pendingSync > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$pendingSync pending",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = { SciLabRepository.syncWithCloud() },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .height(28.dp)
                                    .testTag("sync_cloud_button")
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = SciCyan, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Syncing...", fontSize = 11.sp)
                                } else {
                                    Icon(Icons.Default.CloudSync, contentDescription = "Sync Cloud", tint = SciCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sync Now", fontSize = 11.sp, color = SciCyan)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search experiments, hypotheses, telemetry...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = SciCyan) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("experiment_search_bar")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("All (${experiments.size})", fontSize = 11.sp) }
                    )
                    ScienceCategory.values().forEach { cat ->
                        val count = experiments.count { it.category == cat }
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = {
                                selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                            },
                            label = { Text("${cat.name.take(4)} ($count)", fontSize = 11.sp) }
                        )
                    }
                }
            }

            // Experiment Cards List
            if (filteredExperiments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Science, contentDescription = null, tint = SciCyan, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No experiments match your search criteria.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))
                            TextButton(onClick = { searchQuery = ""; selectedCategoryFilter = null }) {
                                Text("Reset Filters", color = SciCyan)
                            }
                        }
                    }
                }
            } else {
                items(filteredExperiments) { exp ->
                    val points = dataPointsMap[exp.id] ?: emptyList()
                    val lastSample = points.lastOrNull()
                    ExperimentCard(
                        experiment = exp,
                        dataPointsCount = points.size,
                        latestReading = if (lastSample != null) "${String.format("%.2f", lastSample.value)} ${lastSample.unit}" else null,
                        onClick = { onSelectExperiment(exp.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // New Experiment Creation Dialog with 1-Tap Presets
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Science, contentDescription = null, tint = SciCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("New Science Experiment", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // 1-Tap Ready Presets
                    Text("⚡ 1-Tap Ready Templates:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SciCyan)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            PresetChip("Pendulum Period", ScienceCategory.PHYSICS) {
                                newTitle = "Harmonic Oscillator & Gravitational Constant"
                                newCategory = ScienceCategory.PHYSICS
                                newHypothesis = "Oscillation period T squared is strictly proportional to length L, verifying g ≈ 9.81 m/s²."
                            }
                        }
                        item {
                            PresetChip("Catalase Kinetics", ScienceCategory.CHEMISTRY) {
                                newTitle = "Enzyme Catalase Decomposition Kinetics"
                                newCategory = ScienceCategory.CHEMISTRY
                                newHypothesis = "Reaction rate v increases with substrate saturation following Michaelis-Menten curve."
                            }
                        }
                        item {
                            PresetChip("Plant Photosynthesis", ScienceCategory.BIOLOGY) {
                                newTitle = "Aquatic Plant Photosynthetic DO Generation"
                                newCategory = ScienceCategory.BIOLOGY
                                newHypothesis = "Dissolved Oxygen production rises linearly with photon flux density up to saturation."
                            }
                        }
                        item {
                            PresetChip("Urban Heat Island", ScienceCategory.ENVIRONMENTAL) {
                                newTitle = "Urban Microclimate & Surface Temperature"
                                newCategory = ScienceCategory.ENVIRONMENTAL
                                newHypothesis = "Vegetation corridors exhibit 4.2°C lower ambient surface temperature versus asphalt."
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Experiment Title") },
                        placeholder = { Text("e.g. Acid-Base Neutralization Calorimetry") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_exp_title_input")
                    )

                    Text("Science Domain:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        ScienceCategory.values().forEach { cat ->
                            FilterChip(
                                selected = newCategory == cat,
                                onClick = { newCategory = cat },
                                label = { Text(cat.name.take(4), fontSize = 10.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = newHypothesis,
                        onValueChange = { newHypothesis = it },
                        label = { Text("Hypothesis / Research Question") },
                        placeholder = { Text("State your expected relationship...") },
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_exp_hypothesis_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Remote Field Research Mode", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("Stores locally with AES-256 for offline remote sync", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isNewFieldMode,
                            onCheckedChange = { isNewFieldMode = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val titleToUse = if (newTitle.isNotBlank()) newTitle.trim() else "Harmonic Motion Experiment"
                        val createdId = SciLabRepository.createExperiment(
                            title = titleToUse,
                            category = newCategory,
                            hypothesis = if (newHypothesis.isNotBlank()) newHypothesis.trim() else "Empirical telemetry measurement trial.",
                            isFieldMode = isNewFieldMode
                        )
                        newTitle = ""
                        newHypothesis = ""
                        showCreateDialog = false
                        onSelectExperiment(createdId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SciCyan, contentColor = Color(0xFF0A192F)),
                    modifier = Modifier.testTag("confirm_create_exp_button")
                ) {
                    Text("Start Lab Now", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showHowToUseDialog) {
        HowToUseDialog(onDismiss = { showHowToUseDialog = false })
    }

    if (showExpirationDialog) {
        com.example.ui.components.SubscriptionExpiryEmailDialog(
            userEmail = userProfile.email,
            daysRemaining = userProfile.subscriptionDaysRemaining,
            onRenewNow = onOpenUpgrade,
            onDismiss = { showExpirationDialog = false }
        )
    }
}

@Composable
private fun PresetChip(label: String, category: ScienceCategory, onClick: () -> Unit) {
    Surface(
        color = SciCyan.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, SciCyan.copy(alpha = 0.4f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Bolt, contentDescription = null, tint = SciCyan, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SciCyan)
        }
    }
}

@Composable
fun ExperimentCard(
    experiment: ScienceExperiment,
    dataPointsCount: Int,
    latestReading: String?,
    onClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("experiment_card_${experiment.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = SciCyan.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = experiment.category.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SciCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (experiment.isEncrypted) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "AES-256 Encrypted",
                            tint = SciEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Surface(
                        color = when (experiment.status) {
                            ExperimentStatus.ACTIVE -> SciCyan.copy(alpha = 0.15f)
                            ExperimentStatus.IN_REVIEW -> SciAmber.copy(alpha = 0.15f)
                            ExperimentStatus.COMPLETED -> SciEmerald.copy(alpha = 0.15f)
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = experiment.status.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (experiment.status) {
                                ExperimentStatus.ACTIVE -> SciCyan
                                ExperimentStatus.IN_REVIEW -> SciAmber
                                ExperimentStatus.COMPLETED -> SciEmerald
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = experiment.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Hypothesis: ${experiment.hypothesis}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ShowChart, contentDescription = null, tint = SciAmber, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$dataPointsCount samples",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (latestReading != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "($latestReading)",
                            fontSize = 11.sp,
                            color = SciCyan,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Icon(Icons.Default.Group, contentDescription = null, tint = SciCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${experiment.collaborators.size}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (experiment.teacherGrade != null) {
                    Surface(
                        color = SciEmerald.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Grade: ${experiment.teacherGrade}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SciEmerald,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Open Lab", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SciCyan)
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = SciCyan, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}
