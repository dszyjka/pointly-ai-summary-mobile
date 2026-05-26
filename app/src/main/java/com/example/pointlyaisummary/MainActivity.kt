package com.example.pointlyaisummary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import android.net.Uri
import androidx.compose.ui.Alignment
import com.example.pointlyaisummary.ui.theme.PointlyAISummaryTheme
import android.content.Context
import android.provider.OpenableColumns
import android.database.Cursor
import androidx.compose.ui.platform.LocalContext
import android.content.SharedPreferences
import java.util.UUID
import java.io.File
import java.io.FileOutputStream


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PointlyAISummaryTheme {
                MainScreen()
            }
        }

        val uuidManager: UUIDManager = UUIDManager(this)
        val deviceUUID: String = uuidManager.getDeviceUUID()
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
            prefs.edit().putString(KEY_UUID, uuid).apply()
        }

        return uuid
    }

    companion object {
        private const val PREFS_NAME = "AppPreferences"
        private const val KEY_UUID = "device_uuid"
    }
}

@Composable
fun MainScreen() {
    val context: Context = LocalContext.current
    var currScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            currScreen = Screen.Summarization(fileName = getFileName(context, uri), uri)
        }
    }

    val items = listOf(
        NavigationItem("Home", Icons.Filled.Home, Icons.Outlined.Home, Screen.Home),
        NavigationItem("History", Icons.Filled.Menu, Icons.Outlined.Menu, Screen.History("user123"))
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    val isSelected = currScreen == item.screen

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currScreen = item.screen },
                        label = { Text(item.title) },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (val screen = currScreen) {
                is Screen.Home -> {
                    HomeScreen(onFilePickRequested = {
                        filePickerLauncher.launch(
                            arrayOf(
                                "application/pdf",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                "text/plain"
                            )
                        )
                    })
                }
                is Screen.History -> {
                    Text(text = "Tutaj będzie historia użytkownika.")
                }
                is Screen.Summarization -> {
                    SummaryScreen(fileName = screen.fileName) {
                        currScreen = Screen.Home
                    }
                }
                is Screen.FileDetails -> {
                    Text(text = "File details")
                }
            }
        }
    }
}

fun getFileName(context: Context, uri: Uri) : String {
    var result: String? = null

    if (uri.scheme == "content") {
        val cursor: Cursor?  = context.contentResolver.query(uri, null, null, null, null)

        try {
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)

                if (index != -1) {
                    result = cursor.getString(index)
                }
            }
        } finally {
            cursor?.close()
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