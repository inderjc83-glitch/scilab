package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.data.SciLabRepository
import com.example.model.ScienceCategory
import com.example.model.ScienceQuizQuestion
import com.example.ui.theme.SciAmber
import com.example.ui.theme.SciCoral
import com.example.ui.theme.SciCyan
import com.example.ui.theme.SciEmerald

@Composable
fun QuizGameScreen(
    modifier: Modifier = Modifier
) {
    val allQuizzes by SciLabRepository.quizzes.collectAsState()

    var selectedCategory by remember { mutableStateOf<ScienceCategory?>(null) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isAnswerSubmitted by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var quizCompleted by remember { mutableStateOf(false) }

    val activeQuizList = remember(selectedCategory, allQuizzes) {
        if (selectedCategory == null) allQuizzes else allQuizzes.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Header & Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Science Knowledge Arena",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Test your scientific principles, mechanics, and laboratory protocols",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Score Banner
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Current Score", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$score pts", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SciCyan)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Streak", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = SciAmber, modifier = Modifier.size(18.dp))
                            Text("$streak 🔥", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SciAmber)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Question", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "${(currentQuestionIndex + 1).coerceAtMost(activeQuizList.size)}/${activeQuizList.size}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SciEmerald
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Domain Filter Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = {
                        selectedCategory = null
                        currentQuestionIndex = 0
                        selectedOptionIndex = null
                        isAnswerSubmitted = false
                        quizCompleted = false
                    },
                    label = { Text("All Domains", fontSize = 11.sp) }
                )
                ScienceCategory.values().forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = {
                            selectedCategory = cat
                            currentQuestionIndex = 0
                            selectedOptionIndex = null
                            isAnswerSubmitted = false
                            quizCompleted = false
                        },
                        label = { Text(cat.name.take(4), fontSize = 11.sp) }
                    )
                }
            }
        }

        if (quizCompleted || activeQuizList.isEmpty() || currentQuestionIndex >= activeQuizList.size) {
            item {
                Surface(
                    color = SciEmerald.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SciEmerald.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = SciAmber, modifier = Modifier.size(54.dp))
                        Text("Quiz Completed!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SciEmerald)
                        Text(
                            text = "You scored $score points with a top streak of $streak!",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Button(
                            onClick = {
                                currentQuestionIndex = 0
                                selectedOptionIndex = null
                                isAnswerSubmitted = false
                                quizCompleted = false
                                score = 0
                                streak = 0
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SciCyan, contentColor = Color(0xFF0A192F)),
                            modifier = Modifier.testTag("restart_quiz_button")
                        ) {
                            Text("Play Again", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            val q = activeQuizList[currentQuestionIndex]

            item {
                // Question Card
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(
                            color = SciCyan.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = q.category.displayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SciCyan,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Text(
                            text = q.question,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Options
            itemsIndexed(q.options) { idx, option ->
                val isSelected = selectedOptionIndex == idx
                val isCorrect = idx == q.correctIndex

                val cardColor = when {
                    !isAnswerSubmitted && isSelected -> SciCyan.copy(alpha = 0.2f)
                    isAnswerSubmitted && isCorrect -> SciEmerald.copy(alpha = 0.25f)
                    isAnswerSubmitted && isSelected && !isCorrect -> SciCoral.copy(alpha = 0.25f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                val borderColor = when {
                    !isAnswerSubmitted && isSelected -> SciCyan
                    isAnswerSubmitted && isCorrect -> SciEmerald
                    isAnswerSubmitted && isSelected && !isCorrect -> SciCoral
                    else -> Color.Transparent
                }

                Surface(
                    color = cardColor,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isAnswerSubmitted) {
                            selectedOptionIndex = idx
                        }
                        .testTag("quiz_option_$idx")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (isSelected) SciCyan else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = ('A'.code + idx).toChar().toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color(0xFF0A192F) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = option,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        if (isAnswerSubmitted) {
                            if (isCorrect) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Correct", tint = SciEmerald, modifier = Modifier.size(20.dp))
                            } else if (isSelected) {
                                Icon(Icons.Default.Cancel, contentDescription = "Incorrect", tint = SciCoral, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            // Explanation & Next Action
            item {
                AnimatedVisibility(visible = isAnswerSubmitted) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.School, contentDescription = null, tint = SciCyan, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Scientific Explanation:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SciCyan)
                            }
                            Text(
                                text = q.scientificExplanation,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (!isAnswerSubmitted) {
                    Button(
                        onClick = {
                            if (selectedOptionIndex != null) {
                                isAnswerSubmitted = true
                                if (selectedOptionIndex == q.correctIndex) {
                                    score += 10 + streak * 2
                                    streak += 1
                                } else {
                                    streak = 0
                                }
                            }
                        },
                        enabled = selectedOptionIndex != null,
                        colors = ButtonDefaults.buttonColors(containerColor = SciCyan, contentColor = Color(0xFF0A192F)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_quiz_answer_button")
                    ) {
                        Text("Submit Answer", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            if (currentQuestionIndex + 1 < activeQuizList.size) {
                                currentQuestionIndex += 1
                                selectedOptionIndex = null
                                isAnswerSubmitted = false
                            } else {
                                quizCompleted = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SciEmerald, contentColor = Color(0xFF003915)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("next_quiz_question_button")
                    ) {
                        Text(
                            if (currentQuestionIndex + 1 < activeQuizList.size) "Next Question →" else "Finish Quiz & View Results",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
