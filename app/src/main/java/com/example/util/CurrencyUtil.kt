package com.example.util

object CurrencyUtil {
    // Basic standard exchange rates relative to USD (baseline USD = 1.0)
    val exchangeRates = mapOf(
        "USD" to 1.0,
        "EUR" to 0.92,
        "GBP" to 0.78,
        "JPY" to 158.50,
        "CAD" to 1.37
    )

    fun getSymbol(currency: String): String {
        return when (currency) {
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "JPY" -> "¥"
            "CAD" -> "C$"
            else -> "$"
        }
    }

    // Convert an amount from one currency to another
    fun convert(amount: Double, from: String, to: String): Double {
        val fromRate = exchangeRates[from] ?: 1.0
        val toRate = exchangeRates[to] ?: 1.0
        val amountInUSD = amount / fromRate
        return amountInUSD * toRate
    }

    // Format utility with symbol
    fun format(amount: Double, currency: String): String {
        val symbol = getSymbol(currency)
        return if (currency == "JPY") {
            String.format("%s%,.0f", symbol, amount)
        } else {
            String.format("%s%,.2f", symbol, amount)
        }
    }
}
