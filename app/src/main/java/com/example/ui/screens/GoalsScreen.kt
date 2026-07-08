package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.BudgetEntity
import com.example.data.entity.BudgetGoalEntity
import com.example.data.entity.RecurringPaymentEntity
import com.example.ui.FinanceViewModel
import com.example.ui.theme.LimeBrand
import com.example.util.CurrencyUtil
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(viewModel: FinanceViewModel) {
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val budgets by viewModel.budgets.collectAsState()
    val goals by viewModel.goals.collectAsState()
    val recurringPayments by viewModel.recurringPayments.collectAsState()

    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddRecurringDialog by remember { mutableStateOf(false) }
    var showAddBudgetDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0D0E))
            .padding(horizontal = 20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Budgets & Goal Trackers",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Section 1: Active Budgets limits vs spent
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category Limits",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showAddBudgetDialog = true }) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Add Budget Limit", tint = LimeBrand)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (budgets.isEmpty()) {
            item {
                EmptyStateCard("No Category Limits set. Tap '+' to configure one.")
            }
        } else {
            items(budgets) { budget ->
                // Calculate spent in this category dynamically
                val spentInUSD = transactions
                    .filter { it.category == budget.category && it.type == "EXPENSE" }
                    .fold(0.0) { sum, tx -> sum + CurrencyUtil.convert(tx.amount, tx.currency, "USD") }
                
                val spentInSelected = CurrencyUtil.convert(spentInUSD, "USD", selectedCurrency)
                val budgetLimitSelected = CurrencyUtil.convert(budget.limitAmount, budget.currency, selectedCurrency)
                val percentage = if (budgetLimitSelected > 0) (spentInSelected / budgetLimitSelected).coerceIn(0.0..1.0) else 0.0

                CategoryBudgetRow(
                    budget = budget,
                    spentAmount = spentInSelected,
                    limitAmount = budgetLimitSelected,
                    percentage = percentage,
                    selectedCurrency = selectedCurrency,
                    onDelete = { viewModel.deleteBudget(budget) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Section 2: Budgeting Goals Tracker (e.g. Hawaii Summer, New Car)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Budgeting Goals",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showAddGoalDialog = true }) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Add Goal", tint = LimeBrand)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (goals.isEmpty()) {
            item {
                EmptyStateCard("No active target goals. Setup a goal to track savings.")
            }
        } else {
            items(goals) { goal ->
                val targetSelected = CurrencyUtil.convert(goal.targetAmount, goal.currency, selectedCurrency)
                val currentSelected = CurrencyUtil.convert(goal.currentAmount, goal.currency, selectedCurrency)
                val progress = if (targetSelected > 0) (currentSelected / targetSelected).coerceIn(0.0..1.0) else 0.0

                GoalRow(
                    goal = goal,
                    currentAmountSelected = currentSelected,
                    targetAmountSelected = targetSelected,
                    progress = progress,
                    selectedCurrency = selectedCurrency,
                    onAddFunds = { amount -> viewModel.updateGoalProgress(goal, amount) },
                    onDelete = { viewModel.deleteGoal(goal) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Section 3: Recurring Payments Tracker
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recurring Bill Reminders",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showAddRecurringDialog = true }) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Add Bill Reminder", tint = LimeBrand)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (recurringPayments.isEmpty()) {
            item {
                EmptyStateCard("No recurring monthly bill reminders.")
            }
        } else {
            items(recurringPayments) { bill ->
                BillRow(
                    bill = bill,
                    selectedCurrency = selectedCurrency,
                    onTogglePaid = { viewModel.toggleRecurringPaymentPaid(bill) },
                    onDelete = { viewModel.deleteRecurringPayment(bill) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(96.dp))
        }
    }

    // --- DIALOGS FOR ADDING DATA ---

    // 1. Add Budget Limit Dialog
    if (showAddBudgetDialog) {
        var budgetLimitInput by remember { mutableStateOf("") }
        var categorySelected by remember { mutableStateOf("Food") }

        AlertDialog(
            onDismissRequest = { showAddBudgetDialog = false },
            title = { Text("Add Category Limit", color = Color.White) },
            containerColor = Color(0xFF191B1F),
            text = {
                Column {
                    // Category drop list simplified using beautiful row selector
                    Text("Select Category:", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Food", "Shopping", "Transport", "Utilities", "Entertainment").forEach { cat ->
                            val isSelected = categorySelected == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) LimeBrand else Color.White.copy(alpha = 0.05f))
                                    .clickable { categorySelected = cat }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = budgetLimitInput,
                        onValueChange = { budgetLimitInput = it },
                        label = { Text("Monthly Limit Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LimeBrand,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedLabelColor = LimeBrand
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limit = budgetLimitInput.toDoubleOrNull() ?: 0.0
                        if (limit > 0) {
                            viewModel.addBudget(categorySelected, limit, selectedCurrency)
                        }
                        showAddBudgetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LimeBrand, contentColor = Color.Black)
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBudgetDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    // 2. Add Saving Goal Dialog
    if (showAddGoalDialog) {
        var goalTitle by remember { mutableStateOf("") }
        var goalTarget by remember { mutableStateOf("") }
        var goalCurrent by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddGoalDialog = false },
            title = { Text("Create Savings Goal", color = Color.White) },
            containerColor = Color(0xFF191B1F),
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = goalTitle,
                        onValueChange = { goalTitle = it },
                        label = { Text("Goal Title (e.g. New Car)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LimeBrand,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedLabelColor = LimeBrand
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = goalTarget,
                        onValueChange = { goalTarget = it },
                        label = { Text("Target Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LimeBrand,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedLabelColor = LimeBrand
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = goalCurrent,
                        onValueChange = { goalCurrent = it },
                        label = { Text("Initial Saved Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LimeBrand,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedLabelColor = LimeBrand
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = goalTarget.toDoubleOrNull() ?: 0.0
                        val current = goalCurrent.toDoubleOrNull() ?: 0.0
                        if (goalTitle.isNotEmpty() && target > 0) {
                            viewModel.addGoal(
                                title = goalTitle,
                                target = target,
                                current = current,
                                currency = selectedCurrency,
                                date = System.currentTimeMillis() + 86400000L * 180 // 6 months target fallback
                            )
                        }
                        showAddGoalDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LimeBrand, contentColor = Color.Black)
                ) {
                    Text("Create", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGoalDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    // 3. Add Recurring Reminder Dialog
    if (showAddRecurringDialog) {
        var billTitle by remember { mutableStateOf("") }
        var billAmount by remember { mutableStateOf("") }
        var billFrequency by remember { mutableStateOf("Monthly") }

        AlertDialog(
            onDismissRequest = { showAddRecurringDialog = false },
            title = { Text("Add Recurring Bill", color = Color.White) },
            containerColor = Color(0xFF191B1F),
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = billTitle,
                        onValueChange = { billTitle = it },
                        label = { Text("Bill Title (e.g. Rent, AWS)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LimeBrand,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedLabelColor = LimeBrand
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = billAmount,
                        onValueChange = { billAmount = it },
                        label = { Text("Bill Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LimeBrand,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedLabelColor = LimeBrand
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Frequency selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Weekly", "Monthly", "Yearly").forEach { freq ->
                            val isSelected = billFrequency == freq
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) LimeBrand else Color.White.copy(alpha = 0.05f))
                                    .clickable { billFrequency = freq }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = freq,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = billAmount.toDoubleOrNull() ?: 0.0
                        if (billTitle.isNotEmpty() && amt > 0) {
                            viewModel.addRecurringPayment(
                                title = billTitle,
                                amount = amt,
                                currency = selectedCurrency,
                                dueDate = System.currentTimeMillis() + 86400000L * 30, // 30 days next due date
                                frequency = billFrequency
                            )
                        }
                        showAddRecurringDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LimeBrand, contentColor = Color.Black)
                ) {
                    Text("Add Bill", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRecurringDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun EmptyStateCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111215)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun CategoryBudgetRow(
    budget: BudgetEntity,
    spentAmount: Double,
    limitAmount: Double,
    percentage: Double,
    selectedCurrency: String,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF191B1F)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(LimeBrand, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = budget.category,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${CurrencyUtil.format(spentAmount, selectedCurrency)} of ${CurrencyUtil.format(limitAmount, selectedCurrency)}",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = percentage.toFloat(),
                color = if (percentage > 0.9) Color(0xFFFF5252) else LimeBrand,
                trackColor = Color.White.copy(alpha = 0.1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
            )
        }
    }
}

@Composable
fun GoalRow(
    goal: BudgetGoalEntity,
    currentAmountSelected: Double,
    targetAmountSelected: Double,
    progress: Double,
    selectedCurrency: String,
    onAddFunds: (Double) -> Unit,
    onDelete: () -> Unit
) {
    var showAddFundsDialog by remember { mutableStateOf(false) }
    var fundsInput by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF191B1F)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = goal.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Progress: ${(progress * 100).toInt()}%",
                        color = LimeBrand,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showAddFundsDialog = true }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add cash", tint = LimeBrand)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = progress.toFloat(),
                color = LimeBrand,
                trackColor = Color.White.copy(alpha = 0.1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = CurrencyUtil.format(currentAmountSelected, selectedCurrency),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Goal: " + CurrencyUtil.format(targetAmountSelected, selectedCurrency),
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            }
        }
    }

    if (showAddFundsDialog) {
        AlertDialog(
            onDismissRequest = { showAddFundsDialog = false },
            title = { Text("Add Saved Funds", color = Color.White) },
            containerColor = Color(0xFF191B1F),
            text = {
                OutlinedTextField(
                    value = fundsInput,
                    onValueChange = { fundsInput = it },
                    label = { Text("Amount to Add") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = LimeBrand,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedLabelColor = LimeBrand
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val added = fundsInput.toDoubleOrNull() ?: 0.0
                        if (added > 0) {
                            onAddFunds(added)
                        }
                        showAddFundsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LimeBrand, contentColor = Color.Black)
                ) {
                    Text("Add", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFundsDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun BillRow(
    bill: RecurringPaymentEntity,
    selectedCurrency: String,
    onTogglePaid: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(bill.dueDate) {
        val format = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        format.format(Date(bill.dueDate))
    }

    val convertedAmount = remember(bill.amount, bill.currency, selectedCurrency) {
        CurrencyUtil.convert(bill.amount, bill.currency, selectedCurrency)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (bill.isPaid) Color(0xFF191B1F).copy(alpha = 0.5f) else Color(0xFF191B1F)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circle checkbox
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = if (bill.isPaid) LimeBrand else Color.White.copy(alpha = 0.05f),
                            shape = CircleShape
                        )
                        .clickable { onTogglePaid() },
                    contentAlignment = Alignment.Center
                ) {
                    if (bill.isPaid) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Paid",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = bill.title,
                        color = if (bill.isPaid) Color.White.copy(alpha = 0.4f) else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (bill.isPaid) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Due: $dateStr • ${bill.frequency}",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = CurrencyUtil.format(convertedAmount, selectedCurrency),
                    color = if (bill.isPaid) Color.White.copy(alpha = 0.4f) else LimeBrand,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete bill",
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
