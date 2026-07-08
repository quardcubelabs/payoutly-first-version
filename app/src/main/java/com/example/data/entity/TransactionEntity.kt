package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val currency: String, // USD, EUR, GBP, etc.
    val category: String, // Food, Shopping, Transport, Utilities, Entertainment, Income, etc.
    val type: String, // "INCOME" or "EXPENSE"
    val date: Long, // timestamp in ms
    val notes: String = "",
    val receiptImageUri: String? = null,
    val isRecurring: Boolean = false
)
