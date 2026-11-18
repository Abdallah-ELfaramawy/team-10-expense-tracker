package com.example.expensetracker.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.expensetracker.data.Expense
import com.example.expensetracker.data.categories


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDialog(
    existing: Expense?,
    onDismiss: () -> Unit,
    onSave: (Expense) -> Unit
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var amount by remember { mutableStateOf(existing?.amount?.toString() ?: "") }
    var category by remember { mutableStateOf(existing?.category ?: categories.first()) }
    var note by remember { mutableStateOf(existing?.note ?: "") }
    var menuOpen by remember { mutableStateOf(false) }

    val parsedAmount = amount.toDoubleOrNull()
    val isValid = title.isNotBlank() && parsedAmount != null && parsedAmount > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add expense" else "Edit expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = amount.isNotEmpty() && parsedAmount == null,
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = menuOpen,
                    onExpandedChange = { menuOpen = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuOpen) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        categories.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c) },
                                onClick = { category = c; menuOpen = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValid,
                onClick = {
                    val base = existing ?: Expense(title = "", amount = 0.0, category = category)
                    onSave(
                        base.copy(
                            title = title.trim(),
                            amount = parsedAmount ?: 0.0,
                            category = category,
                            note = note.trim()
                        )
                    )
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}