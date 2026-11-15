package com.shortcutkeys.app.quiz

import com.shortcutkeys.app.data.ShortcutItem
import com.shortcutkeys.app.data.SoftwareApp
import com.shortcutkeys.app.data.SoftwareCategory
import com.shortcutkeys.app.data.favoriteKey

private data class PoolItem(
    val app: SoftwareApp,
    val shortcut: ShortcutItem
)

private val examGroups = setOf(
    "File", "Edit", "Format", "Navigation", "Selection", "General",
    "Formulas", "Data", "Slideshow", "Mail", "Desktop", "Explorer",
    "View", "Review", "Windows key", "Files", "Taskbar & dialogs"
)

object QuizGenerator {
    fun generate(
        software: List<SoftwareApp>,
        pack: QuizPack,
        count: Int,
        favoriteKeys: Set<String>
    ): List<QuizQuestion> {
        val pool = buildPool(software, pack, favoriteKeys)
        if (pool.size < 4) return emptyList()
        val take = count.coerceAtMost(pool.size)
        return pool.shuffled()
            .mapIndexedNotNull { index, item ->
                val askKeys = when {
                    item.shortcut.keys.trim().startsWith("=") -> false
                    item.shortcut.keys.length > 28 -> false
                    else -> index % 5 != 1
                }
                makeQuestion(item, pool, askKeys)
            }
            .take(take)
    }

    private fun buildPool(
        software: List<SoftwareApp>,
        pack: QuizPack,
        favoriteKeys: Set<String>
    ): List<PoolItem> {
        val apps = when {
            pack.favoritesOnly -> software.filter { it.category != SoftwareCategory.AI }
            pack.softwareIds != null -> software.filter { it.id in pack.softwareIds }
            else -> software.filter { it.category != SoftwareCategory.AI }
        }
        return apps.flatMap { app ->
            app.shortcuts.map { PoolItem(app, it) }
        }.filter { item ->
            val keys = item.shortcut.keys.trim()
            if (keys.isEmpty() || item.shortcut.titleEn.isBlank()) return@filter false
            if (pack.favoritesOnly && favoriteKey(item.app.id, item.shortcut.id) !in favoriteKeys) {
                return@filter false
            }
            if (pack.examStyle) {
                if (keys.startsWith("=")) return@filter false
                if (keys.length > 28) return@filter false
                if (item.shortcut.groupEn !in examGroups) return@filter false
            }
            true
        }.distinctBy { it.app.id + "::" + it.shortcut.id }
    }

    private fun makeQuestion(
        item: PoolItem,
        pool: List<PoolItem>,
        askKeys: Boolean
    ): QuizQuestion? {
        val correctKeys = item.shortcut.keys.trim()
        val correctTitleEn = item.shortcut.titleEn.trim()
        val correctTitleUr = item.shortcut.titleUr.trim().ifBlank { correctTitleEn }
        val sameApp = pool.filter { it.app.id == item.app.id && it.shortcut.id != item.shortcut.id }
        val others = pool.filter { it.app.id != item.app.id }
        val candidates = (sameApp.shuffled() + others.shuffled())
        val distractors = mutableListOf<Triple<String, String, String>>()
        for (candidate in candidates) {
            if (distractors.size >= 3) break
            val keys = candidate.shortcut.keys.trim()
            val titleEn = candidate.shortcut.titleEn.trim()
            val titleUr = candidate.shortcut.titleUr.trim().ifBlank { titleEn }
            val duplicate = if (askKeys) {
                keys.equals(correctKeys, ignoreCase = true) ||
                    distractors.any { option -> option.first.equals(keys, true) }
            } else {
                titleEn.equals(correctTitleEn, ignoreCase = true) ||
                    distractors.any { option -> option.second.equals(titleEn, true) }
            }
            if (duplicate || keys.isEmpty() || titleEn.isEmpty()) continue
            distractors += Triple(keys, titleEn, titleUr)
        }
        if (distractors.size < 3) return null

        val correctTriple = Triple(correctKeys, correctTitleEn, correctTitleUr)
        val optionTriples = (distractors + correctTriple).shuffled()
        val correctIndex = optionTriples.indexOf(correctTriple)
        val optionsEn = optionTriples.map { if (askKeys) it.first else it.second }
        val optionsUr = optionTriples.map { if (askKeys) it.first else it.third }
        val appEn = item.app.nameEn
        val appUr = item.app.nameUr
        val promptEn = if (askKeys) {
            "In $appEn, which shortcut is used for:\n${item.shortcut.titleEn}"
        } else {
            "In $appEn, what does this shortcut do?\n${item.shortcut.keys}"
        }
        val promptUr = if (askKeys) {
            "$appUr میں اس کام کے لیے کون سی شارٹ کٹ ہے؟\n${item.shortcut.titleUr.ifBlank { item.shortcut.titleEn }}"
        } else {
            "$appUr میں یہ شارٹ کٹ کیا کرتی ہے؟\n${item.shortcut.keys}"
        }
        val exampleEn = item.shortcut.exampleEn.trim()
        val exampleUr = item.shortcut.exampleUr.trim().ifBlank { exampleEn }
        val explainEn = buildString {
            append("Correct: ${item.shortcut.keys} — ${item.shortcut.titleEn}")
            if (exampleEn.isNotEmpty()) append("\n").append(exampleEn)
        }
        val explainUr = buildString {
            append("درست: ${item.shortcut.keys} — ${item.shortcut.titleUr.ifBlank { item.shortcut.titleEn }}")
            if (exampleUr.isNotEmpty()) append("\n").append(exampleUr)
        }
        return QuizQuestion(
            id = item.app.id + "::" + item.shortcut.id,
            softwareId = item.app.id,
            accentColor = item.app.accentColor,
            softwareNameEn = appEn,
            softwareNameUr = appUr,
            askForKeys = askKeys,
            promptEn = promptEn,
            promptUr = promptUr,
            optionsEn = optionsEn,
            optionsUr = optionsUr,
            correctIndex = correctIndex,
            explainEn = explainEn,
            explainUr = explainUr
        )
    }
}
