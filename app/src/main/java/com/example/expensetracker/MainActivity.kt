package com.example.expensetracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.data.currencies
import com.example.expensetracker.ui.ExpenseDialog
import com.example.expensetracker.ui.ExpenseTheme
import com.example.expensetracker.ui.formatDate
import com.example.expensetracker.ui.formatMoney
import com.example.expensetracker.ui.shareText

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ExpenseTheme { HomeScreen() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: ExpenseViewModel = viewModel()) {
    val context = LocalContext.current
    val expenses by vm.expenses.collectAsStateWithLifecycle()
    val total by vm.total.collectAsStateWithLifecycle()
    val byCategory by vm.byCategory.collectAsStateWithLifecycle()
    val currency by vm.currency.collectAsStateWithLifecycle()

    var showAdd by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Tracker") },
                actions = {
                    IconButton(onClick = {
                        val summary = buildString {
                            appendLine("My expenses summary")
                            byCategory.forEach { (c, v) -> appendLine("- $c: ${formatMoney(v, currency)}") }
                            append("Total: ${formatMoney(total, currency)}")
                        }
                        context.shareText(summary)          // implicit intent
                    }) { Icon(Icons.Default.Share, contentDescription = "Share summary") }

                    TextButton(onClick = { menuOpen = true }) { Text(currency) }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        currencies.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c) },
                                onClick = { vm.setCurrency(c); menuOpen = false }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add expense")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            SummaryCard(total = total, byCategory = byCategory, currency = currency)

            if (expenses.isEmpty()) {
                Text(
                    "No expenses yet. Tap + to add one.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            LazyColumn(Modifier.fillMaxWidth()) {
                items(expenses, key = { it.id }) { expense ->
                    ListItem(
                        headlineContent = { Text(expense.title) },
                        supportingContent = { Text("${expense.category} • ${formatDate(expense.date)}") },
                        trailingContent = { Text(formatMoney(expense.amount, currency)) },
                        modifier = Modifier.clickable {
                            // EXPLICIT intent with an extra
                            val intent = Intent(context, DetailsActivity::class.java)
                                .putExtra(DetailsActivity.EXTRA_ID, expense.id)
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }

    if (showAdd) {
        ExpenseDialog(
            existing = null,
            onDismiss = { showAdd = false },
            onSave = { vm.save(it); showAdd = false }
        )
    }
}

@Composable
private fun SummaryCard(total: Double, byCategory: Map<String, Double>, currency: String) {
    Card(Modifier.fillMaxWidth().padding(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Total spent", style = MaterialTheme.typography.labelLarge)
            Text(formatMoney(total, currency), style = MaterialTheme.typography.headlineMedium)
            byCategory.entries.sortedByDescending { it.value }.take(3).forEach { (c, v) ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(c); Text(formatMoney(v, currency))
                }
            }
        }
    }
}
