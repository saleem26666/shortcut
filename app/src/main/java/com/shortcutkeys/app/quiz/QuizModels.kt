package com.shortcutkeys.app.quiz

data class QuizPack(
    val id: String,
    val nameEn: String,
    val nameUr: String,
    val detailEn: String,
    val detailUr: String,
    val softwareIds: Set<String>? = null,
    val examStyle: Boolean = false,
    val favoritesOnly: Boolean = false
)

data class QuizQuestion(
    val id: String,
    val softwareId: String,
    val accentColor: Long,
    val softwareNameEn: String,
    val softwareNameUr: String,
    val askForKeys: Boolean,
    val promptEn: String,
    val promptUr: String,
    val optionsEn: List<String>,
    val optionsUr: List<String>,
    val correctIndex: Int,
    val explainEn: String,
    val explainUr: String
)

data class QuizSession(
    val packId: String,
    val packNameEn: String,
    val packNameUr: String,
    val questionCount: Int,
    val questions: List<QuizQuestion>,
    val current: Int = 0,
    val picked: Int? = null,
    val answers: List<Int?> = List(questions.size) { null },
    val finished: Boolean = false
) {
    val total get() = questions.size
    val score get() = questions.indices.count { answers[it] == questions[it].correctIndex }
    val percent get() = if (total == 0) 0 else (score * 100) / total
    val currentQuestion get() = questions.getOrNull(current)
    val revealed get() = picked != null || (answers.getOrNull(current) != null)
}

object QuizPacks {
    val all = listOf(
        QuizPack(
            id = "job",
            nameEn = "Job test / Computer operator",
            nameUr = "نوکری ٹیسٹ / کمپیوٹر آپریٹر",
            detailEn = "Windows, Word, Excel, PowerPoint — the keys that come in exams.",
            detailUr = "ونڈوز، ورڈ، ایکسل، پاورپوائنٹ — وہ کیز جو امتحان میں آتی ہیں۔",
            softwareIds = setOf("windows", "word", "excel", "powerpoint", "explorer"),
            examStyle = true
        ),
        QuizPack(
            id = "excel",
            nameEn = "Microsoft Excel",
            nameUr = "مائیکروسافٹ ایکسل",
            detailEn = "Shortcuts and formulas from Excel.",
            detailUr = "ایکسل کی شارٹ کٹس اور فارمولے۔",
            softwareIds = setOf("excel")
        ),
        QuizPack(
            id = "word",
            nameEn = "Microsoft Word",
            nameUr = "مائیکروسافٹ ورڈ",
            detailEn = "Typing, format, and hidden Word keys.",
            detailUr = "ورڈ کی ٹائپنگ، فارمیٹ اور پوشیدہ کیز۔",
            softwareIds = setOf("word")
        ),
        QuizPack(
            id = "windows",
            nameEn = "Windows",
            nameUr = "ونڈوز",
            detailEn = "Desktop, File Explorer, and system keys.",
            detailUr = "ڈیسک ٹاپ، فائل ایکسپلورر اور سسٹم کیز۔",
            softwareIds = setOf("windows", "explorer")
        ),
        QuizPack(
            id = "office",
            nameEn = "Office mix",
            nameUr = "آفس مکس",
            detailEn = "Word, Excel, PowerPoint, and Outlook together.",
            detailUr = "ورڈ، ایکسل، پاورپوائنٹ اور آؤٹ لک ایک ساتھ۔",
            softwareIds = setOf("word", "excel", "powerpoint", "outlook")
        ),
        QuizPack(
            id = "favorites",
            nameEn = "My favorites",
            nameUr = "میرے پسندیدہ",
            detailEn = "Quiz only the shortcuts you starred.",
            detailUr = "صرف وہ شارٹ کٹس جن پر ستارہ لگایا ہے۔",
            favoritesOnly = true
        ),
        QuizPack(
            id = "mixed",
            nameEn = "All software",
            nameUr = "تمام سافٹ ویئر",
            detailEn = "A mix from every app in the catalog.",
            detailUr = "کیٹلاگ کی ہر ایپ سے ملا جلا ٹیسٹ۔"
        )
    )

    fun byId(id: String) = all.find { it.id == id }
}
