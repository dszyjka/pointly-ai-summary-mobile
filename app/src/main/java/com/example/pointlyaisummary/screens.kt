package com.example.pointlyaisummary

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pointlyaisummary.ui.theme.BackgroundGray
import com.example.pointlyaisummary.ui.theme.LightPurpleBg
import com.example.pointlyaisummary.ui.theme.MainPurple
import com.example.pointlyaisummary.ui.theme.TextGray
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.style.TextOverflow
import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.material.icons.rounded.Description
import kotlin.io.use


sealed class Screen {
    data object Home : Screen()
    data class History(val userId: String) : Screen()
    data class Summarization(val fileName: String, val fileUri: Uri)  : Screen()
    data class FileDetails(val fileName : String) : Screen()
}

@Composable
fun MainScreen(viewModel: SummaryViewModel) {
    val context = LocalContext.current
    var currScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            currScreen = Screen.Summarization(fileName = getFileName(context, uri), fileUri = uri)
        }
    }

    val items = listOf(
        NavigationItem("Home", Icons.Filled.Home, Icons.Outlined.Home, Screen.Home),
        NavigationItem("History", Icons.Filled.History, Icons.Outlined.History, Screen.History("user123"))
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
                    HistoryScreen(viewModel = viewModel)
                }
                is Screen.Summarization -> {
                    SummaryScreen(
                        fileName = screen.fileName,
                        fileUri = screen.fileUri,
                        viewModel = viewModel.also { it.clearSummaryText() },
                        context = context,
                        onFilePickRequested = {
                            filePickerLauncher.launch(
                                arrayOf(
                                    "application/pdf",
                                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                    "text/plain"
                                )
                            )
                        }) {
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
                imageVector = Icons.Rounded.UploadFile,
                "Add file",
                modifier = Modifier.size(96.dp),
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
fun SummaryScreen(fileName: String,
                  fileUri: Uri,
                  viewModel: SummaryViewModel,
                  context: Context,
                  onFilePickRequested: () -> Unit,
                  onBackClick: () -> Unit
) {

    var selectedType by remember { mutableStateOf("Standard") }
    var instructionText by remember { mutableStateOf(" ") }
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MainPurple.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Description,
                    "File icon",
                    modifier = Modifier.size(40.dp),
                    tint = MainPurple
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(fileName, fontWeight = FontWeight.Bold, fontSize = 25.sp)
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
                        onClick = { copyToClipboard(context, viewModel.summaryText) },
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
                    text = viewModel.summaryText.ifEmpty { "Click Generate Summary to start..." },
                    fontSize = 15.sp,
                    color = Color.DarkGray,
                    lineHeight = 22.sp
                )

                if (viewModel.isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "⏳ Analysing File...",
                        color = MainPurple,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (viewModel.errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Error: ${viewModel.errorMessage}",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onFilePickRequested,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MainPurple)
                    ) {
                        Text("Change Your File", fontSize = 12.sp)
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
                    "Paragraph",
                    "TL;DR",
                    "Bullet Points",
                    "Q&A",
                    "Executive Summary",
                    "Key Metrics",
                    "Action Items",
                    "Explanation",
                    "Business Report"
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
            onClick = {
                val file = uriToFile(context, fileUri, fileName)

                if (file != null) {
                    viewModel.summarizeUploadedFile(
                        file,
                        selectedType,
                        instructionText.ifEmpty { " " }
                    )
                } else {
                    Toast.makeText(context, "Couldn't load your file", Toast.LENGTH_SHORT).show()
                }
            },
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

@Composable
fun HistoryScreen(viewModel: SummaryViewModel) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadUserHistory()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Previous summaries",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MainPurple
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { query ->
                searchQuery = query
                if (query.isEmpty()) viewModel.loadUserHistory()
                else viewModel.searchUserFile(query)
            },
            placeholder = { Text("Search by name...", color = TextGray) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = MainPurple
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = MainPurple
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.historyList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (searchQuery.isEmpty()) "No summaries yet." else "No results found.",
                    color = TextGray,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(viewModel.historyList) { summary ->
                    SummaryHistoryItem(summary = summary, context = context, viewModel)
                }
            }
        }
    }
}

@Composable
fun SummaryHistoryItem(summary: Summary, context: Context, viewModel: SummaryViewModel) {
    val createFileLauncher = rememberLauncherForActivityResult(
        contract = CreateDocument("application/pdf")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val pdfDocument = PdfDocument()

                    val pageWidth = 595
                    val pageHeight = 842
                    val marginX = 40f
                    val marginY = 50f

                    val textWidth = (pageWidth - 2 * marginX).toInt()
                    val availableHeight = (pageHeight - 2 * marginY).toInt()

                    val textPaint = TextPaint().apply {
                        color = Color.Black.hashCode()
                        textSize = 14f
                        isAntiAlias = true
                    }

                    val staticLayout = StaticLayout.Builder.obtain(
                        summary.summary, 0, summary.summary.length, textPaint, textWidth
                    )
                        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                        .setLineSpacing(0f, 1.2f)
                        .build()

                    var currLine = 0
                    val totalLines  = staticLayout.lineCount
                    var pageNum = 1

                    while (currLine < totalLines) {
                        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
                        val page = pdfDocument.startPage(pageInfo)
                        val canvas = page.canvas

                        canvas.save()
                        canvas.translate(marginX, marginY)

                        val startY = staticLayout.getLineTop(currLine)
                        var linesOnPage = 0

                        while (currLine + linesOnPage < totalLines) {
                            val nextLineBottom = staticLayout.getLineBottom(currLine + linesOnPage)

                            if (nextLineBottom - startY > availableHeight) {
                                break
                            }

                            linesOnPage++
                        }

                        if (linesOnPage == 0) linesOnPage = 1

                        val endY = staticLayout.getLineBottom(currLine + linesOnPage - 1)

                        canvas.clipRect(0, 0, textWidth, endY - startY)
                        canvas.translate(0f, -startY.toFloat())

                        staticLayout.draw((canvas))
                        canvas.restore()

                        pdfDocument.finishPage(page)

                        currLine += linesOnPage
                        pageNum++
                    }

                    pdfDocument.writeTo(outputStream)
                    pdfDocument.close()

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(LightPurpleBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Description,
                    contentDescription = null,
                    tint = MainPurple,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = summary.fileName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatServerDate(summary.createdAt),
                    fontSize = 12.sp,
                    color = TextGray
                )
            }

            Row {
                IconButton(onClick = { createFileLauncher.launch(summary.fileName) }) {
                    Icon(
                        imageVector = Icons.Filled.Download,
                        contentDescription = "Save",
                        tint = TextGray
                    )
                }

                IconButton(onClick = { viewModel.deleteUserSummary(summary.id) }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                    )
                }
            }
        }
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