package com.shortcutkeys.app.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shortcutkeys.app.AppViewModel
import com.shortcutkeys.app.data.ShortcutItem
import com.shortcutkeys.app.data.SoftwareCategory
import com.shortcutkeys.app.data.isCopyableCode
import com.shortcutkeys.app.data.matches
import com.shortcutkeys.app.print.ShortcutPrinter
import com.shortcutkeys.app.ui.components.KeyPills
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SoftwareDetailScreen(
    softwareId: String,
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onAddShortcut: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val s = state.strings
    val app = state.software.find { it.id == softwareId }
    var query by remember { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (app == null) {
        Scaffold(topBar = {
            TopAppBar(title = { Text(s.appName) }, navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            })
        }) { padding ->
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(s.noResults)
            }
        }
        return
    }

    val filtered = remember(app, query, s.urdu) {
        val q = query.trim()
        if (q.isEmpty()) app.shortcuts
        else app.shortcuts.filter { it.matches(q.lowercase(), q) }
    }
    val grouped = remember(filtered, s.urdu) {
        filtered.groupBy { if (s.urdu) it.groupUr else it.groupEn }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (s.urdu) app.nameUr else app.nameEn, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        ShortcutPrinter.print(context, app, filtered, state.lang)
                    }) {
                        Icon(Icons.Filled.Print, contentDescription = s.print)
                    }
                    if (app.isCustom) {
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = s.delete)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddShortcut) {
                Icon(Icons.Filled.Add, contentDescription = s.addShortcut)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text(s.searchShortcut) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            if (app.category == SoftwareCategory.AI) {
                Text(
                    text = s.promptHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            TextButton(
                onClick = { ShortcutPrinter.print(context, app, filtered, state.lang) },
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Icon(Icons.Filled.Print, contentDescription = null)
                Text(s.printList, modifier = Modifier.padding(start = 8.dp))
            }
            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(s.noResults, color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    grouped.forEach { (group, items) ->
                        stickyHeader {
                            Text(
                                text = group,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(vertical = 8.dp),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(items, key = { it.id }) { shortcut ->
                            ShortcutRow(
                                shortcut = shortcut,
                                urdu = s.urdu,
                                favorite = viewModel.isFavorite(app.id, shortcut.id),
                                canDelete = shortcut.isCustom,
                                isPrompt = app.category == SoftwareCategory.AI,
                                copiedLabel = s.copied,
                                copyLabel = s.copyPrompt,
                                copiedCodeLabel = s.copiedCode,
                                copyCodeLabel = s.copyCode,
                                onFavorite = { viewModel.toggleFavorite(app.id, shortcut.id) },
                                onDelete = { viewModel.deleteShortcut(app.id, shortcut.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(s.deleteSoftware) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCustomSoftware(app.id)
                    confirmDelete = false
                    onBack()
                }) { Text(s.delete) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(s.cancel) }
            }
        )
    }
}

@Composable
fun ShortcutRow(
    shortcut: ShortcutItem,
    urdu: Boolean,
    favorite: Boolean,
    canDelete: Boolean,
    onFavorite: () -> Unit,
    onDelete: () -> Unit,
    isPrompt: Boolean = false,
    copiedLabel: String = "Copied",
    copyLabel: String = "Copy prompt",
    copiedCodeLabel: String = "Copied — paste in the address bar",
    copyCodeLabel: String = "Copy code"
) {
    var expanded by remember { mutableStateOf(false) }
    var copied by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val example = if (urdu) {
        shortcut.exampleUr.ifBlank { shortcut.exampleEn }
    } else {
        shortcut.exampleEn.ifBlank { shortcut.exampleUr }
    }
    val hasExample = example.isNotBlank()
    val canCopy = isPrompt || shortcut.isCopyableCode
    val canExpand = hasExample || isPrompt
    val activeCopyLabel = if (isPrompt) copyLabel else copyCodeLabel
    val activeCopiedLabel = if (isPrompt) copiedLabel else copiedCodeLabel

    LaunchedEffect(copied) {
        if (copied) {
            delay(1800)
            copied = false
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .then(if (canExpand) Modifier.clickable { expanded = !expanded } else Modifier)
        ) {
            if (isPrompt) {
                Text(
                    text = if (urdu) shortcut.titleUr else shortcut.titleEn,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = shortcut.keys,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    maxLines = if (expanded) Int.MAX_VALUE else 4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                        .padding(10.dp)
                )
                Text(
                    text = if (copied) activeCopiedLabel else activeCopyLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            } else {
                KeyPills(shortcut.keys)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (urdu) shortcut.titleUr else shortcut.titleEn,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (canCopy && !isPrompt) {
                    Text(
                        text = if (copied) activeCopiedLabel else activeCopyLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            if (hasExample && !expanded) {
                Text(
                    text = if (urdu) "مثال دیکھنے کے لیے ٹیپ کریں" else "Tap to see example",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            AnimatedVisibility(
                visible = hasExample && expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f))
                        .padding(10.dp)
                ) {
                    Text(
                        text = if (urdu) "کیسے استعمال کریں" else "How to use",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = example,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
        if (canCopy) {
            IconButton(onClick = {
                clipboard.setText(AnnotatedString(shortcut.keys))
                copied = true
            }) {
                Icon(
                    Icons.Filled.ContentCopy,
                    contentDescription = activeCopyLabel,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        if (canExpand) {
            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (urdu) "مثال" else "Example",
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
        IconButton(onClick = onFavorite) {
            Icon(
                imageVector = if (favorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = if (favorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
            )
        }
        if (canDelete) {
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
