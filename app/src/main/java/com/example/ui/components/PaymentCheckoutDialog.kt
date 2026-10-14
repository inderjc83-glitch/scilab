package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PaymentTransaction
import com.example.ui.theme.SciAmber
import com.example.ui.theme.SciCyan
import com.example.ui.theme.SciEmerald

@Composable
fun PaymentCheckoutDialog(
    userEmail: String,
    onPaymentSuccess: (PaymentTransaction) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTierIndex by remember { mutableIntStateOf(2) } // Default to 1 Year ($9)
    var paymentMethod by remember { mutableStateOf("Stripe") } // "Stripe" or "PayPal"
    var cardNumber by remember { mutableStateOf("4242 •••• •••• 4242") }
    var cardExpiry by remember { mutableStateOf("12/28") }
    var cardCvc by remember { mutableStateOf("888") }
    var cardholderName by remember { mutableStateOf("Alex Rivera") }
    var payerEmail by remember { mutableStateOf(userEmail) }
    var isProcessing by remember { mutableStateOf(false) }

    val tiers = listOf(
        Triple("1 Month Access", 1.00, "For rapid field trials and short assignments"),
        Triple("6 Months Access", 5.00, "Ideal for a full academic semester"),
        Triple("1 Year Full Access", 9.00, "Best Value • Full cloud storage & quiz mastery")
    )

    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = SciCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Secure Subscription Checkout", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Text(
                    text = "1-Week Free Trial active. Choose your recurring plan below.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tier selector cards
                Text("Select Subscription Tier:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                tiers.forEachIndexed { idx, tier ->
                    val isSelected = selectedTierIndex == idx
                    Surface(
                        color = if (isSelected) SciCyan.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) SciCyan else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTierIndex = idx }
                            .testTag("tier_selector_$idx")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedTierIndex = idx },
                                colors = RadioButtonDefaults.colors(selectedColor = SciCyan)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = tier.first,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    if (idx == 2) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = SciAmber.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "BEST VALUE",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SciAmber,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = tier.third,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "$${tier.second.toInt()}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) SciCyan else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Payment Gateway Selector
                Text("Select Payment Gateway:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = paymentMethod == "Stripe",
                        onClick = { paymentMethod = "Stripe" },
                        label = { Text("Stripe (Credit / Debit)") },
                        leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("gateway_stripe")
                    )
                    FilterChip(
                        selected = paymentMethod == "PayPal",
                        onClick = { paymentMethod = "PayPal" },
                        label = { Text("PayPal") },
                        leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("gateway_paypal")
                    )
                }

                if (paymentMethod == "Stripe") {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Stripe Secure Card Element", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SciCyan)

                            OutlinedTextField(
                                value = cardNumber,
                                onValueChange = { cardNumber = it },
                                label = { Text("Card Number") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("card_number_input")
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = cardExpiry,
                                    onValueChange = { cardExpiry = it },
                                    label = { Text("MM/YY") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = cardCvc,
                                    onValueChange = { cardCvc = it },
                                    label = { Text("CVC") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            OutlinedTextField(
                                value = cardholderName,
                                onValueChange = { cardholderName = it },
                                label = { Text("Cardholder Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("PayPal Direct Express Checkout", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SciCyan)
                            Text(
                                text = "One-Touch Secure PayPal Billing",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SciEmerald
                            )
                            Text(
                                text = "Recurring subscriptions & billing managed seamlessly with instant fraud protection.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = payerEmail,
                    onValueChange = { payerEmail = it },
                    label = { Text("Receipt Destination Email") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("receipt_email_input")
                )
            }
        },
        confirmButton = {
            val tier = tiers[selectedTierIndex]
            Button(
                onClick = {
                    isProcessing = true
                    val txn = com.example.data.SciLabRepository.processSubscriptionPayment(
                        planTitle = tier.first,
                        amount = tier.second,
                        gateway = paymentMethod,
                        email = payerEmail
                    )
                    isProcessing = false
                    onPaymentSuccess(txn)
                },
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = SciEmerald, contentColor = Color(0xFF003915)),
                modifier = Modifier.testTag("pay_confirm_button")
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                } else {
                    Text("Pay $${tier.second.toInt()}.00 & Activate", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isProcessing) {
                Text("Cancel")
            }
        }
    )
}
