package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.entity.TransactionEntity
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExportUtil {

    fun generateCsvString(transactions: List<TransactionEntity>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val csv = StringBuilder()
        // Header
        csv.append("Transaction ID,Date,Merchant/Title,Amount,Currency,Type,Category,Notes,Is Recurring\n")
        
        for (tx in transactions) {
            val dateStr = dateFormat.format(Date(tx.date))
            val titleEscaped = tx.title.replace("\"", "\"\"")
            val notesEscaped = tx.notes.replace("\"", "\"\"")
            csv.append("${tx.id},")
                .append("\"$dateStr\",")
                .append("\"$titleEscaped\",")
                .append("${tx.amount},")
                .append("${tx.currency},")
                .append("${tx.type},")
                .append("\"${tx.category}\",")
                .append("\"$notesEscaped\",")
                .append("${tx.isRecurring}\n")
        }
        return csv.toString()
    }

    fun shareCsvFile(context: Context, transactions: List<TransactionEntity>) {
        try {
            val csvContent = generateCsvString(transactions)
            val tempFile = File(context.cacheDir, "payoutly_tax_report.csv")
            FileWriter(tempFile).use { writer ->
                writer.write(csvContent)
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "Payoutly Financial Report")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Export Tax Report (CSV)"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
