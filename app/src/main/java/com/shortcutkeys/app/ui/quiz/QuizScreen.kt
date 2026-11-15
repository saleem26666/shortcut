package com.shortcutkeys.app.ui.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shortcutkeys.app.AppViewModel
import com.shortcutkeys.app.quiz.QuizPack
import com.shortcutkeys.app.quiz.QuizPacks
import com.shortcutkeys.app.quiz.QuizQuestion
import com.shortcutkeys.app.quiz.QuizSession
import com.shortcutkeys.app.ui.Strings
import com.shortcutkeys.app.ui.components.KeyPills

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val session by viewModel.quizSession.collectAsStateWithLifecycle()
    val s = state.strings

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            session == null -> s.practice
                            session!!.finished -> s.quizResult
                            else -> if (s.urdu) session!!.packNameUr else session!!.packNameEn
                        },
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (session?.finished == true) viewModel.discardQuiz()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        when {
            session == null ->             QuizSetup(
                s = s,
                best = state.quizBestPercent,
                modifier = Modifier.padding(padding),
                onStart = { packId, count -> viewModel.startQuiz(packId, count) }
            )
            session!!.questions.isEmpty() -> QuizEmpty(
                s = s,
                modifier = Modifier.padding(padding),
                onReset = { viewModel.discardQuiz() }
            )
            session!!.finished -> QuizResult(
                session = session!!,
                s = s,
                best = state.quizBestPercent[session!!.packId] ?: session!!.percent,
                modifier = Modifier.padding(padding),
                onRetryWrong = { viewModel.retryWrongQuiz() },
                onReplay = { viewModel.replayQuiz() },
                onNew = { viewModel.discardQuiz() }
            )
            else -> QuizPlay(
                session = session!!,
                s = s,
                modifier = Modifier.padding(padding),
                onPick = viewModel::pickQuizAnswer,
                onNext = viewModel::nextQuizQuestion
            )
        }
    }
}

@Composable
private fun QuizSetup(
    s: Strings,
    best: Map<String, Int>,
    modifier: Modifier = Modifier,
    onStart: (String, Int) -> Unit
) {
    var count by remember { mutableIntStateOf(10) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(s.practiceHint, color = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.height(16.dp))
        Text(s.questions, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(10, 20, 30).forEach { n ->
                FilterChip(
                    selected = count == n,
                    onClick = { count = n },
                    label = { Text("$n") }
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        QuizPacks.all.forEach { pack ->
            PackCard(
                pack = pack,
                urdu = s.urdu,
                best = best[pack.id],
                startLabel = s.startQuiz,
                onStart = { onStart(pack.id, count) }
            )
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun PackCard(
    pack: QuizPack,
    urdu: Boolean,
    best: Int?,
    startLabel: String,
    onStart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                if (urdu) pack.nameUr else pack.nameEn,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (urdu) pack.detailUr else pack.detailEn,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (best != null) {
                    Text(
                        "${if (urdu) "بہترین" else "Best"} $best%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
                Button(onClick = onStart) { Text(startLabel) }
            }
        }
    }
}

@Composable
private fun QuizEmpty(s: Strings, modifier: Modifier = Modifier, onReset: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(s.quizEmpty, color = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onReset) { Text(s.newQuiz) }
    }
}

@Composable
private fun QuizPlay(
    session: QuizSession,
    s: Strings,
    modifier: Modifier = Modifier,
    onPick: (Int) -> Unit,
    onNext: () -> Unit
) {
    val q = session.currentQuestion ?: return
    val picked = session.picked ?: session.answers.getOrNull(session.current)
    val last = session.current >= session.questions.lastIndex
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "${s.questionOf} ${session.current + 1} / ${session.total}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { (session.current + if (session.revealed) 1 else 0) / session.total.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp)),
        )
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(q.accentColor)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (s.urdu) q.softwareNameUr.take(1) else q.softwareNameEn.take(1),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                if (s.urdu) q.softwareNameUr else q.softwareNameEn,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            if (s.urdu) q.promptUr else q.promptEn,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(16.dp))
        q.optionsEn.indices.forEach { index ->
            OptionCard(
                letter = ('A' + index).toString(),
                question = q,
                index = index,
                urdu = s.urdu,
                picked = picked,
                revealed = session.revealed,
                onPick = onPick
            )
            Spacer(Modifier.height(8.dp))
        }
        if (session.revealed) {
            val correct = picked == q.correctIndex
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (correct) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    }
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (correct) Icons.Filled.CheckCircle else Icons.Outlined.Cancel,
                            contentDescription = null,
                            tint = if (correct) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (correct) s.correct else s.wrong,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (s.urdu) q.explainUr else q.explainEn,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                Text(if (last) s.seeResult else s.nextQuestion)
            }
        } else {
            Text(s.tapOption, color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun OptionCard(
    letter: String,
    question: QuizQuestion,
    index: Int,
    urdu: Boolean,
    picked: Int?,
    revealed: Boolean,
    onPick: (Int) -> Unit
) {
    val isCorrect = index == question.correctIndex
    val isPicked = picked == index
    val border = when {
        revealed && isCorrect -> MaterialTheme.colorScheme.secondary
        revealed && isPicked && !isCorrect -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }
    val bg = when {
        revealed && isCorrect -> MaterialTheme.colorScheme.secondaryContainer
        revealed && isPicked && !isCorrect -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val text = if (urdu) question.optionsUr[index] else question.optionsEn[index]
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(1.5.dp, border, RoundedCornerShape(14.dp))
            .clickable(enabled = !revealed) { onPick(index) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(border.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(letter, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(Modifier.width(10.dp))
        if (question.askForKeys && !text.startsWith("=") && text.length <= 28) {
            KeyPills(text, modifier = Modifier.weight(1f))
        } else {
            Text(text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun QuizResult(
    session: QuizSession,
    s: Strings,
    best: Int,
    modifier: Modifier = Modifier,
    onRetryWrong: () -> Unit,
    onReplay: () -> Unit,
    onNew: () -> Unit
) {
    val wrongCount = session.total - session.score
    val message = when {
        session.percent >= 80 -> s.greatScore
        session.percent >= 50 -> s.okScore
        else -> s.lowScore
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Quiz, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text("${session.score} / ${session.total}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("${session.percent}% · ${s.bestScore} $best%", color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text(message, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        item {
            Button(onClick = onReplay, modifier = Modifier.fillMaxWidth()) { Text(s.playAgain) }
        }
        if (wrongCount > 0) {
            item {
                OutlinedButton(onClick = onRetryWrong, modifier = Modifier.fillMaxWidth()) {
                    Text("${s.retryWrong} ($wrongCount)")
                }
            }
        }
        item {
            OutlinedButton(onClick = onNew, modifier = Modifier.fillMaxWidth()) { Text(s.newQuiz) }
        }
        items(session.questions.indices.toList()) { i ->
            val q = session.questions[i]
            val chosen = session.answers[i]
            val ok = chosen == q.correctIndex
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        if (ok) s.correct else s.wrong,
                        color = if (ok) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(if (s.urdu) q.promptUr else q.promptEn, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (s.urdu) q.explainUr else q.explainEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
