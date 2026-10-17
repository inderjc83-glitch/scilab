package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuthDataSheetManager
import com.example.data.SciLabRepository
import com.example.i18n.StringsLocalization
import com.example.model.PaymentTransaction
import com.example.model.UserRole
import com.example.ui.components.AdminPinDialog
import com.example.ui.components.HowToUseDialog
import com.example.ui.components.PaymentCheckoutDialog
import com.example.ui.components.ReceiptDialog
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SciAmber
import com.example.ui.theme.SciCyan

enum class AppNavDestination {
    DASHBOARD,
    TEACHER,
    QUIZ,
    PROFILE,
    EXPERIMENT_DETAIL,
    ADMIN_TRANSACTIONS,
    ADMIN_DATA_SHEET
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthDataSheetManager.init(this)
        SciLabRepository.initContext(this)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by SciLabRepository.isDarkMode.collectAsState()
            val isLoggedIn by SciLabRepository.isLoggedIn.collectAsState()

            MyApplicationTheme(darkTheme = isDarkMode) {
                if (!isLoggedIn) {
                    AuthScreen(
                        onLoginSuccess = { /* StateFlow updates and launches main interface */ }
                    )
                } else {
                    SciLabMainApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SciLabMainApp() {
    var currentScreen by remember { mutableStateOf(AppNavDestination.DASHBOARD) }
    var activeExperimentId by remember { mutableStateOf<String?>("exp_physics_01") }
    val userProfile by SciLabRepository.userProfile.collectAsState()
    val isDarkMode by SciLabRepository.isDarkMode.collectAsState()
    val currentLang by SciLabRepository.currentLanguage.collectAsState()

    var showUpgradeCheckout by remember { mutableStateOf(false) }
    var showHowToUseDialog by remember { mutableStateOf(false) }
    var showAdminPinDialog by remember { mutableStateOf(false) }
    var latestReceipt by remember { mutableStateOf<PaymentTransaction?>(null) }

    // System BackHandler
    BackHandler(enabled = currentScreen != AppNavDestination.DASHBOARD) {
        if (currentScreen == AppNavDestination.ADMIN_TRANSACTIONS || currentScreen == AppNavDestination.ADMIN_DATA_SHEET) {
            currentScreen = AppNavDestination.PROFILE
        } else {
            currentScreen = AppNavDestination.DASHBOARD
        }
    }

    Scaffold(
        topBar = {
            if (currentScreen != AppNavDestination.EXPERIMENT_DETAIL && 
                currentScreen != AppNavDestination.ADMIN_TRANSACTIONS && 
                currentScreen != AppNavDestination.ADMIN_DATA_SHEET) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Science,
                                contentDescription = null,
                                tint = SciCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = StringsLocalization.get("app_title", currentLang),
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp
                            )
                        }
                    },
                    actions = {
                        // Quick 1-Tap Role Toggle Pill (Student <-> Teacher)
                        Surface(
                            color = if (userProfile.role == UserRole.STUDENT) SciCyan.copy(alpha = 0.18f) else SciAmber.copy(alpha = 0.18f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (userProfile.role == UserRole.STUDENT) SciCyan.copy(alpha = 0.6f) else SciAmber.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .height(32.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        val next = if (userProfile.role == UserRole.STUDENT) UserRole.TEACHER else UserRole.STUDENT
                                        SciLabRepository.switchUserRole(next)
                                    }
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (userProfile.role == UserRole.STUDENT) Icons.Default.School else Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = if (userProfile.role == UserRole.STUDENT) SciCyan else SciAmber,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (userProfile.role == UserRole.STUDENT) "Student" else "Teacher",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (userProfile.role == UserRole.STUDENT) SciCyan else SciAmber
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    Icons.Default.SwapHoriz,
                                    contentDescription = "Tap to toggle role",
                                    tint = if (userProfile.role == UserRole.STUDENT) SciCyan else SciAmber,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        // Help / Guide Button
                        IconButton(
                            onClick = { showHowToUseDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("topbar_guide_button")
                        ) {
                            Icon(
                                Icons.Default.HelpOutline,
                                contentDescription = "How to use",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Quick Dark / Light Mode Toggle
                        IconButton(
                            onClick = { SciLabRepository.toggleDarkMode() },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("topbar_theme_button")
                        ) {
                            Icon(
                                if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle theme",
                                tint = if (isDarkMode) SciAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Logout Button (returns to Sign Up / Login screen)
                        IconButton(
                            onClick = { SciLabRepository.logout() },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("topbar_logout_button")
                        ) {
                            Icon(
                                Icons.Default.Logout,
                                contentDescription = "Log out",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Upgrade chip if not subscribed
                        if (!userProfile.isSubscribed) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                TextButton(
                                    onClick = { showUpgradeCheckout = true },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier
                                        .height(28.dp)
                                        .testTag("appbar_upgrade_button")
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = SciCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = StringsLocalization.get("upgrade_now", currentLang),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SciCyan
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        },
        bottomBar = {
            if (currentScreen != AppNavDestination.EXPERIMENT_DETAIL && 
                currentScreen != AppNavDestination.ADMIN_TRANSACTIONS && 
                currentScreen != AppNavDestination.ADMIN_DATA_SHEET) {
                NavigationBar(
                    windowInsets = WindowInsets.navigationBars,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppNavDestination.DASHBOARD,
                        onClick = { currentScreen = AppNavDestination.DASHBOARD },
                        icon = {
                            Icon(
                                if (currentScreen == AppNavDestination.DASHBOARD) Icons.Filled.Science else Icons.Outlined.Science,
                                contentDescription = "Experiments"
                            )
                        },
                        label = { Text(StringsLocalization.get("experiments", currentLang), fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_tab_dashboard")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppNavDestination.TEACHER,
                        onClick = { currentScreen = AppNavDestination.TEACHER },
                        icon = {
                            Icon(
                                if (currentScreen == AppNavDestination.TEACHER) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                                contentDescription = "Teacher Portal"
                            )
                        },
                        label = { Text(StringsLocalization.get("teacher_dashboard", currentLang), fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_tab_teacher")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppNavDestination.QUIZ,
                        onClick = { currentScreen = AppNavDestination.QUIZ },
                        icon = {
                            Icon(
                                if (currentScreen == AppNavDestination.QUIZ) Icons.Filled.Psychology else Icons.Outlined.Psychology,
                                contentDescription = "Science Quiz"
                            )
                        },
                        label = { Text(StringsLocalization.get("quizzes", currentLang), fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_tab_quiz")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppNavDestination.PROFILE,
                        onClick = { currentScreen = AppNavDestination.PROFILE },
                        icon = {
                            Icon(
                                if (currentScreen == AppNavDestination.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                                contentDescription = "Profile"
                            )
                        },
                        label = { Text(StringsLocalization.get("profile", currentLang), fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_tab_profile")
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentScreen) {
                AppNavDestination.DASHBOARD -> {
                    DashboardScreen(
                        onSelectExperiment = { expId ->
                            activeExperimentId = expId
                            currentScreen = AppNavDestination.EXPERIMENT_DETAIL
                        },
                        onOpenUpgrade = { showUpgradeCheckout = true }
                    )
                }
                AppNavDestination.EXPERIMENT_DETAIL -> {
                    ExperimentDetailScreen(
                        experimentId = activeExperimentId ?: "exp_physics_01",
                        onBack = { currentScreen = AppNavDestination.DASHBOARD }
                    )
                }
                AppNavDestination.TEACHER -> {
                    TeacherDashboardScreen(
                        onSelectExperiment = { expId ->
                            activeExperimentId = expId
                            currentScreen = AppNavDestination.EXPERIMENT_DETAIL
                        }
                    )
                }
                AppNavDestination.QUIZ -> {
                    QuizGameScreen()
                }
                AppNavDestination.PROFILE -> {
                    ProfileAndSecurityScreen(
                        onOpenAdminTransactions = { currentScreen = AppNavDestination.ADMIN_TRANSACTIONS },
                        onOpenAdminDataSheet = { showAdminPinDialog = true }
                    )
                }
                AppNavDestination.ADMIN_TRANSACTIONS -> {
                    AdminTransactionsScreen(
                        onBack = { currentScreen = AppNavDestination.PROFILE }
                    )
                }
                AppNavDestination.ADMIN_DATA_SHEET -> {
                    AdminDataSheetScreen(
                        onBack = { currentScreen = AppNavDestination.PROFILE }
                    )
                }
            }
        }
    }

    if (showUpgradeCheckout) {
        PaymentCheckoutDialog(
            userEmail = userProfile.email,
            onPaymentSuccess = { txn ->
                showUpgradeCheckout = false
                latestReceipt = txn
            },
            onDismiss = { showUpgradeCheckout = false }
        )
    }

    if (showHowToUseDialog) {
        HowToUseDialog(
            onDismiss = { showHowToUseDialog = false }
        )
    }

    if (showAdminPinDialog) {
        AdminPinDialog(
            onPinCorrect = {
                showAdminPinDialog = false
                currentScreen = AppNavDestination.ADMIN_DATA_SHEET
            },
            onDismiss = { showAdminPinDialog = false }
        )
    }

    if (latestReceipt != null) {
        ReceiptDialog(
            transaction = latestReceipt!!,
            onDismiss = { latestReceipt = null }
        )
    }
}
