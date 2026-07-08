package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.FinanceRepository
import com.example.data.entity.BudgetEntity
import com.example.data.entity.BudgetGoalEntity
import com.example.data.entity.RecurringPaymentEntity
import com.example.data.entity.TransactionEntity
import com.example.util.BiometricHelper
import com.example.util.CurrencyUtil
import com.example.util.GeminiReceiptScanner
import com.example.util.ScannedReceiptResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository
    
    // Core database flows
    val transactions: StateFlow<List<TransactionEntity>>
    val budgets: StateFlow<List<BudgetEntity>>
    val recurringPayments: StateFlow<List<RecurringPaymentEntity>>
    val goals: StateFlow<List<BudgetGoalEntity>>

    // UI States
    private val _selectedCurrency = MutableStateFlow("USD")
    val selectedCurrency: StateFlow<String> = _selectedCurrency.asStateFlow()

    private val _selectedTimePeriod = MutableStateFlow("Month") // "Day", "Week", "Month", "Year"
    val selectedTimePeriod: StateFlow<String> = _selectedTimePeriod.asStateFlow()

    // Google OAuth simulation/real state
    private val _userSession = MutableStateFlow<UserSession?>(null)
    val userSession: StateFlow<UserSession?> = _userSession.asStateFlow()

    // Biometric lock state
    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    // Scanning state
    private val _scanningState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanningState: StateFlow<ScanState> = _scanningState.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FinanceRepository(database.financeDao())

        transactions = repository.allTransactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        budgets = repository.allBudgets.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        recurringPayments = repository.allRecurringPayments.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        goals = repository.allGoals.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Initialize Biometric Lock
        _isAppLocked.value = BiometricHelper.isBiometricEnabled(application)

        // Add pre-populated sample budgets and transactions if empty, to ensure the app looks as stunning as the Dribbble design on first launch!
        viewModelScope.launch {
            repository.allTransactions.first().let { list ->
                if (list.isEmpty()) {
                    prepopulateSampleData()
                }
            }
        }
    }

    fun selectCurrency(currency: String) {
        _selectedCurrency.value = currency
    }

    fun selectTimePeriod(period: String) {
        _selectedTimePeriod.value = period
    }

    // Google OAuth actions
    fun signInWithGoogle(email: String, name: String, avatarUrl: String? = null) {
        viewModelScope.launch {
            _userSession.value = UserSession(email, name, avatarUrl)
        }
    }

    fun signOut() {
        _userSession.value = null
    }

    // Biometric lock actions
    fun toggleBiometricSetting(enabled: Boolean) {
        BiometricHelper.setBiometricEnabled(getApplication(), enabled)
    }

    fun unlockApp() {
        _isAppLocked.value = false
    }

    fun lockApp() {
        _isAppLocked.value = true
    }

    // Transaction actions
    fun addTransaction(
        title: String,
        amount: Double,
        currency: String,
        category: String,
        type: String,
        date: Long,
        notes: String = "",
        isRecurring: Boolean = false
    ) {
        viewModelScope.launch {
            val tx = TransactionEntity(
                title = title,
                amount = amount,
                currency = currency,
                category = category,
                type = type,
                date = date,
                notes = notes,
                isRecurring = isRecurring
            )
            repository.insertTransaction(tx)
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(tx)
        }
    }

    // Budget actions
    fun addBudget(category: String, limit: Double, currency: String) {
        viewModelScope.launch {
            val budget = BudgetEntity(category = category, limitAmount = limit, currency = currency)
            repository.insertBudget(budget)
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }

    // Goal actions
    fun addGoal(title: String, target: Double, current: Double, currency: String, date: Long) {
        viewModelScope.launch {
            val goal = BudgetGoalEntity(
                title = title,
                targetAmount = target,
                currentAmount = current,
                currency = currency,
                targetDate = date
            )
            repository.insertGoal(goal)
        }
    }

    fun updateGoalProgress(goal: BudgetGoalEntity, amountToAdd: Double) {
        viewModelScope.launch {
            val updated = goal.copy(currentAmount = (goal.currentAmount + amountToAdd).coerceAtMost(goal.targetAmount))
            repository.insertGoal(updated)
        }
    }

    fun deleteGoal(goal: BudgetGoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    // Recurring payments actions
    fun addRecurringPayment(title: String, amount: Double, currency: String, dueDate: Long, frequency: String) {
        viewModelScope.launch {
            val payment = RecurringPaymentEntity(
                title = title,
                amount = amount,
                currency = currency,
                dueDate = dueDate,
                frequency = frequency
            )
            repository.insertRecurringPayment(payment)
        }
    }

    fun toggleRecurringPaymentPaid(payment: RecurringPaymentEntity) {
        viewModelScope.launch {
            val updated = payment.copy(isPaid = !payment.isPaid)
            repository.insertRecurringPayment(updated)
        }
    }

    fun deleteRecurringPayment(payment: RecurringPaymentEntity) {
        viewModelScope.launch {
            repository.deleteRecurringPayment(payment)
        }
    }

    // Automated Expense Categorization logic
    fun suggestCategory(title: String): String {
        val cleanTitle = title.lowercase().trim()
        return when {
            cleanTitle.contains("walmart") || cleanTitle.contains("target") || cleanTitle.contains("amazon") || cleanTitle.contains("costco") -> "Shopping"
            cleanTitle.contains("mcdonald") || cleanTitle.contains("starbucks") || cleanTitle.contains("restaurant") || cleanTitle.contains("burger") || cleanTitle.contains("food") || cleanTitle.contains("pizza") -> "Food"
            cleanTitle.contains("uber") || cleanTitle.contains("lyft") || cleanTitle.contains("gas") || cleanTitle.contains("subway") || cleanTitle.contains("metro") || cleanTitle.contains("shell") || cleanTitle.contains("chevron") -> "Transport"
            cleanTitle.contains("netflix") || cleanTitle.contains("spotify") || cleanTitle.contains("hulu") || cleanTitle.contains("cinema") || cleanTitle.contains("disney") || cleanTitle.contains("game") -> "Entertainment"
            cleanTitle.contains("pge") || cleanTitle.contains("power") || cleanTitle.contains("water") || cleanTitle.contains("comcast") || cleanTitle.contains("internet") || cleanTitle.contains("electricity") || cleanTitle.contains("bill") -> "Utilities"
            cleanTitle.contains("salary") || cleanTitle.contains("dividend") || cleanTitle.contains("paycheck") || cleanTitle.contains("stripe") || cleanTitle.contains("bonus") -> "Income"
            else -> "Other"
        }
    }

    // Gemini automated receipt scanning
    fun scanReceiptImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _scanningState.value = ScanState.Scanning
            val result = GeminiReceiptScanner.scanReceipt(bitmap)
            if (result != null) {
                _scanningState.value = ScanState.Success(result)
            } else {
                _scanningState.value = ScanState.Error("Could not scan receipt. Please verify API key or enter details manually.")
            }
        }
    }

    fun clearScanningState() {
        _scanningState.value = ScanState.Idle
    }

    // Prepopulate samples
    private suspend fun prepopulateSampleData() {
        // Sample transactions
        repository.insertTransaction(TransactionEntity(title = "Salary Credited", amount = 8500.00, currency = "USD", category = "Income", type = "INCOME", date = System.currentTimeMillis() - 86400000L * 2, notes = "Monthly base salary"))
        repository.insertTransaction(TransactionEntity(title = "Whole Foods", amount = 142.50, currency = "USD", category = "Food", type = "EXPENSE", date = System.currentTimeMillis() - 86400000L * 1, notes = "Weekly grocery groceries"))
        repository.insertTransaction(TransactionEntity(title = "Uber Ride", amount = 24.80, currency = "USD", category = "Transport", type = "EXPENSE", date = System.currentTimeMillis() - 3600000L * 4, notes = "Ride to office"))
        repository.insertTransaction(TransactionEntity(title = "Netflix Subscription", amount = 15.99, currency = "USD", category = "Entertainment", type = "EXPENSE", date = System.currentTimeMillis() - 3600000L * 12, notes = "Standard premium tier"))
        repository.insertTransaction(TransactionEntity(title = "PG&E Utilities", amount = 112.40, currency = "USD", category = "Utilities", type = "EXPENSE", date = System.currentTimeMillis() - 86400000L * 5, notes = "Electricity and Gas monthly"))
        repository.insertTransaction(TransactionEntity(title = "Freelance UI Design", amount = 1200.00, currency = "USD", category = "Income", type = "INCOME", date = System.currentTimeMillis() - 86400000L * 3, notes = "Landing page deliverable"))
        repository.insertTransaction(TransactionEntity(title = "Apple Store", amount = 899.00, currency = "USD", category = "Shopping", type = "EXPENSE", date = System.currentTimeMillis() - 86400000L * 10, notes = "iPhone upgrade"))

        // Sample Budgets
        repository.insertBudget(BudgetEntity(category = "Food", limitAmount = 800.00, currency = "USD"))
        repository.insertBudget(BudgetEntity(category = "Transport", limitAmount = 300.00, currency = "USD"))
        repository.insertBudget(BudgetEntity(category = "Shopping", limitAmount = 1500.00, currency = "USD"))
        repository.insertBudget(BudgetEntity(category = "Utilities", limitAmount = 400.00, currency = "USD"))
        repository.insertBudget(BudgetEntity(category = "Entertainment", limitAmount = 250.00, currency = "USD"))

        // Sample Goals
        repository.insertGoal(BudgetGoalEntity(title = "New Tesla Car Fund", targetAmount = 45000.0, currentAmount = 12500.0, currency = "USD", targetDate = System.currentTimeMillis() + 86400000L * 365))
        repository.insertGoal(BudgetGoalEntity(title = "Hawaii Summer Vacation", targetAmount = 5000.0, currentAmount = 2300.0, currency = "USD", targetDate = System.currentTimeMillis() + 86400000L * 90))

        // Sample Recurring Payments
        repository.insertRecurringPayment(RecurringPaymentEntity(title = "AWS Server Hosting", amount = 45.0, currency = "USD", dueDate = System.currentTimeMillis() + 86400000L * 4, frequency = "Monthly"))
        repository.insertRecurringPayment(RecurringPaymentEntity(title = "Gym Membership", amount = 60.0, currency = "USD", dueDate = System.currentTimeMillis() + 86400000L * 12, frequency = "Monthly"))
        repository.insertRecurringPayment(RecurringPaymentEntity(title = "Adobe Creative Suite", amount = 54.99, currency = "USD", dueDate = System.currentTimeMillis() + 86400000L * 15, frequency = "Monthly"))
    }
}

data class UserSession(
    val email: String,
    val name: String,
    val avatarUrl: String?
)

sealed interface ScanState {
    object Idle : ScanState
    object Scanning : ScanState
    data class Success(val result: ScannedReceiptResult) : ScanState
    data class Error(val message: String) : ScanState
}
