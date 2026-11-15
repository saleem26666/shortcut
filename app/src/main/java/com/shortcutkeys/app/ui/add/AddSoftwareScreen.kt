package com.shortcutkeys.app.ui.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
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
import com.shortcutkeys.app.data.SoftwareCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSoftwareScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val s = state.strings
    var nameEn by remember { mutableStateOf("") }
    var nameUr by remember { mutableStateOf("") }
    var letter by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(SoftwareCategory.CUSTOM) }
    var expanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s.addSoftware) },
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
                value = nameEn,
                onValueChange = { nameEn = it; error = false },
                label = { Text(s.nameEn) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = nameUr,
                onValueChange = { nameUr = it },
                label = { Text(s.nameUr) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = letter,
                onValueChange = { letter = it.take(2) },
                label = { Text("Icon") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = s.categoryLabel(category),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(s.category) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    SoftwareCategory.entries.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(s.categoryLabel(cat)) },
                            onClick = {
                                category = cat
                                expanded = false
                            }
                        )
                    }
                }
            }
            if (error) {
                Text(s.required, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    if (nameEn.isBlank()) {
                        error = true
                    } else {
                        viewModel.addSoftware(nameEn, nameUr, category, letter)
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
