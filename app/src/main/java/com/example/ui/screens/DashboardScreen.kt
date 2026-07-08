package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.TransactionEntity
import com.example.ui.FinanceViewModel
import com.example.ui.theme.LimeBrand
import com.example.util.CurrencyUtil
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    onQuickActionClick: (String) -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val session by viewModel.userSession.collectAsState()

    // Calculate overall balance
    val totalBalanceUSD = remember(transactions) {
        transactions.fold(0.0) { sum, tx ->
            val convertedAmount = CurrencyUtil.convert(tx.amount, tx.currency, "USD")
            if (tx.type == "INCOME") {
                sum + convertedAmount
            } else {
                sum - convertedAmount
            }
        }
    }

    val convertedBalance = remember(totalBalanceUSD, selectedCurrency) {
        CurrencyUtil.convert(totalBalanceUSD, "USD", selectedCurrency)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0D0E))
            .padding(horizontal = 20.dp)
    ) {
        // Safe drawing top spacing
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Header Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Profile Avatar Placeholder
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(LimeBrand),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (session?.name ?: "F").take(1).uppercase(),
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Welcome back 🔥",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = session?.name ?: "Guest User",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Notification Bell icon
                IconButton(
                    onClick = { /* Mute/unmute notifications */ },
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.05f), shape = CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Credit Card Container
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF191B1F))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF1A1D24), Color(0xFF111215))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top line: Currency Selector + Card Type Logo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Currency Quick Select Pill
                            Row(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.07f), shape = RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("USD", "EUR", "GBP", "JPY", "CAD").forEach { curr ->
                                    val isSelected = selectedCurrency == curr
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) LimeBrand else Color.Transparent)
                                            .clickable { viewModel.selectCurrency(curr) }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = curr,
                                            color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.5f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Visa Logo text
                            Text(
                                text = "VISA",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }

                        // Middle Line: Balance amount
                        Column {
                            Text(
                                text = "Your balance",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = CurrencyUtil.format(convertedBalance, selectedCurrency),
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Bottom line: Card Account Masked + Expiry
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Account Number:  **** 9564",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "02/30",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Action Buttons Grid ("Send", "Received", "Add Goal", "Export")
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickActionButton(
                    title = "Send",
                    icon = Icons.Default.ArrowUpward,
                    iconColor = Color(0xFFFF5252),
                    onClick = { onQuickActionClick("SEND") }
                )
                QuickActionButton(
                    title = "Received",
                    icon = Icons.Default.ArrowDownward,
                    iconColor = Color(0xFF30D158),
                    onClick = { onQuickActionClick("RECEIVE") }
                )
                QuickActionButton(
                    title = "Add Goal",
                    icon = Icons.Default.GolfCourse,
                    iconColor = LimeBrand,
                    onClick = { onQuickActionClick("GOAL") }
                )
                QuickActionButton(
                    title = "More",
                    icon = Icons.Default.MoreHoriz,
                    iconColor = Color.White,
                    onClick = { onQuickActionClick("SETTINGS") }
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
        }

        // Transactions Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaction list",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Today",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Transaction Items
        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No transactions yet",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(transactions.take(15)) { tx ->
                TransactionRow(tx = tx, selectedCurrency = selectedCurrency, onDelete = {
                    viewModel.deleteTransaction(tx)
                })
            }
        }

        item {
            Spacer(modifier = Modifier.height(86.dp)) // Bottom spacing for FAB and nav bar
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Color(0xFF191B1F), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TransactionRow(
    tx: TransactionEntity,
    selectedCurrency: String,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(tx.date) { dateFormat.format(Date(tx.date)) }

    val categoryIcon = when (tx.category) {
        "Food" -> Icons.Default.Fastfood
        "Shopping" -> Icons.Default.ShoppingBag
        "Transport" -> Icons.Default.DirectionsCar
        "Utilities" -> Icons.Default.Power
        "Entertainment" -> Icons.Default.LocalPlay
        "Income" -> Icons.Default.Paid
        else -> Icons.Default.Receipt
    }

    val convertedAmount = remember(tx.amount, tx.currency, selectedCurrency) {
        CurrencyUtil.convert(tx.amount, tx.currency, selectedCurrency)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .background(Color(0xFF111215), shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.05f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = tx.category,
                    tint = if (tx.type == "INCOME") Color(0xFF30D158) else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = tx.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${tx.category} • $formattedDate",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = (if (tx.type == "INCOME") "+ " else "- ") + CurrencyUtil.format(convertedAmount, selectedCurrency),
                color = if (tx.type == "INCOME") Color(0xFF30D158) else Color(0xFFFF5252),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete transaction",
                    tint = Color.White.copy(alpha = 0.25f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
