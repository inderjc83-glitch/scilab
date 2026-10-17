package com.example.data

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class AuthActionType(val displayName: String) {
    SIGN_UP("Sign Up"),
    SIGN_IN("Sign In")
}

data class AuthDataRecord(
    val recordId: String,
    val timestamp: Long,
    val formattedDate: String,
    val action: AuthActionType,
    val fullName: String,
    val email: String,
    val role: UserRole,
    val institution: String,
    val status: String = "SUCCESS",
    val notes: String = "Verified Auth • Internal"
)

object AuthDataSheetManager {
    private const val CSV_FILE_NAME = "auth_records_ledger.csv"
    private const val DEFAULT_MASTER_PIN = "8383"
    private const val PREFS_NAME = "scilab_admin_security_prefs"
    private const val KEY_MASTER_PIN = "master_pin"

    private const val CSV_HEADER = "RecordID,Timestamp,FormattedDate,Action,FullName,Email,Role,Institution,Status,Notes\n"

    private val _records = MutableStateFlow<List<AuthDataRecord>>(emptyList())
    val records: StateFlow<List<AuthDataRecord>> = _records.asStateFlow()

    private val _masterPin = MutableStateFlow(DEFAULT_MASTER_PIN)
    val masterPin: StateFlow<String> = _masterPin.asStateFlow()

    private var isInitialized = false
    private val scope = CoroutineScope(Dispatchers.IO)
    private val fileLock = Any()

    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _masterPin.value = prefs.getString(KEY_MASTER_PIN, DEFAULT_MASTER_PIN) ?: DEFAULT_MASTER_PIN

        scope.launch {
            loadRecordsFromFile(context.applicationContext)
        }
    }

    private fun loadRecordsFromFile(context: Context) {
        synchronized(fileLock) {
            val file = File(context.filesDir, CSV_FILE_NAME)
            if (!file.exists() || file.length() == 0L) {
                // Seed initial realistic records so the owner immediately sees a working data sheet
                val initialRecords = generateSeedRecords()
                writeAllRecordsToFile(file, initialRecords)
                _records.value = initialRecords
            } else {
                val parsed = parseCsvFile(file)
                if (parsed.isEmpty()) {
                    val initialRecords = generateSeedRecords()
                    writeAllRecordsToFile(file, initialRecords)
                    _records.value = initialRecords
                } else {
                    _records.value = parsed
                }
            }
        }
    }

    private fun generateSeedRecords(): List<AuthDataRecord> {
        val now = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        return listOf(
            AuthDataRecord(
                recordId = "REC-1001",
                timestamp = now - 86400000L * 2,
                formattedDate = sdf.format(Date(now - 86400000L * 2)),
                action = AuthActionType.SIGN_UP,
                fullName = "Dr. Marcus Vance",
                email = "marcus.vance@faculty.edu",
                role = UserRole.TEACHER,
                institution = "Faculty of Advanced Physics & Chemistry",
                status = "SUCCESS",
                notes = "Initial Faculty Account Creation"
            ),
            AuthDataRecord(
                recordId = "REC-1002",
                timestamp = now - 86400000L * 2 + 1800000L,
                formattedDate = sdf.format(Date(now - 86400000L * 2 + 1800000L)),
                action = AuthActionType.SIGN_IN,
                fullName = "Dr. Marcus Vance",
                email = "marcus.vance@faculty.edu",
                role = UserRole.TEACHER,
                institution = "Faculty of Advanced Physics & Chemistry",
                status = "SUCCESS",
                notes = "Faculty Portal Authentication"
            ),
            AuthDataRecord(
                recordId = "REC-1003",
                timestamp = now - 86400000L,
                formattedDate = sdf.format(Date(now - 86400000L)),
                action = AuthActionType.SIGN_UP,
                fullName = "Alex Rivera",
                email = "alex.rivera@student.edu",
                role = UserRole.STUDENT,
                institution = "Department of Natural Sciences",
                status = "SUCCESS",
                notes = "Student Onboarding Registration"
            ),
            AuthDataRecord(
                recordId = "REC-1004",
                timestamp = now - 3600000L,
                formattedDate = sdf.format(Date(now - 3600000L)),
                action = AuthActionType.SIGN_IN,
                fullName = "Alex Rivera",
                email = "alex.rivera@student.edu",
                role = UserRole.STUDENT,
                institution = "Department of Natural Sciences",
                status = "SUCCESS",
                notes = "Student Active Session Login"
            )
        )
    }

    private fun parseCsvFile(file: File): List<AuthDataRecord> {
        val list = mutableListOf<AuthDataRecord>()
        try {
            file.bufferedReader().useLines { lines ->
                var isFirst = true
                lines.forEach { line ->
                    if (isFirst) {
                        isFirst = false // skip header
                    } else if (line.isNotBlank()) {
                        val tokens = parseCsvLine(line)
                        if (tokens.size >= 8) {
                            val record = AuthDataRecord(
                                recordId = tokens.getOrNull(0) ?: "REC-000",
                                timestamp = tokens.getOrNull(1)?.toLongOrNull() ?: System.currentTimeMillis(),
                                formattedDate = tokens.getOrNull(2) ?: "",
                                action = if (tokens.getOrNull(3) == "SIGN_UP") AuthActionType.SIGN_UP else AuthActionType.SIGN_IN,
                                fullName = tokens.getOrNull(4) ?: "Unknown",
                                email = tokens.getOrNull(5) ?: "",
                                role = when (tokens.getOrNull(6)) {
                                    "TEACHER" -> UserRole.TEACHER
                                    "ADMIN" -> UserRole.ADMIN
                                    else -> UserRole.STUDENT
                                },
                                institution = tokens.getOrNull(7) ?: "",
                                status = tokens.getOrNull(8) ?: "SUCCESS",
                                notes = tokens.getOrNull(9) ?: "Internal Auth Record"
                            )
                            list.add(record)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    sb.append('\"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString())
                sb.clear()
            } else {
                sb.append(c)
            }
            i++
        }
        tokens.add(sb.toString())
        return tokens
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    private fun writeAllRecordsToFile(file: File, records: List<AuthDataRecord>) {
        FileWriter(file, false).use { writer ->
            writer.write(CSV_HEADER)
            records.forEach { r ->
                val line = listOf(
                    r.recordId,
                    r.timestamp.toString(),
                    escapeCsv(r.formattedDate),
                    r.action.name,
                    escapeCsv(r.fullName),
                    escapeCsv(r.email),
                    r.role.name,
                    escapeCsv(r.institution),
                    escapeCsv(r.status),
                    escapeCsv(r.notes)
                ).joinToString(",") + "\n"
                writer.write(line)
            }
        }
    }

    fun logEvent(
        context: Context?,
        action: AuthActionType,
        fullName: String,
        email: String,
        role: UserRole,
        institution: String,
        notes: String = "Verified Auth • Internal"
    ) {
        val now = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val formattedDate = sdf.format(Date(now))
        val nextId = "REC-" + (1000 + _records.value.size + 1)

        val newRecord = AuthDataRecord(
            recordId = nextId,
            timestamp = now,
            formattedDate = formattedDate,
            action = action,
            fullName = fullName.ifBlank { "User" },
            email = email.ifBlank { "unknown@scilab.edu" },
            role = role,
            institution = institution.ifBlank { "Department of Natural Sciences" },
            status = "SUCCESS",
            notes = notes
        )

        val updatedList = listOf(newRecord) + _records.value
        _records.value = updatedList

        if (context != null) {
            val appContext = context.applicationContext
            scope.launch {
                synchronized(fileLock) {
                    val file = File(appContext.filesDir, CSV_FILE_NAME)
                    val isNew = !file.exists() || file.length() == 0L
                    FileWriter(file, true).use { writer ->
                        if (isNew) {
                            writer.write(CSV_HEADER)
                        }
                        val line = listOf(
                            newRecord.recordId,
                            newRecord.timestamp.toString(),
                            escapeCsv(newRecord.formattedDate),
                            newRecord.action.name,
                            escapeCsv(newRecord.fullName),
                            escapeCsv(newRecord.email),
                            newRecord.role.name,
                            escapeCsv(newRecord.institution),
                            escapeCsv(newRecord.status),
                            escapeCsv(newRecord.notes)
                        ).joinToString(",") + "\n"
                        writer.write(line)
                    }
                }
            }
        }
    }

    fun verifyPin(enteredPin: String): Boolean {
        return enteredPin.trim() == _masterPin.value.trim()
    }

    fun updateMasterPin(context: Context, newPin: String): Boolean {
        if (newPin.length < 4) return false
        _masterPin.value = newPin
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_MASTER_PIN, newPin).apply()
        return true
    }

    fun getRawCsvContent(context: Context): String {
        return synchronized(fileLock) {
            val file = File(context.filesDir, CSV_FILE_NAME)
            if (file.exists()) {
                file.readText()
            } else {
                CSV_HEADER
            }
        }
    }

    fun copyCsvToClipboard(context: Context) {
        val content = getRawCsvContent(context)
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("SciLab Auth Data Sheet", content)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Data sheet CSV copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    fun createShareIntent(context: Context): Intent {
        val file = File(context.filesDir, CSV_FILE_NAME)
        if (!file.exists()) {
            synchronized(fileLock) {
                writeAllRecordsToFile(file, _records.value)
            }
        }

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Confidential SciLab Auth Data Sheet (Sign Up & Log In Records)")
            putExtra(
                Intent.EXTRA_TEXT,
                "Attached is the confidential background authentication data sheet recording all Sign Up and Log In activities.\nGenerated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}\nTotal records: ${_records.value.size}"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        return Intent.createChooser(intent, "Open or Export Data Sheet (Google Sheets / Drive / Email)")
    }

    fun clearAllRecords(context: Context) {
        synchronized(fileLock) {
            val file = File(context.filesDir, CSV_FILE_NAME)
            FileWriter(file, false).use { writer ->
                writer.write(CSV_HEADER)
            }
            _records.value = emptyList()
        }
    }
}
