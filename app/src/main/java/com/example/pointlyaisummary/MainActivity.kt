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
import android.widget.Button
import android.widget.Space
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pointlyaisummary.ui.theme.BackgroundGray
import com.example.pointlyaisummary.ui.theme.LightPurpleBg
import com.example.pointlyaisummary.ui.theme.MainPurple
import com.example.pointlyaisummary.ui.theme.PointlyAISummaryTheme
import com.example.pointlyaisummary.ui.theme.TextGray
import androidx.compose.material3.Button


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PointlyAISummaryTheme {
                MainScreen()
            }
        }
    }
}

sealed class  Screen {
    data object Home : Screen()
    data class History(val userId: String) : Screen()
    data class Summarization(val fileName: String)  : Screen()
}

data class NavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val screen: Screen
)

@Composable
fun MainScreen() {
    var currScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            currScreen = Screen.Summarization(fileName = uri.toString())
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
            when (currScreen) {
                is Screen.Home -> {
                    HomeScreen(onFilePickRequested = {
                        filePickerLauncher.launch(
                            arrayOf(
                                "application/pdf",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                "application/txt"
                            )
                        )
                    })
                }
                is Screen.History -> {
                    Text(text = "Tutaj będzie historia użytkownika.")
                }
                is Screen.Summarization -> {
                    Text(text = "Tu będzie streszczanie")
                }
            }
        }
    }
}

@Composable
fun HomeScreen(onFilePickRequested: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background((BackgroundGray))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Summarize quickly",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MainPurple
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Choose a file to summarize",
            fontSize = 16.sp,
            color = TextGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 48.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        UploadZone(onUploadClick = onFilePickRequested)
    }
}

@Composable
fun UploadZone(onUploadClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(LightPurpleBg)
            .border(
                width = 2.dp,
                color = MainPurple.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.AddCircle,
                "Add file",
                modifier = Modifier.size(64.dp),
                tint = MainPurple
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Acceptable files: PDF, DOCX, TXT",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = MainPurple
            )

            Spacer(modifier= Modifier.height(24.dp))

            Button(
                onClick = onUploadClick,
                colors = ButtonDefaults.buttonColors(containerColor = MainPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Browse files",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 16.sp
                )
            }
        }
    }
}
