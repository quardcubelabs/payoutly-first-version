package com.example.data

import com.example.data.dao.FinanceDao
import com.example.data.entity.BudgetEntity
import com.example.data.entity.BudgetGoalEntity
import com.example.data.entity.RecurringPaymentEntity
import com.example.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val financeDao: FinanceDao) {

    // --- Transactions ---
    val allTransactions: Flow<List<TransactionEntity>> = financeDao.getAllTransactionsFlow()

    suspend fun getAllTransactionsList(): List<TransactionEntity> {
        return financeDao.getAllTransactions()
    }

    suspend fun insertTransaction(transaction: TransactionEntity) {
        financeDao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        financeDao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Int) {
        financeDao.deleteTransactionById(id)
    }

    // --- Budgets ---
    val allBudgets: Flow<List<BudgetEntity>> = financeDao.getAllBudgetsFlow()

    suspend fun getAllBudgetsList(): List<BudgetEntity> {
        return financeDao.getAllBudgets()
    }

    suspend fun insertBudget(budget: BudgetEntity) {
        financeDao.insertBudget(budget)
    }

    suspend fun deleteBudget(budget: BudgetEntity) {
        financeDao.deleteBudget(budget)
    }

    suspend fun deleteBudgetById(id: Int) {
        financeDao.deleteBudgetById(id)
    }

    // --- Recurring Payments ---
    val allRecurringPayments: Flow<List<RecurringPaymentEntity>> = financeDao.getAllRecurringPaymentsFlow()

    suspend fun insertRecurringPayment(payment: RecurringPaymentEntity) {
        financeDao.insertRecurringPayment(payment)
    }

    suspend fun deleteRecurringPayment(payment: RecurringPaymentEntity) {
        financeDao.deleteRecurringPayment(payment)
    }

    suspend fun updateRecurringPaymentStatus(id: Int, isPaid: Boolean) {
        financeDao.updateRecurringPaymentStatus(id, isPaid)
    }

    // --- Budget Goals ---
    val allGoals: Flow<List<BudgetGoalEntity>> = financeDao.getAllGoalsFlow()

    suspend fun insertGoal(goal: BudgetGoalEntity) {
        financeDao.insertGoal(goal)
    }

    suspend fun deleteGoal(goal: BudgetGoalEntity) {
        financeDao.deleteGoal(goal)
    }
}
