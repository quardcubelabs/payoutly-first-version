package com.example.data.dao

import androidx.room.*
import com.example.data.entity.BudgetEntity
import com.example.data.entity.BudgetGoalEntity
import com.example.data.entity.RecurringPaymentEntity
import com.example.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {

    // --- Transactions ---
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAllTransactions(): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Int)

    // --- Budgets ---
    @Query("SELECT * FROM budgets")
    fun getAllBudgetsFlow(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets")
    suspend fun getAllBudgets(): List<BudgetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteBudgetById(id: Int)

    // --- Recurring Payments ---
    @Query("SELECT * FROM recurring_payments ORDER BY dueDate ASC")
    fun getAllRecurringPaymentsFlow(): Flow<List<RecurringPaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringPayment(payment: RecurringPaymentEntity)

    @Delete
    suspend fun deleteRecurringPayment(payment: RecurringPaymentEntity)

    @Query("UPDATE recurring_payments SET isPaid = :isPaid WHERE id = :id")
    suspend fun updateRecurringPaymentStatus(id: Int, isPaid: Boolean)

    // --- Budget Goals ---
    @Query("SELECT * FROM budget_goals")
    fun getAllGoalsFlow(): Flow<List<BudgetGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: BudgetGoalEntity)

    @Delete
    suspend fun deleteGoal(goal: BudgetGoalEntity)
}
