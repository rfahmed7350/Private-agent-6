package com.yesmin.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val Night = Color(0xFF090B16)
private val Panel = Color(0xFF15192B)
private val Cyan = Color(0xFF6CEBFF)
private val Violet = Color(0xFF9D8CFF)
private val Pink = Color(0xFFFF8BCB)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("yesmin_settings", MODE_PRIVATE)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                background = Night, surface = Panel, primary = Cyan,
                secondary = Violet, tertiary = Pink
            )) {
                YesminApp(
                    initialKey = prefs.getString("gemini_api_key", "") ?: "",
                    onSaveKey = { prefs.edit().putString("gemini_api_key", it).apply() }
                )
            }
        }
    }
}

@Composable
private fun YesminApp(initialKey: String, onSaveKey: (String) -> Unit) {
    var apiKey by remember { mutableStateOf(initialKey) }
    var showSettings by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Ready when you are") }
    val messages = remember { mutableStateListOf("Yesmin" to "Hi! I'm Yesmin AI, your AI companion. 😊\nAaj kya baat karein?") }
    val scope = rememberCoroutineScope()

    Surface(Modifier.fillMaxSize(), color = Night) {
        Column(Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF14132B), Night, Color(0xFF08151F)))
        ).padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.size(48.dp).background(Brush.linearGradient(listOf(Violet, Cyan)), CircleShape), contentAlignment = Alignment.Center) {
                    Text("Y", color = Night, fontSize = 25.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("YESMIN AI", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text("Your AI companion • Hindi · Hinglish · English", color = Cyan, fontSize = 11.sp)
                }
                TextButton(onClick = { showSettings = true }) { Text("Settings", color = Violet) }
            }
            Spacer(Modifier.height(22.dp))
            Box(Modifier.fillMaxWidth().background(Panel.copy(alpha = .8f), RoundedCornerShape(24.dp)).padding(18.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(92.dp).background(Brush.linearGradient(listOf(Violet.copy(alpha=.8f), Cyan.copy(alpha=.6f))), CircleShape), contentAlignment = Alignment.Center) {
                        Text("Y", fontSize = 42.sp, color = Color.White, fontWeight = FontWeight.Light)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("I'm here for you", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Text(status, color = Cyan, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("CONVERSATION", color = Violet, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))
            LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(messages) { (speaker, text) ->
                    Column(Modifier.fillMaxWidth().background(if (speaker == "You") Color(0xFF202642) else Panel, RoundedCornerShape(16.dp)).padding(14.dp)) {
                        Text(speaker, color = if (speaker == "You") Cyan else Pink, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(text, color = Color(0xFFE9ECFF), fontSize = 14.sp)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = input, onValueChange = { input = it }, modifier = Modifier.weight(1f), placeholder = { Text("Message Yesmin…") }, shape = RoundedCornerShape(18.dp), maxLines = 4)
                Spacer(Modifier.width(8.dp))
                Button(enabled = input.isNotBlank() && !busy, onClick = {
                    val prompt = input.trim()
                    input = ""
                    messages.add("You" to prompt)
                    if (apiKey.isBlank()) {
                        messages.add("Yesmin" to "Please add your Gemini API key in Settings to enable AI replies.")
                    } else {
                        busy = true; status = "Thinking…"
                        scope.launch {
                            val reply = GeminiClient(apiKey).reply(messages.dropLast(1).takeLast(12), prompt)
                            messages.add("Yesmin" to reply)
                            busy = false; status = "Ready when you are"
                        }
                    }
                }, shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 18.dp)) { Text("Send") }
            }
            Text("AI companion — responses may be inaccurate.", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }

    if (showSettings) {
        var draft by remember { mutableStateOf(apiKey) }
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("Gemini settings") },
            text = {
                Column {
                    Text("Paste your API key. It is stored in this app's private preferences, but this starter does not encrypt it.", fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = draft, onValueChange = { draft = it }, label = { Text("Gemini API key") }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(onClick = { apiKey = draft.trim(); onSaveKey(apiKey); showSettings = false }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { draft = ""; apiKey = ""; onSaveKey(""); showSettings = false }) { Text("Remove key") }
            }
        )
    }
}
