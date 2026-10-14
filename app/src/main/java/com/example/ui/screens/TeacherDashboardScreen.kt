package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.analytics.ScienceFeedbackEngine
import com.example.data.SciLabRepository
import com.example.model.ExperimentStatus
import com.example.model.ScienceExperiment
import com.example.model.UserRole
import com.example.ui.theme.SciAmber
import com.example.ui.theme.SciCyan
import com.example.ui.theme.SciEmerald

@Composable
fun TeacherDashboardScreen(
    onSelectExperiment: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val experiments by SciLabRepository.experiments.collectAsState()
    val dataPointsMap by SciLabRepository.dataPoints.collectAsState()
    val userProfile by SciLabRepository.userProfile.collectAsState()

    val isStudent = userProfile.role == UserRole.STUDENT

    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "PENDING", "GRADED"
    var reviewingExperiment by remember { mutableStateOf<ScienceExperiment?>(null) }
    var gradeInput by remember { mutableStateOf("95/100 (A)") }
    var notesInput by remember { mutableStateOf("Data demonstrates rigorous alignment with theoretical expectations. Good control of variables.") }
    var showReviewModal by remember { mutableStateOf(false) }

    val filteredList = remember(experiments, selectedFilter) {
        when (selectedFilter) {
            "PENDING" -> experiments.filter { it.teacherGrade == null }
            "GRADED" -> experiments.filter { it.teacherGrade != null }
            else -> experiments
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Header & Role Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isStudent) "Student Review Portal" else "Instructor & Faculty Dashboard",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isStudent)
                            "Inspect cohort submissions & read published teacher assessments"
                        else
                            "Real-time telemetry monitoring, 1-tap grading & rubric feedback",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 1-Tap Role Toggle
                Surface(
                    color = if (isStudent) SciCyan.copy(alpha = 0.2f) else SciAmber.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isStudent) SciCyan else SciAmber),
                    modifier = Modifier
                        .clickable {
                            val nextRole = if (isStudent) UserRole.TEACHER else UserRole.STUDENT
                            SciLabRepository.switchUserRole(nextRole)
                        }
                        .testTag("teacher_screen_toggle_role")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isStudent) Icons.Default.School else Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = if (isStudent) SciCyan else SciAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isStudent) "Switch to Teacher" else "Switch to Student",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isStudent) SciCyan else SciAmber
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Role Banner
            if (isStudent) {
                Surface(
                    color = SciCyan.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SciCyan.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = SciCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Student Limited Access Active", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SciCyan)
                        }
                        Text(
                            text = "Students can view telemetry logs and read teacher evaluations. Publishing official grades and editing rubrics are elevated features reserved for Teacher accounts.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                Surface(
                    color = SciAmber.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = SciAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Elevated Teacher Access: 1-Tap Official Grading Enabled", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SciAmber)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Overview Metric Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Active Cohorts", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${experiments.size} Groups", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = SciCyan)
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Reviewed Labs", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val gradedCount = experiments.count { it.teacherGrade != null }
                        Text("$gradedCount/${experiments.size} Graded", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = SciEmerald)
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Avg R² Fit", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("0.981", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = SciAmber)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    label = { Text("All Labs (${experiments.size})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedFilter == "PENDING",
                    onClick = { selectedFilter = "PENDING" },
                    label = { Text("Pending Review (${experiments.count { it.teacherGrade == null }})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedFilter == "GRADED",
                    onClick = { selectedFilter = "GRADED" },
                    label = { Text("Graded (${experiments.count { it.teacherGrade != null }})", fontSize = 11.sp) }
                )
            }
        }

        // Experiment Cohort Cards
        items(filteredList) { exp ->
            val points = dataPointsMap[exp.id] ?: emptyList()
            val feedback = remember(points) {
                ScienceFeedbackEngine.analyzeExperimentData(points, exp.hypothesis)
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("teacher_review_card_${exp.id}")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                text = exp.category.displayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SciCyan,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (exp.teacherGrade != null) {
                            Surface(
                                color = SciEmerald.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Graded: ${exp.teacherGrade}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SciEmerald,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            Surface(
                                color = SciAmber.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Pending Review",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SciAmber,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = exp.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Team: ${exp.collaborators.joinToString(", ")} • ${points.size} telemetry points",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Automated Feedback snippet
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SciCyan, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Auto-Analysis (R² = ${String.format("%.3f", feedback.rSquared)}):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SciCyan
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = feedback.scientificVerdict,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    if (exp.teacherNotes != null) {
                        Surface(
                            color = SciAmber.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Faculty Evaluation: \"${exp.teacherNotes}\"",
                                fontSize = 11.sp,
                                color = SciAmber,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    // 1-Tap Quick Grade Buttons for Teachers!
                    if (!isStudent) {
                        Text("1-Tap Quick Grade:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SciAmber)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            QuickGradeChip("A (96%)", SciEmerald) {
                                SciLabRepository.quickGrade(exp.id, "96/100 (A)")
                            }
                            QuickGradeChip("B+ (88%)", SciCyan) {
                                SciLabRepository.quickGrade(exp.id, "88/100 (B+)")
                            }
                            QuickGradeChip("C (75%)", SciAmber) {
                                SciLabRepository.quickGrade(exp.id, "75/100 (C)")
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onSelectExperiment(exp.id) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ShowChart, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Inspect Telemetry", fontSize = 12.sp)
                        }

                        if (!isStudent) {
                            Button(
                                onClick = {
                                    reviewingExperiment = exp
                                    gradeInput = exp.teacherGrade ?: "95/100 (A)"
                                    notesInput = exp.teacherNotes ?: "Empirical findings strongly substantiate the stated hypothesis."
                                    showReviewModal = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SciEmerald, contentColor = Color(0xFF003915)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("grade_lab_button_${exp.id}")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (exp.teacherGrade != null) "Edit Grade" else "Custom Rubric", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (exp.teacherGrade != null) "Assessed by Faculty" else "Awaiting Teacher",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (exp.teacherGrade != null) SciEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    // Teacher Review & Grade Dialog with 1-Tap Presets
    if (showReviewModal && reviewingExperiment != null) {
        val exp = reviewingExperiment!!
        val points = dataPointsMap[exp.id] ?: emptyList()
        val feedback = remember(points) {
            ScienceFeedbackEngine.analyzeExperimentData(points, exp.hypothesis)
        }

        AlertDialog(
            onDismissRequest = { showReviewModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = SciEmerald)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Grade Experiment & Rubric", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(exp.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("Collaborators: ${exp.collaborators.joinToString(", ")}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    // 1-Tap Rubric Score Presets
                    Text("1-Tap Grade & Notes Presets:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SciAmber)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PresetRubricChip("Grade A (95%)", SciEmerald) {
                            gradeInput = "95/100 (A)"
                            notesInput = "Empirical data demonstrates strong control of variables and rigorous harmonic oscillation conformity."
                        }
                        PresetRubricChip("Grade B+ (88%)", SciCyan) {
                            gradeInput = "88/100 (B+)"
                            notesInput = "Solid telemetry capture and clear methodology. Consider repeating trials with dampened noise."
                        }
                        PresetRubricChip("Grade C (75%)", SciAmber) {
                            gradeInput = "75/100 (C)"
                            notesInput = "Preliminary observations recorded. Recommend recalibrating sensor baseline to verify outlier variance."
                        }
                    }

                    // Button to inject automated AI feedback into notes
                    OutlinedButton(
                        onClick = {
                            notesInput = "${feedback.scientificVerdict} Linear regression R² = ${String.format("%.3f", feedback.rSquared)}. ${feedback.recommendations.firstOrNull() ?: ""}"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SciCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Apply AI Telemetry Verdict to Feedback", fontSize = 11.sp)
                    }

                    OutlinedTextField(
                        value = gradeInput,
                        onValueChange = { gradeInput = it },
                        label = { Text("Letter Grade / Score") },
                        placeholder = { Text("e.g. 96/100 (A)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("grade_input_field")
                    )

                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("Pedagogical Notes & Scientific Advice") },
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("teacher_notes_input_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        SciLabRepository.submitTeacherReview(
                            experimentId = exp.id,
                            grade = gradeInput.trim(),
                            feedbackNotes = notesInput.trim()
                        )
                        showReviewModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SciEmerald, contentColor = Color(0xFF003915)),
                    modifier = Modifier.testTag("submit_grade_button")
                ) {
                    Text("Publish Evaluation", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun QuickGradeChip(label: String, color: Color, onClick: () -> Unit) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun PresetRubricChip(label: String, color: Color, onClick: () -> Unit) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
        )
    }
}
