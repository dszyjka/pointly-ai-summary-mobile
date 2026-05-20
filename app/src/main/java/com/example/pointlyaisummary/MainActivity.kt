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
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pointlyaisummary.ui.theme.BackgroundGray
import com.example.pointlyaisummary.ui.theme.LightPurpleBg
import com.example.pointlyaisummary.ui.theme.MainPurple
import com.example.pointlyaisummary.ui.theme.PointlyAISummaryTheme
import com.example.pointlyaisummary.ui.theme.TextGray
import androidx.compose.material3.Button
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import android.content.Context
import android.provider.OpenableColumns
import android.database.Cursor
import androidx.compose.ui.platform.LocalContext


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
    data class FileDetails(val fileName : String) : Screen()
}

data class NavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val screen: Screen
)

@Composable
fun MainScreen() {
    val context: Context = LocalContext.current
    var currScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            currScreen = Screen.Summarization(fileName = getFileName(context, uri))
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

@Composable
fun SummaryScreen(fileName: String, onBackClick: () -> Unit) {
    var selectedType by remember { mutableStateOf("Standard") }
    var instructionText by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.background(Color.White, CircleShape)
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Red.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text("PDF", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(fileName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = LightPurpleBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Your Summary ✨", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Button(
                        onClick = { /* Kopiowanie */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = MainPurple
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Copy", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Raport przedstawia wyniki finansowe firmy za rok 2023. Najważniejsze informacje:\n\n" +
                            "• Przychody wzrosły o 18% w porównaniu do roku 2022.\n" +
                            "• Zwiększyła się marża EBITDA, co wskazuje na poprawę rentowności operacyjnej.\n" +
                            "• Głównym ryzykiem pozostaje inflacja oraz koszty logistyki.\n" +
                            "• Firma planuje ekspansję na rynki europejskie w 2024 roku.",
                    fontSize = 15.sp,
                    color = Color.DarkGray,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* Jeszcze raz */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MainPurple)
                    ) {
                        Text("Generate Again", fontSize = 12.sp)
                    }
                }
            }
        }

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Response Type", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val types = listOf(
                    "Standard",
                    "TL;DR",
                    "Bullet Points",
                    "Scientific",
                    "Technical",
                    "Business",
                    "Academic"
                )

                items(types) { type ->
                    val isSelected = selectedType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedType = type },
                        label = { Text(type) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MainPurple,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = TextGray
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) MainPurple else Color.LightGray
                        )
                    )
                }
            }
        }

        Column {
            Text("Your instructions (optional)", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = instructionText,
                onValueChange = { if (it.length <= 500) instructionText = it },
                placeholder = { Text("Add your own rules...", color = TextGray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = MainPurple
                )
            )

            Text(
                text = "${instructionText.length}/500",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                textAlign = TextAlign.End,
                color = TextGray,
                fontSize = 12.sp
            )
        }

        Button(
            onClick = { /* generowanie */ },
            colors = ButtonDefaults.buttonColors(containerColor = MainPurple),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("✨ Generate Summary", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))

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