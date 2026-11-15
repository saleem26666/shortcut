package com.shortcutkeys.app.ui.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shortcutkeys.app.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShortcutScreen(
    softwareId: String,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val s = state.strings
    var keys by remember { mutableStateOf("") }
    var titleEn by remember { mutableStateOf("") }
    var titleUr by remember { mutableStateOf("") }
    var groupEn by remember { mutableStateOf("Custom") }
    var groupUr by remember { mutableStateOf("اپنا") }
    var exampleEn by remember { mutableStateOf("") }
    var exampleUr by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s.addShortcut) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = keys,
                onValueChange = { keys = it; error = false },
                label = { Text(s.keys) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = titleEn,
                onValueChange = { titleEn = it; error = false },
                label = { Text(s.actionEn) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = titleUr,
                onValueChange = { titleUr = it },
                label = { Text(s.actionUr) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = groupEn,
                onValueChange = { groupEn = it },
                label = { Text(s.groupEn) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = groupUr,
                onValueChange = { groupUr = it },
                label = { Text(s.groupUr) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = exampleEn,
                onValueChange = { exampleEn = it },
                label = { Text(s.exampleEn) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            OutlinedTextField(
                value = exampleUr,
                onValueChange = { exampleUr = it },
                label = { Text(s.exampleUrLabel) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            if (error) {
                Text(s.fillShortcut, color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = {
                    if (keys.isBlank() || titleEn.isBlank()) {
                        error = true
                    } else {
                        viewModel.addShortcut(
                            softwareId, keys, titleEn, titleUr, groupEn, groupUr, exampleEn, exampleUr
                        )
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(s.save)
            }
        }
    }
}
