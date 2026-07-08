package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_payments")
data class RecurringPaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val currency: String,
    val dueDate: Long, // timestamp
    val frequency: String, // "Weekly", "Monthly", "Yearly"
    val isPaid: Boolean = false
)
