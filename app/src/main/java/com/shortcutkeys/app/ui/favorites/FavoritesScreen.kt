package com.shortcutkeys.app.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shortcutkeys.app.AppViewModel
import com.shortcutkeys.app.data.SoftwareApp
import com.shortcutkeys.app.data.SoftwareCategory
import com.shortcutkeys.app.print.ShortcutPrinter
import com.shortcutkeys.app.ui.detail.ShortcutRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val s = state.strings
    val items = remember(state.favorites, state.software) { viewModel.favoriteItems() }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s.favorites) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (items.isNotEmpty()) {
                        IconButton(onClick = {
                            val fake = SoftwareApp(
                                id = "favorites",
                                nameEn = "Favorites",
                                nameUr = "پسندیدہ",
                                category = SoftwareCategory.CUSTOM,
                                accentColor = 0xFF0F4C81,
                                letter = "★",
                                shortcuts = items.map { (app, shortcut) ->
                                    shortcut.copy(
                                        titleEn = "${app.nameEn}: ${shortcut.titleEn}",
                                        titleUr = "${app.nameUr}: ${shortcut.titleUr}"
                                    )
                                }
                            )
                            ShortcutPrinter.print(context, fake, fake.shortcuts, state.lang)
                        }) {
                            Icon(Icons.Filled.Print, contentDescription = s.print)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(s.emptyFavorites, color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items, key = { it.first.id + it.second.id }) { (app, shortcut) ->
                    Text(
                        text = if (s.urdu) app.nameUr else app.nameEn,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                    ShortcutRow(
                        shortcut = shortcut,
                        urdu = s.urdu,
                        favorite = true,
                        canDelete = false,
                        isPrompt = app.category == SoftwareCategory.AI,
                        copiedLabel = s.copied,
                        copyLabel = s.copyPrompt,
                        copiedCodeLabel = s.copiedCode,
                        copyCodeLabel = s.copyCode,
                        onFavorite = { viewModel.toggleFavorite(app.id, shortcut.id) },
                        onDelete = {}
                    )
                }
            }
        }
    }
}
