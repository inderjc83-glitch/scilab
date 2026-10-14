package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ForwardToInbox
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ShoppingCart
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
import com.example.data.SciLabRepository
import com.example.model.SubscriptionAlertEmail
import com.example.ui.theme.SciAmber
import com.example.ui.theme.SciCyan
import com.example.ui.theme.SciEmerald
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SubscriptionExpiryEmailDialog(
    userEmail: String,
    daysRemaining: Int,
    onRenewNow: () -> Unit,
    onDismiss: () -> Unit
) {
    val emailLogs by SciLabRepository.expirationEmails.collectAsState()
    val latestEmail = emailLogs.firstOrNull() ?: SubscriptionAlertEmail(
        recipientEmail = userEmail,
        subject = "⚠️ SciLab Alert: Subscription Completes in $daysRemaining Days",
        daysRemaining = daysRemaining,
        body = "Automated email reminder dispatched 2 days prior to subscription completion.",
        dispatchedAt = System.currentTimeMillis()
    )

    val dateFormatted = SimpleDateFormat("MMM dd, yyyy HH:mm:ss z", Locale.US).format(Date(latestEmail.dispatchedAt))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = SciAmber,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Subscription Renewal Alert", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = SciAmber.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SciAmber.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "STATUS: EXPIRES IN $daysRemaining DAYS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = SciAmber
                        )
                        Text(
                            text = "An automated renewal reminder email was dispatched to user 2 days prior to subscription completion.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ForwardToInbox, contentDescription = null, tint = SciCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Dispatched Automated Email Log", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SciCyan)
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        Text("To: ${latestEmail.recipientEmail}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("Subject: ${latestEmail.subject}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SciAmber)
                        Text("Sent: $dateFormatted", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = latestEmail.body,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

                // Days Adjustment / Resend Controls for Testing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            SciLabRepository.sendSubscriptionExpirationEmail(2)
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Resend 2-Day Mail", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            SciLabRepository.setSubscriptionDaysRemaining(1)
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Test 1 Day Left", fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onRenewNow()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SciEmerald, contentColor = Color(0xFF003915)),
                modifier = Modifier.testTag("renew_now_from_alert_button")
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Renew Subscription Now", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
