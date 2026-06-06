package com.example.pointlyaisummary

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.vector.ImageVector
import android.net.Uri
import com.example.pointlyaisummary.ui.theme.PointlyAISummaryTheme
import android.content.Context
import android.provider.OpenableColumns
import android.database.Cursor
import android.content.SharedPreferences
import android.os.Build
import android.widget.Toast
import java.util.UUID
import java.io.File
import java.io.FileOutputStream
import androidx.core.content.edit
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutionException


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val uuidManager = UUIDManager(this)
        val deviceUUID: String = uuidManager.getDeviceUUID()
        val repo = SummaryRepository(deviceUUID)
        val viewModel = SummaryViewModel(repo)

        setContent {
            PointlyAISummaryTheme {
                MainScreen(viewModel)
            }
        }
    }
}

data class NavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val screen: Screen
)

class UUIDManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getDeviceUUID(): String {
        var uuid: String? = prefs.getString(KEY_UUID, null)

        if (uuid == null) {
            uuid = UUID.randomUUID().toString()
            prefs.edit { putString(KEY_UUID, uuid) }
        }

        return uuid
    }

    companion object {
        private const val PREFS_NAME = "AppPreferences"
        private const val KEY_UUID = "device_uuid"
    }
}

fun getFileName(context: Context, uri: Uri) : String {
    var result: String? = null

    if (uri.scheme == "content") {
        val cursor: Cursor?  = context.contentResolver.query(uri, null, null, null, null)

        cursor.use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)

                if (index != -1) {
                    result = cursor.getString(index)
                }
            }
        }
    }

    if (result == null) {
        result = uri.path
        val cut: Int = result?.lastIndexOf('/') ?: -1

        if (cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result ?: "Unknown file"
}

fun uriToFile(context: Context, uri: Uri, fileName: String): File? {
    val file = File(context.cacheDir, fileName)
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun formatServerDate(rawDate: String): String {
    return try {
        val cleanDate = rawDate.replace("T", " ").substringBefore(".")

        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

        val date = inputFormat.parse(cleanDate)

        if (date != null) outputFormat.format(date) else rawDate
    } catch (e: ExecutionException) {
        rawDate
    }
}

fun copyToClipboard(context: Context, textToCopy: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Copied text", textToCopy)
    clipboard.setPrimaryClip(clip)

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        Toast.makeText(context, "Copied text to clipboard", Toast.LENGTH_SHORT).show()
    }
}

fun createCorrectFileName(currName: String): String {
    return if (currName.endsWith(".pdf", ignoreCase = true)) {
        currName
    } else {
        val baseName = currName.substringBeforeLast(".")
        "${baseName}.pdf"
    }
}