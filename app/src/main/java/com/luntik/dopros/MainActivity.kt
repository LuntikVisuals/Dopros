package com.luntik.dopros

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        setContent { DoprosApp() }
    }
}

private object C {
    val Bg = Color(0xFF0A0A0C)
    val Panel = Color(0xFF141418)
    val Border = Color.White.copy(alpha = 0.12f)
    val Text = Color(0xFFE8E6E3)
    val Dim = Color.White.copy(alpha = 0.55f)
    val Mute = Color.White.copy(alpha = 0.32f)
    val Blood = Color(0xFFC45C5C)
    val Fear = Color(0xFFE8A838)
    val Ok = Color(0xFF5CFFB0)
    val Shock = Color(0xFF6EC8FF)
}

private enum class Screen { NamePick, Menu, Interrogation, Results }

@Composable
fun DoprosApp() {
    var screen by remember { mutableStateOf(Screen.NamePick) }
    var investigator by remember { mutableStateOf("") }
    var suspectIndex by remember { mutableIntStateOf(0) }
    var lastStars by remember { mutableFloatStateOf(0f) }
    var lastInfo by remember { mutableFloatStateOf(0f) }
    var lastLabel by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize().background(C.Bg)) {
        when (screen) {
            Screen.NamePick -> NamePickScreen {
                investigator = it
                screen = Screen.Menu
            }
            Screen.Menu -> MenuScreen(investigator) {
                suspectIndex = 0
                screen = Screen.Interrogation
            }
            Screen.Interrogation -> {
                val suspect = GameData.tutorialSuspects[suspectIndex]
                InterrogationScreen(investigator, suspect) { stars, info, label ->
                    lastStars = stars
                    lastInfo = info
                    lastLabel = label
                    screen = Screen.Results
                }
            }
            Screen.Results -> ResultsScreen(
                stars = lastStars,
                info = lastInfo,
                label = lastLabel,
                hasNext = suspectIndex < GameData.tutorialSuspects.lastIndex,
                onNext = {
                    suspectIndex++
                    screen = Screen.Interrogation
                },
                onMenu = { screen = Screen.Menu }
            )
        }
    }
}

@Composable
private fun NamePickScreen(onPick: (String) -> Unit) {
    Column(
        Modifier.fillMaxSize().statusBarsPadding().padding(24.dp).verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(32.dp))
        Text("ДОПРОС", color = C.Blood, fontSize = 36.sp, fontWeight = FontWeight.Bold)
        Text("0.1 · training", color = C.Mute, fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))
        Text("Choose investigator name", color = C.Dim, fontSize = 15.sp)
        Spacer(Modifier.height(20.dp))
        GameData.investigatorNames.forEach { name ->
            Panel {
                Text(
                    name,
                    color = C.Text,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth().clickable { onPick(name) }.padding(4.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MenuScreen(investigator: String, onStart: () -> Unit) {
    Column(
        Modifier.fillMaxSize().statusBarsPadding().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("ДОПРОС", color = C.Blood, fontSize = 40.sp, fontWeight = FontWeight.Bold)
        Text("Investigator: $investigator", color = C.Dim, fontSize = 14.sp)
        Spacer(Modifier.height(32.dp))
        Panel {
            Text("Location", color = C.Mute, fontSize = 12.sp)
            Text("Earth · Precinct", color = C.Text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text("Training · 2 humans", color = C.Dim, fontSize = 13.sp)
            Spacer(Modifier.height(16.dp))
            ActionBtn("Start interrogation", C.Blood, onStart)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "18+ · You ASK questions. Suspect answers.\nShock / flashlight. Don't die.",
            color = C.Mute,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun InterrogationScreen(
    investigator: String,
    suspect: Suspect,
    onFinished: (stars: Float, info: Float, label: String) -> Unit
) {
    // remaining questions the PLAYER can ask
    var remaining by remember { mutableStateOf(suspect.asks) }
    var info by remember { mutableFloatStateOf(0f) }
    var fearSubject by remember { mutableFloatStateOf(20f) }
    var fearPlayer by remember { mutableFloatStateOf(10f) }
    var shockCd by remember { mutableIntStateOf(0) }
    var lightCd by remember { mutableIntStateOf(0) }
    var excessShock by remember { mutableIntStateOf(0) }
    var log by remember {
        mutableStateOf(listOf("${suspect.name} sits down.", suspect.intro))
    }
    var alive by remember { mutableStateOf(true) }

    LaunchedEffect(shockCd) {
        if (shockCd > 0) { delay(1000); shockCd-- }
    }
    LaunchedEffect(lightCd) {
        if (lightCd > 0) { delay(1000); lightCd-- }
    }
    LaunchedEffect(fearPlayer, fearSubject) {
        if (fearPlayer > fearSubject + 15f && alive) {
            delay(400)
            log = log + "${suspect.name} turns it on you. Pressure."
            fearPlayer = (fearPlayer + 5f).coerceAtMost(100f)
            if (fearPlayer >= 100f) {
                alive = false
                onFinished(0f, info, "Death · breakdown")
            }
        }
    }

    fun addLog(line: String) {
        log = (log + line).takeLast(8)
    }

    fun finishCase() {
        val infoScore = (info / suspect.maxInfo).coerceIn(0f, 1f) * 7f
        val survive = if (alive) 2f else 0f
        val clean = (1f - (excessShock * 0.15f).coerceAtMost(1f)) * 1f
        val stars = (infoScore + survive + clean).coerceIn(0f, 10f)
        val label = when {
            !alive -> "Death"
            stars >= 10f -> "Success"
            stars >= 8.1f -> "Completed"
            stars >= 6f -> "Partial"
            stars >= 5f -> "Incomplete"
            stars >= 3f -> "Fail"
            stars >= 1f -> "TOTAL FAIL"
            else -> "Death"
        }
        onFinished(stars, info, label)
    }

    Column(Modifier.fillMaxSize().statusBarsPadding().padding(16.dp)) {
        Text("Inv. $investigator", color = C.Mute, fontSize = 11.sp)
        Text(suspect.name, color = C.Text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(suspect.role, color = C.Dim, fontSize = 12.sp)
        Spacer(Modifier.height(10.dp))

        Meter("Info", info / suspect.maxInfo, C.Ok)
        Spacer(Modifier.height(6.dp))
        Meter("Suspect fear", fearSubject / 100f, C.Fear)
        Spacer(Modifier.height(6.dp))
        Meter("Your fear", fearPlayer / 100f, C.Blood)
        Spacer(Modifier.height(12.dp))

        Panel {
            log.forEach {
                Text(it, color = C.Dim, fontSize = 13.sp, lineHeight = 18.sp)
                Spacer(Modifier.height(4.dp))
            }
        }
        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ToolBtn(
                if (shockCd > 0) "Shock ${shockCd}s" else "Shock",
                C.Shock,
                shockCd == 0 && alive
            ) {
                shockCd = 5
                excessShock++
                fearSubject = (fearSubject + 18f).coerceAtMost(100f)
                fearPlayer = (fearPlayer + 4f).coerceAtMost(100f)
                addLog("You use the shock. ${suspect.name} jerks.")
            }
            ToolBtn(
                if (lightCd > 0) "Light ${lightCd}s" else "Flashlight",
                C.Fear,
                lightCd == 0 && alive
            ) {
                lightCd = 4
                fearSubject = (fearSubject + 12f).coerceAtMost(100f)
                fearPlayer = (fearPlayer + 2f).coerceAtMost(100f)
                addLog("Flashlight in the face. Fear climbs.")
            }
        }
        Spacer(Modifier.height(12.dp))

        if (remaining.isNotEmpty() && alive) {
            Text("Ask:", color = C.Mute, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            remaining.forEach { ask ->
                Panel {
                    Text(
                        ask.question,
                        color = C.Text,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // YOU asked — THEY answer
                                addLog("You: ${ask.question}")
                                addLog("${suspect.name}: ${ask.reply}")
                                info = (info + ask.infoGain).coerceAtMost(suspect.maxInfo)
                                fearSubject = (fearSubject + ask.fearSubject).coerceIn(0f, 100f)
                                fearPlayer = (fearPlayer + ask.fearPlayer).coerceIn(0f, 100f)
                                if (ask.infoGain >= 25f) addLog("Important lead logged.")
                                remaining = remaining - ask
                            }
                            .padding(4.dp)
                    )
                }
                Spacer(Modifier.height(6.dp))
            }
        } else if (alive) {
            ActionBtn("End interrogation", C.Blood) { finishCase() }
        }
    }
}

@Composable
private fun ResultsScreen(
    stars: Float,
    info: Float,
    label: String,
    hasNext: Boolean,
    onNext: () -> Unit,
    onMenu: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().statusBarsPadding().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("RESULT", color = C.Mute, fontSize = 14.sp)
        Text(String.format("%.1f", stars), color = C.Fear, fontSize = 56.sp, fontWeight = FontWeight.Bold)
        Text("/ 10 stars", color = C.Dim, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        Text(label, color = C.Text, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Text("Info: ${info.roundToInt()}%", color = C.Dim, fontSize = 14.sp)
        Spacer(Modifier.height(28.dp))
        if (hasNext) {
            ActionBtn("Next suspect", C.Blood, onNext)
            Spacer(Modifier.height(10.dp))
        }
        ActionBtn("Menu", C.Panel, onMenu)
    }
}

@Composable
private fun Meter(label: String, value: Float, color: Color) {
    val anim by animateFloatAsState(value.coerceIn(0f, 1f), label = label)
    Column {
        Text(label, color = C.Mute, fontSize = 11.sp)
        Box(
            Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = 0.08f))
        ) {
            Box(Modifier.fillMaxWidth(anim).height(8.dp).background(color))
        }
    }
}

@Composable
private fun Panel(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(C.Panel)
            .border(1.dp, C.Border, RoundedCornerShape(14.dp)).padding(14.dp),
        content = content
    )
}

@Composable
private fun ActionBtn(text: String, color: Color, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(color)
            .clickable(onClick = onClick).padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (color == C.Panel) C.Text else Color.Black, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ToolBtn(label: String, color: Color, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(10.dp))
            .background(if (enabled) color.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
            .border(1.dp, if (enabled) color.copy(alpha = 0.5f) else C.Border, RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(label, color = if (enabled) color else C.Mute, fontSize = 13.sp)
    }
}
