package com.antigravity.telemetry.core.telemetry

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.ArrayDeque
import java.util.Date
import java.util.Locale

object AutoTelemetryLogger {

    private const val LOG_DIR_NAME = "logs"
    private const val LOG_FILE_NAME = "auto_telemetry_stream.log"
    private const val MAX_RECENT_LOGS = 100

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    private val recentLogsQueue = ArrayDeque<String>(MAX_RECENT_LOGS)
    private val scope = CoroutineScope(Dispatchers.IO)
    private val lock = Any()

    @Volatile
    private var logFile: File? = null

    fun initialize(context: Context) {
        synchronized(lock) {
            val dir = File(context.filesDir, LOG_DIR_NAME)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            logFile = File(dir, LOG_FILE_NAME)
        }
        log("INIT", "AutoTelemetryLogger initialized. Ready to record telemetry stream.")
    }

    fun log(tag: String, message: String, isError: Boolean = false) {
        val timestamp = synchronized(dateFormat) { dateFormat.format(Date()) }
        val prefix = if (isError) "[ERROR]" else "[INFO]"
        val formattedLine = "$timestamp $prefix [$tag] $message"

        synchronized(recentLogsQueue) {
            if (recentLogsQueue.size >= MAX_RECENT_LOGS) {
                recentLogsQueue.pollFirst()
            }
            recentLogsQueue.offerLast(formattedLine)
        }

        val targetFile = logFile
        if (targetFile != null) {
            scope.launch {
                try {
                    synchronized(lock) {
                        PrintWriter(FileWriter(targetFile, true)).use { out ->
                            out.println(formattedLine)
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun getRecentLogs(): List<String> {
        return synchronized(recentLogsQueue) {
            recentLogsQueue.toList()
        }
    }

    fun getLogFile(context: Context): File {
        return logFile ?: run {
            val dir = File(context.filesDir, LOG_DIR_NAME).apply { if (!exists()) mkdirs() }
            val file = File(dir, LOG_FILE_NAME)
            logFile = file
            file
        }
    }

    fun getLogSizeBytes(context: Context): Long {
        val file = getLogFile(context)
        return if (file.exists()) file.length() else 0L
    }

    fun getLogSizeFormatted(context: Context): String {
        val bytes = getLogSizeBytes(context)
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
            else -> String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
        }
    }

    fun clearLog(context: Context) {
        synchronized(lock) {
            val file = getLogFile(context)
            if (file.exists()) {
                file.delete()
                file.createNewFile()
            }
            synchronized(recentLogsQueue) {
                recentLogsQueue.clear()
            }
        }
        log("SYSTEM", "Telemetry log reset by user.")
    }

    fun shareLogFile(context: Context) {
        val file = getLogFile(context)
        if (!file.exists() || file.length() == 0L) {
            // Write a header entry if file is empty
            log("INFO", "Telemetry log shared at ${dateFormat.format(Date())}")
        }

        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "Vehicle Telemetry Android Auto Stream Log [${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())}]"
                )
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Attached is the real-time vehicle telemetry stream log captured from Android Auto for analysis.\nFile size: ${getLogSizeFormatted(context)}"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Email / Share Telemetry Log").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            log("SHARE_ERROR", "Failed to share log file: ${e.message}", isError = true)
        }
    }
}
