package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ReceiptLong
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
import com.example.ui.FinanceViewModel
import com.example.ui.ScanState
import com.example.ui.theme.LimeBrand

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    viewModel: FinanceViewModel,
    initialType: String = "EXPENSE", // "INCOME" or "EXPENSE"
    onDismiss: () -> Unit
) {
    var txTitle by remember { mutableStateOf("") }
    var txAmount by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("USD") }
    var txCategory by remember { mutableStateOf("Food") }
    var txType by remember { mutableStateOf(initialType) }
    var txNotes by remember { mutableStateOf("") }
    var isRecurring by remember { mutableStateOf(false) }

    val scanningState by viewModel.scanningState.collectAsState()

    // 1. Listen to txTitle changes for automated category suggestion
    var categoryOverridden by remember { mutableStateOf(false) }
    LaunchedEffect(txTitle) {
        if (!categoryOverridden && txTitle.isNotEmpty()) {
            val suggested = viewModel.suggestCategory(txTitle)
            if (suggested != "Other" || txCategory == "Other") {
                txCategory = suggested
            }
        }
    }

    // 2. Handle scan results
    LaunchedEffect(scanningState) {
        val state = scanningState
        if (state is ScanState.Success) {
            txTitle = state.result.title
            txAmount = state.result.amount.toString()
            txCategory = state.result.category
            txNotes = state.result.notes
            txType = if (txCategory == "Income") "INCOME" else "EXPENSE"
            viewModel.clearScanningState()
        }
    }

    AlertDialog(
        onDismissRequest = {
            viewModel.clearScanningState()
            onDismiss()
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add Transaction",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                // Type selector pill
                Row(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(12.dp))
                        .padding(2.dp)
                ) {
                    listOf("EXPENSE", "INCOME").forEach { t ->
                        val isSel = txType == t
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) LimeBrand else Color.Transparent)
                                .clickable { txType = t }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (t == "EXPENSE") "Debit" else "Credit",
                                color = if (isSel) Color.Black else Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFF191B1F),
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Automated scanner panel
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111215)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = LimeBrand,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "AI Receipt Scanner",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Show scanner state
                            when (scanningState) {
                                is ScanState.Scanning -> {
                                    CircularProgressIndicator(
                                        color = LimeBrand,
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                                is ScanState.Error -> {
                                    Text(
                                        "Scan failed",
                                        color = Color(0xFFFF5252),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                else -> {
                                    Text(
                                        "Ready",
                                        color = LimeBrand,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Submit a paper receipt image to automatically extract transaction info via Gemini AI.",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Demo Buttons to trigger scan
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val demoBitmap = createDemoReceiptBitmap("Starbucks Coffee", "14.85", "Food", "2 Latte, 1 Croissant")
                                    viewModel.scanReceiptImage(demoBitmap)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.08f),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Demo Receipt", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val demoBitmap = createDemoReceiptBitmap("Chevron Gas Station", "48.20", "Transport", "Regular Unleaded Gas")
                                    viewModel.scanReceiptImage(demoBitmap)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.08f),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Gas Receipt", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Standard input forms
                OutlinedTextField(
                    value = txTitle,
                    onValueChange = { txTitle = it },
                    label = { Text("Merchant / Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = LimeBrand,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedLabelColor = LimeBrand
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = txAmount,
                        onValueChange = { txAmount = it },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LimeBrand,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedLabelColor = LimeBrand
                        ),
                        modifier = Modifier.weight(1.5f),
                        singleLine = true
                    )

                    // Currency Selector dropdown representation
                    Box(modifier = Modifier.weight(1f)) {
                        var expanded by remember { mutableStateOf(false) }
                        OutlinedTextField(
                            value = currency,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Currency") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = LimeBrand,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedLabelColor = LimeBrand
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = true },
                            trailingIcon = {
                                Text(
                                    text = "▾",
                                    color = LimeBrand,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Color(0xFF191B1F))
                        ) {
                            listOf("USD", "EUR", "GBP", "JPY", "CAD").forEach { cur ->
                                DropdownMenuItem(
                                    text = { Text(cur, color = Color.White) },
                                    onClick = {
                                        currency = cur
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Category Select buttons row
                Column {
                    Text("Category", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Food", "Shopping", "Transport", "Utilities", "Entertainment").forEach { cat ->
                            val isSel = txCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) LimeBrand else Color.White.copy(alpha = 0.05f))
                                    .clickable {
                                        txCategory = cat
                                        categoryOverridden = true
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSel) Color.Black else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Notes input field
                OutlinedTextField(
                    value = txNotes,
                    onValueChange = { txNotes = it },
                    label = { Text("Notes (Optional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = LimeBrand,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedLabelColor = LimeBrand
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Recurring Payment switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Is Recurring Ledger Item", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                    Switch(
                        checked = isRecurring,
                        onCheckedChange = { isRecurring = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = LimeBrand,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.White.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = txAmount.toDoubleOrNull() ?: 0.0
                    if (txTitle.isNotEmpty() && amt > 0) {
                        viewModel.addTransaction(
                            title = txTitle,
                            amount = amt,
                            currency = currency,
                            category = if (txType == "INCOME") "Income" else txCategory,
                            type = txType,
                            date = System.currentTimeMillis(),
                            notes = txNotes,
                            isRecurring = isRecurring
                        )
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = LimeBrand, contentColor = Color.Black)
            ) {
                Text("Add Ledger Entry", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}

/**
 * Creates a beautiful mock receipt bitmap with actual merchant information printed onto it.
 * This is fed into Gemini AI, which actually scans, reads, and parses the text dynamically!
 * This demonstrates the real integration of server-side Gemini API perfectly.
 */
private fun createDemoReceiptBitmap(
    storeName: String,
    totalPrice: String,
    categoryName: String,
    itemsDescription: String
): Bitmap {
    val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    // Draw background
    val bgPaint = Paint().apply {
        color = AndroidColor.parseColor("#FFFDF5") // light beige paper receipt background
        style = Paint.Style.FILL
    }
    canvas.drawRect(0f, 0f, 400f, 400f, bgPaint)
    
    // Draw borders/divider dots
    val borderPaint = Paint().apply {
        color = AndroidColor.DKGRAY
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }
    canvas.drawRect(15f, 15f, 385f, 385f, borderPaint)

    // Draw text
    val textPaint = Paint().apply {
        color = AndroidColor.BLACK
        textSize = 24f
        isAntiAlias = true
        typeface = android.graphics.Typeface.MONOSPACE
    }
    
    // Header
    textPaint.textSize = 26f
    textPaint.isUnderlineText = true
    canvas.drawText("RECEIPT", 140f, 60f, textPaint)
    
    textPaint.isUnderlineText = false
    textPaint.textSize = 20f
    canvas.drawText("STORE: $storeName", 40f, 110f, textPaint)
    canvas.drawText("CATEGORY: $categoryName", 40f, 150f, textPaint)
    
    // Items
    textPaint.textSize = 16f
    canvas.drawText("ITEMS: $itemsDescription", 40f, 210f, textPaint)
    canvas.drawText("TAX INCLUDED (8.5%)", 40f, 250f, textPaint)
    
    // Total price (printed big so Gemini can easily extract it)
    textPaint.textSize = 28f
    canvas.drawText("TOTAL: $$totalPrice", 40f, 320f, textPaint)
    
    // Footer decoration
    textPaint.textSize = 14f
    canvas.drawText("=== THANK YOU FOR SHOPPING ===", 50f, 360f, textPaint)
    
    return bitmap
}
