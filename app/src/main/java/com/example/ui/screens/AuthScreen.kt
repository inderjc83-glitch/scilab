package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SciLabRepository
import com.example.model.UserRole
import com.example.ui.theme.SciAmber
import com.example.ui.theme.SciCyan
import com.example.ui.theme.SciEmerald

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    var nameInput by remember { mutableStateOf("Alex Rivera") }
    var emailInput by remember { mutableStateOf("alex.rivera@science.edu") }
    var institutionInput by remember { mutableStateOf("Department of Natural Sciences") }
    var showCustomInputs by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // App Emblem & Welcome
            Surface(
                color = SciCyan.copy(alpha = 0.15f),
                shape = CircleShape,
                border = BorderStroke(2.dp, SciCyan),
                modifier = Modifier.size(76.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Science,
                        contentDescription = "SciLab Logo",
                        tint = SciCyan,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Welcome to SciLab",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Easy, real-time science experiments & team collaboration",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Friendly Getting Started Prompt
            Surface(
                color = SciCyan.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SciCyan.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.EmojiObjects, contentDescription = null, tint = SciCyan, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Choose your role below for instant 1-tap entry. No passwords needed!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // STUDENT CARD
            val isStudent = selectedRole == UserRole.STUDENT
            Surface(
                color = if (isStudent) SciCyan.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = if (isStudent) 2.dp else 1.dp,
                    color = if (isStudent) SciCyan else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedRole = UserRole.STUDENT
                        nameInput = "Alex Rivera"
                        emailInput = "alex.rivera@science.edu"
                    }
                    .testTag("role_card_student")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = SciCyan.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.School, contentDescription = null, tint = SciCyan, modifier = Modifier.size(24.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("I am a Student", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Record labs, watch live charts & play quizzes", fontSize = 11.sp, color = SciCyan)
                            }
                        }

                        RadioButton(
                            selected = isStudent,
                            onClick = {
                                selectedRole = UserRole.STUDENT
                                nameInput = "Alex Rivera"
                                emailInput = "alex.rivera@science.edu"
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = SciCyan)
                        )
                    }

                    Text(
                        text = "Students can record live sensor telemetry, create interactive graphs, complete milestones, chat with peers, and practice trivia.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        QuickFeatureTag("📈 Live Graphs", SciCyan)
                        QuickFeatureTag("📶 Offline Mode", SciCyan)
                        QuickFeatureTag("🧠 Science Quizzes", SciCyan)
                    }

                    Button(
                        onClick = {
                            val nameToUse = if (showCustomInputs && nameInput.isNotBlank()) nameInput.trim() else "Alex Rivera"
                            val emailToUse = if (showCustomInputs && emailInput.isNotBlank()) emailInput.trim() else "alex.rivera@science.edu"
                            SciLabRepository.login(
                                name = nameToUse,
                                email = emailToUse,
                                role = UserRole.STUDENT
                            )
                            onLoginSuccess()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SciCyan, contentColor = Color(0xFF0A192F)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("enter_as_student_button")
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (showCustomInputs) "Start as Student ($nameInput)" else "1-Tap Start as Student (Alex Rivera)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // TEACHER CARD
            val isTeacher = selectedRole == UserRole.TEACHER
            Surface(
                color = if (isTeacher) SciAmber.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = if (isTeacher) 2.dp else 1.dp,
                    color = if (isTeacher) SciAmber else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedRole = UserRole.TEACHER
                        nameInput = "Dr. Marcus Vance"
                        emailInput = "marcus.vance@faculty.edu"
                    }
                    .testTag("role_card_teacher")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = SciAmber.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = SciAmber, modifier = Modifier.size(24.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("I am a Teacher / Faculty", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Official grading, rubrics & AI analytics", fontSize = 11.sp, color = SciAmber)
                            }
                        }

                        RadioButton(
                            selected = isTeacher,
                            onClick = {
                                selectedRole = UserRole.TEACHER
                                nameInput = "Dr. Marcus Vance"
                                emailInput = "marcus.vance@faculty.edu"
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = SciAmber)
                        )
                    }

                    Text(
                        text = "Teachers have elevated privileges to review student submissions, endorse automated AI telemetry statistics, assign grades, and oversee research cohorts.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        QuickFeatureTag("📝 Official Grading", SciAmber)
                        QuickFeatureTag("🤖 AI Telemetry", SciAmber)
                        QuickFeatureTag("📊 Cohort Insights", SciAmber)
                    }

                    Button(
                        onClick = {
                            val nameToUse = if (showCustomInputs && nameInput.isNotBlank()) nameInput.trim() else "Dr. Marcus Vance"
                            val emailToUse = if (showCustomInputs && emailInput.isNotBlank()) emailInput.trim() else "marcus.vance@faculty.edu"
                            SciLabRepository.login(
                                name = nameToUse,
                                email = emailToUse,
                                role = UserRole.TEACHER
                            )
                            onLoginSuccess()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SciAmber, contentColor = Color(0xFF332B00)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("enter_as_teacher_button")
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (showCustomInputs) "Start as Teacher ($nameInput)" else "1-Tap Start as Teacher (Dr. Marcus)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Optional Customize Profile Section
            TextButton(
                onClick = { showCustomInputs = !showCustomInputs },
                modifier = Modifier.testTag("toggle_custom_credentials")
            ) {
                Icon(
                    if (showCustomInputs) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (showCustomInputs) "Hide Custom Details" else "Want to enter your own name or school? Tap here",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = showCustomInputs) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Personalize Profile", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Your Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SciCyan) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SciCyan) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = institutionInput,
                            onValueChange = { institutionInput = it },
                            label = { Text("School / Department") },
                            leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = SciCyan) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun QuickFeatureTag(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
