package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AuthActionType
import com.example.data.AuthDataRecord
import com.example.data.AuthDataSheetManager
import com.example.model.UserRole
import com.example.ui.theme.SciAmber
import com.example.ui.theme.SciCyan
import com.example.ui.theme.SciEmerald

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDataSheetScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val records by AuthDataSheetManager.records.collectAsState()
    val masterPin by AuthDataSheetManager.masterPin.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedActionFilter by remember { mutableStateOf<AuthActionType?>(null) }
    var selectedRoleFilter by remember { mutableStateOf<UserRole?>(null) }

    var selectedRecordForDetail by remember { mutableStateOf<AuthDataRecord?>(null) }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showRawCsvDialog by remember { mutableStateOf(false) }

    BackHandler { onBack() }

    // Filter records
    val filteredRecords = remember(records, searchQuery, selectedActionFilter, selectedRoleFilter) {
        records.filter { rec ->
            val matchesQuery = searchQuery.isBlank() ||
                    rec.fullName.contains(searchQuery, ignoreCase = true) ||
                    rec.email.contains(searchQuery, ignoreCase = true) ||
                    rec.institution.contains(searchQuery, ignoreCase = true) ||
                    rec.recordId.contains(searchQuery, ignoreCase = true)

            val matchesAction = selectedActionFilter == null || rec.action == selectedActionFilter
            val matchesRole = selectedRoleFilter == null || rec.role == selectedRoleFilter

            matchesQuery && matchesAction && matchesRole
        }
    }

    val totalSignUps = remember(records) { records.count { it.action == AuthActionType.SIGN_UP } }
    val totalSignIns = remember(records) { records.count { it.action == AuthActionType.SIGN_IN } }
    val studentCount = remember(records) { records.count { it.role == UserRole.STUDENT } }
    val teacherCount = remember(records) { records.count { it.role == UserRole.TEACHER } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Owner Data Sheet", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = SciEmerald.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, SciEmerald.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    "CONFIDENTIAL",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SciEmerald,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            "Background Sign Up & Log In Records • Total: ${records.size}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_sheet_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Export to Google Sheets / Drive
                    IconButton(
                        onClick = {
                            try {
                                val intent = AuthDataSheetManager.createShareIntent(context)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error launching share: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.testTag("admin_sheet_export_action")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Export to Google Sheets or Drive", tint = SciCyan)
                    }

                    // Copy CSV
                    IconButton(
                        onClick = { AuthDataSheetManager.copyCsvToClipboard(context) },
                        modifier = Modifier.testTag("admin_sheet_copy_action")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy CSV to Clipboard")
                    }

                    // Security / PIN Settings
                    IconButton(onClick = { showChangePinDialog = true }) {
                        Icon(Icons.Default.Security, contentDescription = "Security Settings", tint = SciAmber)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val intent = AuthDataSheetManager.createShareIntent(context)
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SciEmerald, contentColor = Color(0xFF003915)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(44.dp)
                            .testTag("export_to_sheets_button")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export CSV / Open in Sheets", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showRawCsvDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(0.8f)
                            .height(44.dp)
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View CSV", fontSize = 12.sp)
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Summary KPI Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiStatCard(
                        title = "Sign Ups",
                        count = totalSignUps.toString(),
                        subtitle = "New accounts",
                        color = SciEmerald,
                        modifier = Modifier.weight(1f)
                    )
                    KpiStatCard(
                        title = "Sign Ins",
                        count = totalSignIns.toString(),
                        subtitle = "Active logins",
                        color = SciCyan,
                        modifier = Modifier.weight(1f)
                    )
                    KpiStatCard(
                        title = "Students",
                        count = studentCount.toString(),
                        subtitle = "Role",
                        color = Color(0xFF06B6D4),
                        modifier = Modifier.weight(1f)
                    )
                    KpiStatCard(
                        title = "Teachers",
                        count = teacherCount.toString(),
                        subtitle = "Role",
                        color = SciAmber,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Security Notice Banner
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = SciCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Private Internal Storage File (`auth_records_ledger.csv`)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                "Automatically recorded in the background during every login and signup. Only accessible via Owner PIN.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Search & Filter controls
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by name, email, or institution...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
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
                            .testTag("admin_sheet_search_input")
                    )

                    // Filter Chips Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Filter:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        FilterChip(
                            selected = selectedActionFilter == null,
                            onClick = { selectedActionFilter = null },
                            label = { Text("All Actions") }
                        )

                        FilterChip(
                            selected = selectedActionFilter == AuthActionType.SIGN_UP,
                            onClick = { selectedActionFilter = if (selectedActionFilter == AuthActionType.SIGN_UP) null else AuthActionType.SIGN_UP },
                            label = { Text("Sign Ups Only") },
                            leadingIcon = {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = SciEmerald, modifier = Modifier.size(14.dp))
                            }
                        )

                        FilterChip(
                            selected = selectedActionFilter == AuthActionType.SIGN_IN,
                            onClick = { selectedActionFilter = if (selectedActionFilter == AuthActionType.SIGN_IN) null else AuthActionType.SIGN_IN },
                            label = { Text("Sign Ins Only") },
                            leadingIcon = {
                                Icon(Icons.Default.Login, contentDescription = null, tint = SciCyan, modifier = Modifier.size(14.dp))
                            }
                        )

                        FilterChip(
                            selected = selectedRoleFilter == UserRole.STUDENT,
                            onClick = { selectedRoleFilter = if (selectedRoleFilter == UserRole.STUDENT) null else UserRole.STUDENT },
                            label = { Text("Students") }
                        )

                        FilterChip(
                            selected = selectedRoleFilter == UserRole.TEACHER,
                            onClick = { selectedRoleFilter = if (selectedRoleFilter == UserRole.TEACHER) null else UserRole.TEACHER },
                            label = { Text("Teachers") }
                        )
                    }
                }
            }

            // Spreadsheet Container
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Header Bar with Quick Table Tools
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TableChart, contentDescription = null, tint = SciCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Spreadsheet Data Table (${filteredRecords.size} rows)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                "Scroll horizontally to see all columns ➜",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Scrollable Spreadsheet View
                        val horizontalScrollState = rememberScrollState()

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(horizontalScrollState)
                        ) {
                            // Column Headers Row
                            Row(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TableHeaderCell("#", 60.dp)
                                TableHeaderCell("Time & Date", 150.dp)
                                TableHeaderCell("Action", 100.dp)
                                TableHeaderCell("User Name", 140.dp)
                                TableHeaderCell("Email Address", 200.dp)
                                TableHeaderCell("Role", 100.dp)
                                TableHeaderCell("Institution / Dept", 200.dp)
                                TableHeaderCell("Status", 90.dp)
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            if (filteredRecords.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .width(1040.dp)
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "No records found matching filters.",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                filteredRecords.forEachIndexed { index, record ->
                                    val isEven = index % 2 == 0
                                    Row(
                                        modifier = Modifier
                                            .background(
                                                if (isEven) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                            )
                                            .clickable { selectedRecordForDetail = record }
                                            .padding(vertical = 10.dp, horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // # ID
                                        Text(
                                            text = record.recordId,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.width(60.dp)
                                        )

                                        // Timestamp
                                        Text(
                                            text = record.formattedDate,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.width(150.dp)
                                        )

                                        // Action Pill
                                        Box(modifier = Modifier.width(100.dp)) {
                                            ActionBadge(record.action)
                                        }

                                        // Full Name
                                        Text(
                                            text = record.fullName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.width(140.dp)
                                        )

                                        // Email
                                        Text(
                                            text = record.email,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = SciCyan,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.width(200.dp)
                                        )

                                        // Role Badge
                                        Box(modifier = Modifier.width(100.dp)) {
                                            RoleBadge(record.role)
                                        }

                                        // Institution
                                        Text(
                                            text = record.institution,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.width(200.dp)
                                        )

                                        // Status
                                        Surface(
                                            color = SciEmerald.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.width(80.dp)
                                        ) {
                                            Text(
                                                text = record.status,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SciEmerald,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                                }
                            }
                        }
                    }
                }
            }

            // Danger Zone / Clear Options
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { showClearConfirmDialog = true }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear Data Sheet", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }

                    TextButton(onClick = { showChangePinDialog = true }) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = SciCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Change Master PIN", color = SciCyan, fontSize = 12.sp)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Detail Dialog
    if (selectedRecordForDetail != null) {
        val rec = selectedRecordForDetail!!
        Dialog(onDismissRequest = { selectedRecordForDetail = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, SciCyan.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Record ${rec.recordId}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        ActionBadge(rec.action)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    DetailItem("Timestamp", rec.formattedDate)
                    DetailItem("Full Name", rec.fullName)
                    DetailItem("Email Address", rec.email)
                    DetailItem("Role Assignment", if (rec.role == UserRole.STUDENT) "Student (Restricted Access)" else "Teacher (Elevated Access)")
                    DetailItem("Institution / Org", rec.institution)
                    DetailItem("Auth Status", rec.status)
                    DetailItem("Security Notes", rec.notes)

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { selectedRecordForDetail = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }

    // Raw CSV Dialog
    if (showRawCsvDialog) {
        val rawCsv = remember { AuthDataSheetManager.getRawCsvContent(context) }
        Dialog(onDismissRequest = { showRawCsvDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Raw CSV Data Sheet", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        LazyColumn(modifier = Modifier.padding(12.dp)) {
                            item {
                                Text(
                                    text = rawCsv,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color(0xFFF1F5F9)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { AuthDataSheetManager.copyCsvToClipboard(context) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy CSV")
                        }
                        Button(
                            onClick = { showRawCsvDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Done")
                        }
                    }
                }
            }
        }
    }

    // Change PIN Dialog
    if (showChangePinDialog) {
        var newPin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf<String?>(null) }

        Dialog(onDismissRequest = { showChangePinDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Change Owner Master PIN", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Current PIN: $masterPin", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { if (it.length <= 10) newPin = it },
                        label = { Text("New PIN (min 4 digits)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { if (it.length <= 10) confirmPin = it },
                        label = { Text("Confirm New PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth()
                    )

                    AnimatedVisibility(visible = pinError != null) {
                        Text(pinError ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = { showChangePinDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (newPin.length < 4) {
                                    pinError = "PIN must be at least 4 digits"
                                } else if (newPin != confirmPin) {
                                    pinError = "PINs do not match"
                                } else {
                                    AuthDataSheetManager.updateMasterPin(context, newPin)
                                    Toast.makeText(context, "Master PIN updated successfully!", Toast.LENGTH_SHORT).show()
                                    showChangePinDialog = false
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }

    // Clear Confirm Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear All Auth Records?") },
            text = { Text("This will erase all recorded login and signup rows from the background CSV data sheet. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        AuthDataSheetManager.clearAllRecords(context)
                        showClearConfirmDialog = false
                        Toast.makeText(context, "Data sheet cleared.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TableHeaderCell(title: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.width(width)
    )
}

@Composable
private fun ActionBadge(action: AuthActionType) {
    val isSignUp = action == AuthActionType.SIGN_UP
    Surface(
        color = if (isSignUp) SciEmerald.copy(alpha = 0.18f) else SciCyan.copy(alpha = 0.18f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, if (isSignUp) SciEmerald.copy(alpha = 0.5f) else SciCyan.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isSignUp) Icons.Default.PersonAdd else Icons.Default.Login,
                contentDescription = null,
                tint = if (isSignUp) SciEmerald else SciCyan,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = action.displayName,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSignUp) SciEmerald else SciCyan
            )
        }
    }
}

@Composable
private fun RoleBadge(role: UserRole) {
    val isStudent = role == UserRole.STUDENT
    Surface(
        color = if (isStudent) Color(0xFF0EA5E9).copy(alpha = 0.15f) else SciAmber.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, if (isStudent) Color(0xFF0EA5E9).copy(alpha = 0.4f) else SciAmber.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isStudent) Icons.Default.School else Icons.Default.VerifiedUser,
                contentDescription = null,
                tint = if (isStudent) Color(0xFF0EA5E9) else SciAmber,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isStudent) "Student" else "Teacher",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isStudent) Color(0xFF0EA5E9) else SciAmber
            )
        }
    }
}

@Composable
private fun KpiStatCard(
    title: String,
    count: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column {
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}
