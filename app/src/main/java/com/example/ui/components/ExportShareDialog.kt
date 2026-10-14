package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExperimentDataPoint
import com.example.model.ScienceExperiment
import com.example.ui.theme.SciCyan

@Composable
fun ExportShareDialog(
    experiment: ScienceExperiment,
    dataPoints: List<ExperimentDataPoint>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedFormat by remember { mutableStateOf("CSV") } // "CSV" or "JSON" or "Summary"

    val csvText = buildString {
        appendLine("Experiment: ${experiment.title}")
        appendLine("Category: ${experiment.category.displayName}")
        appendLine("Hypothesis: ${experiment.hypothesis}")
        appendLine("Checksum: ${experiment.encryptionChecksum}")
        appendLine("---")
        appendLine("TimestampOffset(s),Parameter,Value,Unit,Notes")
        dataPoints.forEach { pt ->
            appendLine("${pt.timeOffsetSec},${pt.paramName},${pt.value},${pt.unit},\"${pt.notes}\"")
        }
    }

    val jsonText = buildString {
        appendLine("{")
        appendLine("  \"id\": \"${experiment.id}\",")
        appendLine("  \"title\": \"${experiment.title}\",")
        appendLine("  \"category\": \"${experiment.category.name}\",")
        appendLine("  \"hypothesis\": \"${experiment.hypothesis}\",")
        appendLine("  \"points_count\": ${dataPoints.size},")
        appendLine("  \"data\": [")
        dataPoints.forEachIndexed { i, pt ->
            val comma = if (i == dataPoints.lastIndex) "" else ","
            appendLine("    {\"timeOffset\": ${pt.timeOffsetSec}, \"param\": \"${pt.paramName}\", \"value\": ${pt.value}, \"unit\": \"${pt.unit}\"}$comma")
        }
        appendLine("  ]")
        appendLine("}")
    }

    val summaryText = buildString {
        appendLine("🔬 SciLab Experiment Report: ${experiment.title}")
        appendLine("Category: ${experiment.category.displayName}")
        appendLine("Collaborators: ${experiment.collaborators.joinToString(", ")}")
        appendLine("Hypothesis: ${experiment.hypothesis}")
        if (dataPoints.isNotEmpty()) {
            val vals = dataPoints.map { it.value }
            appendLine("Samples: ${dataPoints.size}")
            appendLine("Mean: ${String.format("%.3f", vals.average())}")
            appendLine("Range: [${vals.minOrNull()} - ${vals.maxOrNull()}]")
        }
        appendLine("E2E Encryption: ${experiment.encryptionChecksum}")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Share, contentDescription = null, tint = SciCyan, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export & Social Share", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("export_dialog"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedFormat == "CSV",
                        onClick = { selectedFormat = "CSV" },
                        label = { Text("CSV") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedFormat == "JSON",
                        onClick = { selectedFormat = "JSON" },
                        label = { Text("JSON") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedFormat == "Summary",
                        onClick = { selectedFormat = "Summary" },
                        label = { Text("Report") },
                        modifier = Modifier.weight(1f)
                    )
                }

                val preview = when (selectedFormat) {
                    "CSV" -> csvText
                    "JSON" -> jsonText
                    else -> summaryText
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Text(
                        text = preview,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val shareContent = when (selectedFormat) {
                        "CSV" -> csvText
                        "JSON" -> jsonText
                        else -> summaryText
                    }
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareContent)
                        putExtra(Intent.EXTRA_SUBJECT, "SciLab Findings: ${experiment.title}")
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Share Scientific Findings")
                    context.startActivity(shareIntent)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SciCyan, contentColor = Color(0xFF0A192F)),
                modifier = Modifier.testTag("share_action_button")
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share findings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
