package com.mychatbot.aichatbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Palet warna hitam-putih (monokrom)
private val BgDark = Color(0xFF000000)
private val BubbleAssistant = Color(0xFF1C1C1C)
private val AccentWhite = Color(0xFFFFFFFF)
private val ChipBg = Color(0xFF1A1A1A)
private val TextGray = Color(0xFF9E9E9E)
private val BorderGray = Color(0xFF333333)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = BgDark,
                    surface = BgDark,
                    primary = AccentWhite
                )
            ) {
                ChatbotApp()
            }
        }
    }
}

private val quickReplies = listOf(
    "⏰ Jam berapa" to "Sekarang jam berapa?",
    "💡 Kasih ide" to "Kasih aku ide dong",
    "📝 Bantu nulis" to "Bantu aku nulis sesuatu",
    "😄 Lelucon" to "Kasih aku lelucon lucu"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val settings = remember { SettingsStore(context) }
    val apiClient = remember { ClaudeApiClient() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Kontrol animasi masuk aplikasi
    var showSplashLogo by remember { mutableStateOf(true) }
    var showChatContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1200)          // logo tampil sebentar
        showSplashLogo = false // logo mulai memudar
        delay(200)
        showChatContent = true // tampilan chat mulai muncul (geser + fade)
    }

    var messages by remember {
        mutableStateOf(
            listOf(ChatMessage("assistant", "Halo! Aku Alpha. Suruh apa aja, gas langsung jawab cepet. Coba pencet salah satu tombol di bawah, atau ketik sendiri."))
        )
    }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    fun sendMessage(overrideText: String? = null) {
        val text = (overrideText ?: inputText).trim()
        if (text.isEmpty() || isLoading) return
        if (settings.apiKey.isBlank()) {
            errorText = "Masukkan API Key dulu di Pengaturan (ikon ⚙️ di kanan atas)."
            return
        }

        val newHistory = messages + ChatMessage("user", text)
        messages = newHistory
        inputText = ""
        isLoading = true
        errorText = null

        scope.launch {
            try {
                val reply = withContext(Dispatchers.IO) {
                    apiClient.sendMessage(settings.apiKey, settings.systemPrompt, newHistory)
                }
                messages = newHistory + ChatMessage("assistant", reply)
            } catch (e: Exception) {
                errorText = e.message ?: "Terjadi kesalahan tak terduga."
            } finally {
                isLoading = false
            }
        }
    }

    if (showSettings) {
        SettingsScreen(
            settings = settings,
            onClose = { showSettings = false }
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {

        // Konten chat utama - muncul dengan fade + geser dari atas/bawah
        AnimatedVisibility(
            visible = showChatContent,
            enter = fadeIn(animationSpec = tween(500))
        ) {
            Scaffold(
                containerColor = BgDark,
                topBar = {
                    AnimatedVisibility(
                        visible = showChatContent,
                        enter = slideInVertically(
                            initialOffsetY = { -it },
                            animationSpec = tween(500)
                        ) + fadeIn(animationSpec = tween(500))
                    ) {
                        AlphaHeader(onSettingsClick = { showSettings = true })
                    }
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BgDark)
                        .padding(padding)
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        items(messages) { msg ->
                            MessageBubble(msg)
                        }
                        if (isLoading) {
                            item {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text("Alpha sedang mengetik...", color = TextGray, modifier = Modifier.padding(8.dp))
                                }
                            }
                        }
                        errorText?.let { err ->
                            item {
                                Text(
                                    "⚠️ $err",
                                    color = Color.White,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF262626), RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                                )
                            }
                        }
                    }

                    LaunchedEffect(messages.size, isLoading) {
                        if (messages.isNotEmpty()) {
                            listState.animateScrollToItem(messages.size - 1 + if (isLoading) 1 else 0)
                        }
                    }

                    AnimatedVisibility(
                        visible = showChatContent,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(500)
                        ) + fadeIn(animationSpec = tween(500))
                    ) {
                        Column {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                items(quickReplies) { pair ->
                                    QuickReplyChip(label = pair.first) { sendMessage(pair.second) }
                                }
                            }

                            Divider(color = BorderGray, thickness = 1.dp)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = inputText,
                                    onValueChange = { inputText = it },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("Suruh Alpha ngapain...", color = TextGray) },
                                    shape = RoundedCornerShape(28.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = ChipBg,
                                        unfocusedContainerColor = ChipBg,
                                        focusedBorderColor = AccentWhite,
                                        unfocusedBorderColor = BorderGray,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        cursorColor = AccentWhite
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(AccentWhite)
                                        .clickable(enabled = !isLoading) { sendMessage() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Send, contentDescription = "Kirim", tint = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Logo splash - di atas semua, memudar duluan
        AnimatedVisibility(
            visible = showSplashLogo,
            exit = fadeOut(animationSpec = tween(400))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgDark),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(AccentWhite),
                    contentAlignment = Alignment.Center
                ) {
                    Text("A", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 52.sp)
                }
            }
        }
    }
}

@Composable
fun AlphaHeader(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgDark)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(AccentWhite),
            contentAlignment = Alignment.Center
        ) {
            Text("A", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Alpha", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("online • fast respon", color = TextGray, fontSize = 12.sp)
        }
        IconButton(onClick = onSettingsClick) {
            Icon(Icons.Filled.Settings, contentDescription = "Pengaturan", tint = TextGray)
        }
    }
}

@Composable
fun QuickReplyChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(ChipBg)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(label, color = Color.White, fontSize = 13.sp)
    }
}

@Composable
fun MessageBubble(msg: ChatMessage) {
    val isUser = msg.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (isUser) AccentWhite else BubbleAssistant,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = msg.text,
                color = if (isUser) Color.Black else Color.White
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(settings: SettingsStore, onClose: () -> Unit) {
    var apiKey by remember { mutableStateOf(settings.apiKey) }
    var systemPrompt by remember { mutableStateOf(settings.systemPrompt) }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = { Text("⚙️ Pengaturan", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgDark)
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("API Key Anthropic", fontWeight = FontWeight.Bold, color = Color.White)
            Text(
                "Dapatkan gratis di console.anthropic.com. Disimpan hanya di HP ini.",
                color = TextGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("sk-ant-...") },
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ChipBg,
                    unfocusedContainerColor = ChipBg,
                    focusedBorderColor = AccentWhite,
                    unfocusedBorderColor = BorderGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text("Instruksi AI (System Prompt)", fontWeight = FontWeight.Bold, color = Color.White)
            Text(
                "Ubah ini untuk mengatur perilaku/kepribadian Alpha.",
                color = TextGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = systemPrompt,
                onValueChange = { systemPrompt = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ChipBg,
                    unfocusedContainerColor = ChipBg,
                    focusedBorderColor = AccentWhite,
                    unfocusedBorderColor = BorderGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row {
                Button(
                    onClick = {
                        settings.apiKey = apiKey
                        settings.systemPrompt = systemPrompt
                        onClose()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentWhite, contentColor = Color.Black)
                ) {
                    Text("💾 Simpan")
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = onClose,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Batal", color = Color.White)
                }
            }
        }
    }
}
