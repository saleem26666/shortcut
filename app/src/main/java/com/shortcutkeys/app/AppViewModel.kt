package com.shortcutkeys.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shortcutkeys.app.data.AppLang
import com.shortcutkeys.app.data.ShortcutDto
import com.shortcutkeys.app.data.ShortcutItem
import com.shortcutkeys.app.data.ShortcutRepository
import com.shortcutkeys.app.data.SoftwareApp
import com.shortcutkeys.app.data.SoftwareCategory
import com.shortcutkeys.app.data.SoftwareDto
import com.shortcutkeys.app.data.favoriteKey
import com.shortcutkeys.app.data.matches
import com.shortcutkeys.app.quiz.QuizGenerator
import com.shortcutkeys.app.quiz.QuizPacks
import com.shortcutkeys.app.quiz.QuizSession
import com.shortcutkeys.app.ui.Strings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.util.UUID

data class UiState(
    val software: List<SoftwareApp> = emptyList(),
    val query: String = "",
    val category: SoftwareCategory? = null,
    val lang: AppLang = AppLang.EN,
    val favorites: Set<String> = emptySet(),
    val quizBestPercent: Map<String, Int> = emptyMap()
) {
    val strings get() = Strings(lang)

    val visibleSoftware: List<SoftwareApp>
        get() {
            val q = query.trim().lowercase()
            val byCat = if (category == null) software else software.filter { it.category == category }
            if (q.isEmpty()) return byCat
            return byCat.filter { app ->
                app.nameEn.lowercase().contains(q) ||
                    app.nameUr.contains(query.trim()) ||
                    app.shortcuts.any { it.matches(q, query.trim()) }
            }
        }
}

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = ShortcutRepository(application)

    private val query = MutableStateFlow("")
    private val category = MutableStateFlow<SoftwareCategory?>(null)
    private val tick = MutableStateFlow(0)
    private val quiz = MutableStateFlow<QuizSession?>(null)

    val state: StateFlow<UiState> = combine(query, category, tick) { q, cat, _ ->
        val data = repo.snapshot()
        UiState(
            software = repo.mergedSoftware(),
            query = q,
            category = cat,
            lang = if (data.urdu) AppLang.UR else AppLang.EN,
            favorites = data.favorites,
            quizBestPercent = data.quizBestPercent
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState())

    val quizSession: StateFlow<QuizSession?> = quiz

    fun setQuery(value: String) = query.update { value }
    fun setCategory(value: SoftwareCategory?) = category.update { value }

    fun toggleLanguage() {
        repo.update { it.copy(urdu = !it.urdu) }
        refresh()
    }

    fun toggleFavorite(softwareId: String, shortcutId: String) {
        val key = favoriteKey(softwareId, shortcutId)
        repo.update { data ->
            val next = data.favorites.toMutableSet()
            if (!next.add(key)) next.remove(key)
            data.copy(favorites = next)
        }
        refresh()
    }

    fun isFavorite(softwareId: String, shortcutId: String): Boolean {
        return repo.snapshot().favorites.contains(favoriteKey(softwareId, shortcutId))
    }

    fun addSoftware(nameEn: String, nameUr: String, category: SoftwareCategory, letter: String) {
        val id = "custom_" + UUID.randomUUID().toString().take(8)
        val color = customColors.random()
        val dto = SoftwareDto(
            id = id,
            nameEn = nameEn.trim(),
            nameUr = nameUr.trim().ifBlank { nameEn.trim() },
            category = category.name,
            accentColor = color,
            letter = letter.ifBlank { nameEn.take(2) }.take(2),
            shortcuts = emptyList()
        )
        repo.update { it.copy(customSoftware = it.customSoftware + dto) }
        refresh()
    }

    fun addShortcut(
        softwareId: String,
        keys: String,
        titleEn: String,
        titleUr: String,
        groupEn: String,
        groupUr: String,
        exampleEn: String = "",
        exampleUr: String = ""
    ) {
        val shortcut = ShortcutDto(
            id = "c_" + UUID.randomUUID().toString().take(8),
            keys = keys.trim(),
            titleEn = titleEn.trim(),
            titleUr = titleUr.trim().ifBlank { titleEn.trim() },
            groupEn = groupEn.trim().ifBlank { "Custom" },
            groupUr = groupUr.trim().ifBlank { "اپنا" },
            exampleEn = exampleEn.trim(),
            exampleUr = exampleUr.trim()
        )
        repo.update { data ->
            val customIndex = data.customSoftware.indexOfFirst { it.id == softwareId }
            if (customIndex >= 0) {
                val app = data.customSoftware[customIndex]
                val updated = app.copy(shortcuts = app.shortcuts + shortcut)
                val list = data.customSoftware.toMutableList()
                list[customIndex] = updated
                data.copy(customSoftware = list)
            } else {
                val extras = data.extraShortcuts.toMutableMap()
                extras[softwareId] = extras[softwareId].orEmpty() + shortcut
                data.copy(extraShortcuts = extras)
            }
        }
        refresh()
    }

    fun deleteCustomSoftware(id: String) {
        repo.update { data ->
            data.copy(
                customSoftware = data.customSoftware.filterNot { it.id == id },
                favorites = data.favorites.filterNot { it.startsWith("$id::") }.toSet()
            )
        }
        refresh()
    }

    fun deleteShortcut(softwareId: String, shortcutId: String) {
        repo.update { data ->
            val customIndex = data.customSoftware.indexOfFirst { it.id == softwareId }
            if (customIndex >= 0) {
                val app = data.customSoftware[customIndex]
                val list = data.customSoftware.toMutableList()
                list[customIndex] = app.copy(shortcuts = app.shortcuts.filterNot { it.id == shortcutId })
                data.copy(customSoftware = list)
            } else {
                val extras = data.extraShortcuts.toMutableMap()
                extras[softwareId] = extras[softwareId].orEmpty().filterNot { it.id == shortcutId }
                data.copy(extraShortcuts = extras)
            }
        }
        refresh()
    }

    fun softwareById(id: String): SoftwareApp? = repo.mergedSoftware().find { it.id == id }

    fun favoriteItems(): List<Pair<SoftwareApp, ShortcutItem>> {
        val apps = repo.mergedSoftware().associateBy { it.id }
        return repo.snapshot().favorites.mapNotNull { key ->
            val parts = key.split("::", limit = 2)
            if (parts.size != 2) return@mapNotNull null
            val app = apps[parts[0]] ?: return@mapNotNull null
            val shortcut = app.shortcuts.find { it.id == parts[1] } ?: return@mapNotNull null
            app to shortcut
        }
    }

    fun startQuiz(packId: String, count: Int) {
        val pack = QuizPacks.byId(packId) ?: return
        val questions = QuizGenerator.generate(
            software = repo.mergedSoftware(),
            pack = pack,
            count = count,
            favoriteKeys = repo.snapshot().favorites
        )
        quiz.value = QuizSession(
            packId = pack.id,
            packNameEn = pack.nameEn,
            packNameUr = pack.nameUr,
            questionCount = count,
            questions = questions
        )
    }

    fun pickQuizAnswer(index: Int) {
        val session = quiz.value ?: return
        if (session.finished || session.revealed) return
        val answers = session.answers.toMutableList()
        answers[session.current] = index
        quiz.value = session.copy(picked = index, answers = answers)
    }

    fun nextQuizQuestion() {
        val session = quiz.value ?: return
        if (!session.revealed) return
        if (session.current >= session.questions.lastIndex) {
            val finished = session.copy(finished = true)
            quiz.value = finished
            saveBest(finished)
        } else {
            quiz.value = session.copy(current = session.current + 1, picked = null)
        }
    }

    fun retryWrongQuiz() {
        val session = quiz.value ?: return
        val wrong = session.questions.filterIndexed { i, q -> session.answers[i] != q.correctIndex }
        if (wrong.isEmpty()) {
            startQuiz(session.packId, session.questionCount)
            return
        }
        quiz.value = QuizSession(
            packId = session.packId,
            packNameEn = session.packNameEn,
            packNameUr = session.packNameUr,
            questionCount = wrong.size,
            questions = wrong
        )
    }

    fun replayQuiz() {
        val session = quiz.value ?: return
        startQuiz(session.packId, session.questionCount.coerceAtLeast(10))
    }

    fun discardQuiz() {
        quiz.value = null
    }

    private fun saveBest(session: QuizSession) {
        if (session.total < 10) return
        val percent = session.percent
        repo.update { data ->
            val current = data.quizBestPercent[session.packId] ?: 0
            if (percent <= current) data
            else data.copy(quizBestPercent = data.quizBestPercent + (session.packId to percent))
        }
        refresh()
    }

    private fun refresh() = tick.update { it + 1 }

    companion object {
        private val customColors = listOf(
            0xFF0F4C81, 0xFF0E7C7B, 0xFFD24726, 0xFF217346, 0xFF7B2D8E, 0xFFC44536
        )
    }
}
