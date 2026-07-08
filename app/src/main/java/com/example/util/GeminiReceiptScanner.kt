package com.example.util

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object GeminiReceiptScanner {
    private const val TAG = "ReceiptScanner"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun scanReceipt(bitmap: Bitmap): ScannedReceiptResult? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API key is not configured.")
            return@withContext null
        }

        // Convert bitmap to Base64
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

        // Construct request JSON
        val prompt = """
            You are a receipt scanning expert. Analyze the receipt image.
            Extract and return a JSON object with:
            "title" (string, store or merchant name),
            "amount" (number, total cost),
            "category" (string, choose strictly one from: Food, Shopping, Transport, Utilities, Entertainment, Other),
            "notes" (string, quick summary of items).
            Return ONLY raw JSON, do not wrap in markdown or backticks.
        """.trimIndent()

        try {
            val inlineData = JSONObject().apply {
                put("mimeType", "image/jpeg")
                put("data", base64Image)
            }

            val textPart = JSONObject().apply {
                put("text", prompt)
            }

            val imagePart = JSONObject().apply {
                put("inlineData", inlineData)
            }

            val parts = JSONArray().apply {
                put(textPart)
                put(imagePart)
            }

            val content = JSONObject().apply {
                put("parts", parts)
            }

            val contents = JSONArray().apply {
                put(content)
            }

            // Specify JSON output format
            val responseFormatText = JSONObject().apply {
                put("mimeType", "application/json")
            }
            val responseFormat = JSONObject().apply {
                put("text", responseFormatText)
            }
            val generationConfig = JSONObject().apply {
                put("responseFormat", responseFormat)
            }

            val requestJson = JSONObject().apply {
                put("contents", contents)
                put("generationConfig", generationConfig)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "API Call Failed: ${response.code} ${response.message}")
                    return@withContext null
                }

                val responseBodyStr = response.body?.string() ?: return@withContext null
                Log.d(TAG, "API Response: ${responseBodyStr.take(500)}")

                val root = JSONObject(responseBodyStr)
                val candidates = root.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val responseContent = firstCandidate?.optJSONObject("content")
                val responseParts = responseContent?.optJSONArray("parts")
                val firstPart = responseParts?.optJSONObject(0)
                val responseText = firstPart?.optString("text") ?: return@withContext null

                val cleanedJsonStr = responseText.trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                val resultJson = JSONObject(cleanedJsonStr)
                return@withContext ScannedReceiptResult(
                    title = resultJson.optString("title", "Scanned Merchant"),
                    amount = resultJson.optDouble("amount", 0.0),
                    category = resultJson.optString("category", "Other"),
                    notes = resultJson.optString("notes", "")
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning receipt", e)
            return@withContext null
        }
    }
}

data class ScannedReceiptResult(
    val title: String,
    val amount: Double,
    val category: String,
    val notes: String
)
