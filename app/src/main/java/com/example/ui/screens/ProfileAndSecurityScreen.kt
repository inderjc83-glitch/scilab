package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.SciLabRepository
import com.example.model.AppLanguage
import com.example.model.PaymentTransaction
import com.example.model.UserRole
import com.example.ui.components.HowToUseDialog
import com.example.ui.components.PaymentCheckoutDialog
import com.example.ui.components.ReceiptDialog
import com.example.ui.components.SecurityMfaDialog
import com.example.ui.theme.SciAmber
import com.example.ui.theme.SciCyan
import com.example.ui.theme.SciEmerald

@Composable
fun ProfileAndSecurityScreen(
    onOpenAdminTransactions: () -> Unit,
    onOpenAdminDataSheet: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userProfile by SciLabRepository.userProfile.collectAsState()
    val isDarkMode by SciLabRepository.isDarkMode.collectAsState()
    val currentLang by SciLabRepository.currentLanguage.collectAsState()
    val isOfflineMode by SciLabRepository.isOfflineFieldMode.collectAsState()
    val experiments by SciLabRepository.experiments.collectAsState()
    val dataPointsMap by SciLabRepository.dataPoints.collectAsState()

    var showMfaDialog by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var activeReceipt by remember { mutableStateOf<PaymentTransaction?>(null) }
    var showLangDialog by remember { mutableStateOf(false) }
    var showExpiryNoticeDialog by remember { mutableStateOf(false) }
    var showHowToUseDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Profile Card
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = SciCyan.copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = SciCyan, modifier = Modifier.size(32.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userProfile.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = userProfile.email,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = when (userProfile.role) {
                                    UserRole.STUDENT -> SciCyan.copy(alpha = 0.2f)
                                    UserRole.TEACHER -> SciAmber.copy(alpha = 0.2f)
                                    UserRole.ADMIN -> SciEmerald.copy(alpha = 0.2f)
                                },
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Active Role: ${userProfile.role.name}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (userProfile.role) {
                                        UserRole.STUDENT -> SciCyan
                                        UserRole.TEACHER -> SciAmber
                                        UserRole.ADMIN -> SciEmerald
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Role Switcher for Testing / Evaluation
                    Text("1-Tap Role Switcher (RBAC):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        UserRole.values().forEach { role ->
                            FilterChip(
                                selected = userProfile.role == role,
                                onClick = { SciLabRepository.switchUserRole(role) },
                                label = { Text(role.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("role_switch_${role.name}")
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { SciLabRepository.logout() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sign_out_button")
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Switch Academic Account / Log Out", fontSize = 12.sp)
                    }
                }
            }
        }

        // Data Storage & Access Transparency Card (Answers: Where is data saved? How to access it?)
        item {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SciEmerald.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = SciEmerald, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Where Is Data Saved & How to Access It?", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Text(
                        text = "1. Student Data: Experiments, telemetry points, and lab notes are stored locally in encrypted on-device repository storage (AES-256) and auto-synced to cloud.\n\n2. Teacher Data: Assessments, cohort feedback, and rubrics are saved in the faculty ledger.\n\n3. How to Access: Tap below to export everything as a text/CSV report, or use the Share icon inside any experiment.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val report = buildString {
                                    appendLine("=== SCILAB FULL RESEARCH DATA EXPORT ===")
                                    appendLine("User: ${userProfile.name} (${userProfile.email}) - Role: ${userProfile.role}")
                                    appendLine("Experiments Total: ${experiments.size}")
                                    appendLine("=========================================\n")
                                    experiments.forEachIndexed { idx, exp ->
                                        val pts = dataPointsMap[exp.id] ?: emptyList()
                                        appendLine("${idx + 1}. [${exp.category.displayName}] ${exp.title}")
                                        appendLine("   Status: ${exp.status.label} | Grade: ${exp.teacherGrade ?: "Ungraded"}")
                                        appendLine("   Hypothesis: ${exp.hypothesis}")
                                        appendLine("   Telemetry Samples (${pts.size}):")
                                        pts.take(5).forEach { pt ->
                                            appendLine("     - t=${pt.timeOffsetSec}s: ${pt.paramName} = ${pt.value} ${pt.unit}")
                                        }
                                        if (pts.size > 5) appendLine("     ... (+${pts.size - 5} more samples)")
                                        appendLine("   Notes: ${exp.labNotes.take(150)}")
                                        appendLine("-----------------------------------------")
                                    }
                                }
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, report)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Export Complete Science Data"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SciCyan, contentColor = Color(0xFF0A192F)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(40.dp)
                                .testTag("export_all_profile_button")
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export All Data", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showHowToUseDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("open_storage_guide_button")
                        ) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, tint = SciCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Help Guide", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Subscription & Trial Section
        item {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CardMembership, contentDescription = null, tint = SciCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Subscription & Billing", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Surface(
                            color = if (userProfile.isSubscribed) SciEmerald.copy(alpha = 0.2f) else SciAmber.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (userProfile.isSubscribed) "ACTIVE: ${userProfile.activePlan}" else "TRIAL (${userProfile.trialDaysRemaining}d left)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (userProfile.isSubscribed) SciEmerald else SciAmber,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Text(
                        text = "Pricing Tiers: 1 Month = $1 • 6 Months = $5 • 1 Year = $9\nIncludes unlimited encrypted cloud storage, team collaboration, quiz arenas, and automated email receipts.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 2-Days Expiry Notice Banner
                    if (userProfile.subscriptionDaysRemaining <= 2) {
                        Surface(
                            color = SciAmber.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, SciAmber),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showExpiryNoticeDialog = true }
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = SciAmber, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Renewal Notice Sent (${userProfile.subscriptionDaysRemaining} Days Left)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SciAmber)
                                    }
                                    Text("View Mail", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SciCyan)
                                }
                                Text(
                                    text = "Automated email reminder dispatched to ${userProfile.email} 2 days prior to subscription completion.",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { showCheckoutDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SciEmerald, contentColor = Color(0xFF003915)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_subscription_plans_button")
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (userProfile.isSubscribed) "Manage / Renew Subscription" else "Subscribe Now ($1/mo • $5/6mo • $9/yr)", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onOpenAdminTransactions,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("view_transactions_button")
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = SciCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("View Billing & Transaction Records")
                    }

                    OutlinedButton(
                        onClick = onOpenAdminDataSheet,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, SciEmerald.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("view_auth_datasheet_button")
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, tint = SciEmerald, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Owner Auth Data Sheet (Sign Up & Log In Records)", color = SciEmerald, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Security & App Controls
        item {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Security & Global Settings", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    // MFA Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showMfaDialog = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = SciCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Multi-Factor Authentication", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(if (userProfile.isMfaEnabled) "Enabled (6-digit TOTP)" else "Disabled", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Field Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WifiOff, contentDescription = null, tint = SciAmber, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Offline Remote Field Mode", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Persist locally with AES-256 for remote research", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = isOfflineMode,
                            onCheckedChange = { SciLabRepository.toggleOfflineFieldMode() }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Dark Theme Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, tint = SciCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Dark / Light Theme", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(if (isDarkMode) "Deep navy science palette" else "Clean daylight palette", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { SciLabRepository.toggleDarkMode() }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Language Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLangDialog = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = SciEmerald, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Language / Idioma", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Current: ${currentLang.displayName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }
        }

        item {
            Button(
                onClick = { SciLabRepository.logout() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("logout_button")
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out & Return to Sign Up / Login Screen", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    if (showHowToUseDialog) {
        HowToUseDialog(onDismiss = { showHowToUseDialog = false })
    }

    if (showMfaDialog) {
        SecurityMfaDialog(
            isMfaEnabled = userProfile.isMfaEnabled,
            onToggleMfa = { SciLabRepository.toggleMfa(it) },
            onDismiss = { showMfaDialog = false }
        )
    }

    if (showCheckoutDialog) {
        PaymentCheckoutDialog(
            userEmail = userProfile.email,
            onPaymentSuccess = { txn ->
                showCheckoutDialog = false
                activeReceipt = txn
            },
            onDismiss = { showCheckoutDialog = false }
        )
    }

    if (activeReceipt != null) {
        ReceiptDialog(
            transaction = activeReceipt!!,
            onDismiss = { activeReceipt = null }
        )
    }

    if (showExpiryNoticeDialog) {
        com.example.ui.components.SubscriptionExpiryEmailDialog(
            userEmail = userProfile.email,
            daysRemaining = userProfile.subscriptionDaysRemaining,
            onRenewNow = {
                showExpiryNoticeDialog = false
                showCheckoutDialog = true
            },
            onDismiss = { showExpiryNoticeDialog = false }
        )
    }

    if (showLangDialog) {
        AlertDialog(
            onDismissRequest = { showLangDialog = false },
            title = { Text("Select App Language", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppLanguage.values().forEach { lang ->
                        Surface(
                            color = if (currentLang == lang) SciCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (currentLang == lang) SciCyan else MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    SciLabRepository.setLanguage(lang)
                                    showLangDialog = false
                                }
                        ) {
                            Text(
                                text = "${lang.flagEmoji} ${lang.displayName}",
                                fontSize = 14.sp,
                                fontWeight = if (currentLang == lang) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLangDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
