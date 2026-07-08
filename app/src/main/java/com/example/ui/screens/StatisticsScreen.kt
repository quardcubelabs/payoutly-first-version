package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.TransactionEntity
import com.example.ui.FinanceViewModel
import com.example.ui.components.SpendingLineChart
import com.example.ui.theme.LimeBrand
import com.example.util.CsvExportUtil
import com.example.util.CurrencyUtil

@Composable
fun StatisticsScreen(viewModel: FinanceViewModel) {
    val context = LocalContext.current
    val transactions by viewModel.transactions.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val selectedPeriod by viewModel.selectedTimePeriod.collectAsState()

    // 1. Filter transactions based on selectedTimePeriod
    val now = System.currentTimeMillis()
    val filteredTransactions = remember(transactions, selectedPeriod) {
        transactions.filter { tx ->
            val diffMs = now - tx.date
            when (selectedPeriod) {
                "Day" -> diffMs <= 86400000L // last 24h
                "Week" -> diffMs <= 86400000L * 7
                "Month" -> diffMs <= 86400000L * 30
                "Year" -> diffMs <= 86400000L * 365
                else -> true
            }
        }
    }

    // 2. Compute Debits (Expense), Credits (Income), and net Balance in selected currency
    val stats = remember(filteredTransactions, selectedCurrency) {
        var credits = 0.0
        var debits = 0.0
        for (tx in filteredTransactions) {
            val converted = CurrencyUtil.convert(tx.amount, tx.currency, selectedCurrency)
            if (tx.type == "INCOME") {
                credits += converted
            } else {
                debits += converted
            }
        }
        val net = credits - debits
        Triple(net, debits, credits)
    }
    val netBalance = stats.first
    val debitsTotal = stats.second
    val creditsTotal = stats.third

    // 3. Generate chart data (y-points and x-labels) based on period
    val (chartPoints, chartLabels) = remember(filteredTransactions, selectedPeriod, selectedCurrency) {
        val points = mutableListOf<Double>()
        val labels = mutableListOf<String>()

        when (selectedPeriod) {
            "Day" -> {
                // Last 24 hours split in 6 chunks of 4h
                labels.addAll(listOf("00:00", "04:00", "08:00", "12:00", "16:00", "20:00"))
                for (i in 0..5) {
                    val end = now - (86400000L / 6) * (5 - i)
                    val start = end - (86400000L / 6)
                    val sum = filteredTransactions
                        .filter { it.date in start..end && it.type == "EXPENSE" }
                        .fold(0.0) { s, t -> s + CurrencyUtil.convert(t.amount, t.currency, selectedCurrency) }
                    points.add(sum)
                }
            }
            "Week" -> {
                // Last 7 days
                labels.addAll(listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"))
                // Distribute last 7 days ending today
                for (i in 0..6) {
                    val end = now - 86400000L * (6 - i)
                    val start = end - 86400000L
                    val sum = filteredTransactions
                        .filter { it.date in start..end && it.type == "EXPENSE" }
                        .fold(0.0) { s, t -> s + CurrencyUtil.convert(t.amount, t.currency, selectedCurrency) }
                    points.add(sum)
                }
            }
            "Month" -> {
                // Last 30 days split in 5 chunks of 6 days
                labels.addAll(listOf("Wk 1", "Wk 2", "Wk 3", "Wk 4", "Wk 5"))
                for (i in 0..4) {
                    val end = now - (86400000L * 6) * (4 - i)
                    val start = end - (86400000L * 6)
                    val sum = filteredTransactions
                        .filter { it.date in start..end && it.type == "EXPENSE" }
                        .fold(0.0) { s, t -> s + CurrencyUtil.convert(t.amount, t.currency, selectedCurrency) }
                    points.add(sum)
                }
            }
            "Year" -> {
                // Last 12 months (grouped in 4 quarters for simpler high fidelity visualization)
                labels.addAll(listOf("Q1", "Q2", "Q3", "Q4"))
                for (i in 0..3) {
                    val end = now - (86400000L * 91) * (3 - i)
                    val start = end - (86400000L * 91)
                    val sum = filteredTransactions
                        .filter { it.date in start..end && it.type == "EXPENSE" }
                        .fold(0.0) { s, t -> s + CurrencyUtil.convert(t.amount, t.currency, selectedCurrency) }
                    points.add(sum)
                }
            }
        }

        // Ensure chart always has a nice baseline if all points are zero
        val finalPoints = if (points.all { it == 0.0 }) {
            points.mapIndexed { idx, _ -> (idx + 1) * 15.0 + 10.0 }
        } else {
            points
        }

        Pair(finalPoints, labels)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0D0E))
            .padding(horizontal = 20.dp)
    ) {
        // Upper spacing
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Title Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Statement",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // View Tabs Row: Day, Week, Month, Year
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.04f), shape = RoundedCornerShape(14.dp))
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Day", "Week", "Month", "Year").forEach { tab ->
                    val isSelected = selectedPeriod == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .background(
                                color = if (isSelected) Color.White.copy(alpha = 0.08f) else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { viewModel.selectTimePeriod(tab) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            color = if (isSelected) LimeBrand else Color.White.copy(alpha = 0.5f),
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Line Chart Area
        item {
            SpendingLineChart(
                points = chartPoints,
                labels = chartLabels,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Stat Cards Summaries (Balance, Debits, Credit)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Balance
                StatCard(
                    title = "Net Cashflow",
                    amount = netBalance,
                    currency = selectedCurrency,
                    color = if (netBalance >= 0) Color(0xFF30D158) else Color(0xFFFF5252),
                    modifier = Modifier.weight(1f)
                )

                // Debits
                StatCard(
                    title = "Debits Out",
                    amount = debitsTotal,
                    currency = selectedCurrency,
                    color = Color(0xFFFF5252),
                    modifier = Modifier.weight(1f)
                )

                // Credits
                StatCard(
                    title = "Credits In",
                    amount = creditsTotal,
                    currency = selectedCurrency,
                    color = Color(0xFF30D158),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
        }

        // Transaction list header + Export actions
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Statement Period Logs",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Export Actions
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            CsvExportUtil.shareCsvFile(context, filteredTransactions)
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = LimeBrand
                        ),
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(8.dp))
                            .height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Filtered transactions list
        if (filteredTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No activities found in this period",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(filteredTransactions) { tx ->
                TransactionRow(tx = tx, selectedCurrency = selectedCurrency, onDelete = {
                    viewModel.deleteTransaction(tx)
                })
            }
        }

        item {
            Spacer(modifier = Modifier.height(86.dp))
        }
    }
}

@Composable
fun StatCard(
    title: String,
    amount: Double,
    currency: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF191B1F)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = CurrencyUtil.format(amount, currency),
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }
    }
}
