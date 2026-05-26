package com.example.pointlyaisummary

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
import java.util.UUID
import java.io.File
import java.io.FileOutputStream
import androidx.core.content.edit


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
                MainScreen(deviceUUID, viewModel)
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