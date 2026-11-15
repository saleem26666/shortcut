package com.shortcutkeys.app.print

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.shortcutkeys.app.data.AppLang
import com.shortcutkeys.app.data.ShortcutItem
import com.shortcutkeys.app.data.SoftwareApp

object ShortcutPrinter {
    private var keepAlive: WebView? = null

    fun print(
        context: Context,
        app: SoftwareApp,
        shortcuts: List<ShortcutItem>,
        lang: AppLang
    ) {
        val urdu = lang == AppLang.UR
        val title = if (urdu) app.nameUr else app.nameEn
        val html = buildHtml(app, shortcuts, urdu)
        val webView = WebView(context.applicationContext)
        keepAlive = webView
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                if (url == "about:blank") return
                view.post {
                    try {
                        val manager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                        val adapter = view.createPrintDocumentAdapter(title)
                        manager.print(
                            title,
                            adapter,
                            PrintAttributes.Builder()
                                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                                .build()
                        )
                    } catch (error: Exception) {
                        Toast.makeText(
                            context,
                            if (urdu) "پرنٹ / PDF نہیں کھل سکا۔" else "Could not open Print / PDF.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
        webView.loadDataWithBaseURL(
            "https://localhost/",
            html,
            "text/html",
            "UTF-8",
            null
        )
    }

    private fun buildHtml(
        app: SoftwareApp,
        shortcuts: List<ShortcutItem>,
        urdu: Boolean
    ): String {
        val isPrompt = app.id == "ai_prompts"
        val dir = if (urdu && !isPrompt) "rtl" else "ltr"
        val name = if (urdu) app.nameUr else app.nameEn
        val subtitle = when {
            isPrompt && urdu -> "اے آئی پرامپٹ — پرنٹ آؤٹ"
            isPrompt -> "AI Prompt — Printout"
            urdu -> "شارٹ کٹ کیز — پرنٹ آؤٹ"
            else -> "Shortcut Keys — Printout"
        }
        val grouped = shortcuts.groupBy { if (urdu) it.groupUr else it.groupEn }
        val body = buildString {
            grouped.forEach { (group, items) ->
                append("<h2>").append(escape(group)).append("</h2>")
                if (isPrompt) {
                    items.forEach { item ->
                        val action = if (urdu) item.titleUr else item.titleEn
                        val example = if (urdu) {
                            item.exampleUr.ifBlank { item.exampleEn }
                        } else {
                            item.exampleEn.ifBlank { item.exampleUr }
                        }
                        append("<div class='card'>")
                        append("<div class='ptitle'>").append(escape(action)).append("</div>")
                        append("<div class='prompt'>")
                        append(escape(item.keys).replace("\n", "<br/>"))
                        append("</div>")
                        if (example.isNotBlank()) {
                            append("<div class='ex'><b>")
                            append(if (urdu) "کیسے استعمال کریں: " else "How to use: ")
                            append("</b>")
                            append(escape(example).replace("\n", "<br/>"))
                            append("</div>")
                        }
                        append("</div>")
                    }
                } else {
                    append("<table><thead><tr><th>")
                    append(if (urdu) "شارٹ کٹ" else "Shortcut")
                    append("</th><th>")
                    append(if (urdu) "کام" else "Action")
                    append("</th></tr></thead><tbody>")
                    items.forEach { item ->
                        val action = if (urdu) item.titleUr else item.titleEn
                        val example = if (urdu) {
                            item.exampleUr.ifBlank { item.exampleEn }
                        } else {
                            item.exampleEn.ifBlank { item.exampleUr }
                        }
                        append("<tr><td class='keys'>")
                        append(escape(item.keys))
                        append("</td><td>")
                        append(escape(action))
                        if (example.isNotBlank()) {
                            append("<div class='ex'><b>")
                            append(if (urdu) "مثال: " else "Example: ")
                            append("</b>")
                            append(escape(example).replace("\n", "<br/>"))
                            append("</div>")
                        }
                        append("</td></tr>")
                    }
                    append("</tbody></table>")
                }
            }
        }
        return """
            <!DOCTYPE html>
            <html dir="$dir" lang="${if (urdu) "ur" else "en"}">
            <head>
              <meta charset="utf-8"/>
              <style>
                body { font-family: sans-serif; padding: 24px; color: #1a1a1a; overflow-wrap: anywhere; }
                h1 { margin: 0; font-size: 26px; }
                .sub { color: #555; margin: 4px 0 20px; }
                h2 { font-size: 16px; margin: 22px 0 8px; border-bottom: 2px solid #0F4C81; padding-bottom: 4px; color: #0F4C81; }
                table { width: 100%; border-collapse: collapse; margin-bottom: 8px; }
                th, td { border: 1px solid #d0d7de; padding: 8px 10px; text-align: start; font-size: 13px; overflow-wrap: anywhere; }
                th { background: #0F4C81; color: #fff; }
                tr:nth-child(even) { background: #f4f7fb; }
                .keys { font-weight: 700; font-family: Consolas, monospace; }
                .card { border: 1px solid #d0d7de; padding: 10px 12px; margin: 0 0 10px; page-break-inside: avoid; }
                .ptitle { font-weight: 700; font-size: 14px; margin-bottom: 6px; color: #0F4C81; }
                .prompt { font-family: Consolas, monospace; font-size: 12px; white-space: pre-wrap; overflow-wrap: anywhere; background: #f4f7fb; padding: 8px; }
                .ex { color: #3d5a45; font-size: 12px; margin-top: 6px; font-weight: 400; line-height: 1.4; }
              </style>
            </head>
            <body>
              <h1>${escape(name)}</h1>
              <div class="sub">${escape(subtitle)} · ${shortcuts.size}</div>
              $body
            </body>
            </html>
        """.trimIndent()
    }

    private fun escape(value: String): String = buildString(value.length) {
        value.forEach { ch ->
            when (ch) {
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '&' -> append("&amp;")
                '"' -> append("&quot;")
                '%' -> append("&#37;")
                else -> append(ch)
            }
        }
    }
}
