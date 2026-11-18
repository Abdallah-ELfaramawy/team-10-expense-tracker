package com.example.expensetracker

import android.content.res.ColorStateList
import android.graphics.Color
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ProgressBar
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.data.currencies
import com.example.expensetracker.data.categories
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
    val monthTotal by vm.thisMonthTotal.collectAsStateWithLifecycle()
    val monthCategories by vm.thisMonthByCategory.collectAsStateWithLifecycle()
    val monthlyBudget by vm.monthlyBudget.collectAsStateWithLifecycle()
    val currency by vm.currency.collectAsStateWithLifecycle()

    var showAdd by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var showBudgetEditor by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("All") }
    val visibleExpenses = expenses.filter { selectedCategory == "All" || it.category == selectedCategory }

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
        LazyColumn(Modifier.padding(padding).fillMaxSize()) {
            item { SummaryCard(total = total, byCategory = byCategory, currency = currency) }
            item {
                BudgetCard(
                    spent = monthTotal,
                    budget = monthlyBudget,
                    currency = currency,
                    onEdit = { showBudgetEditor = true }
                )
            }
            item { CategoryChart(byCategory = monthCategories, currency = currency) }
            item {
                CategoryFilters(
                    selected = selectedCategory,
                    onSelect = { selectedCategory = it }
                )
            }
            if (visibleExpenses.isEmpty()) {
                item {
                    Text(
                        if (expenses.isEmpty()) "No expenses yet. Tap + to add one."
                        else "No expenses in this category.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            items(visibleExpenses, key = { it.id }) { expense ->
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

    if (showAdd) {
        ExpenseDialog(
            existing = null,
            onDismiss = { showAdd = false },
            onSave = { vm.save(it); showAdd = false }
        )
    }

    if (showBudgetEditor) {
        BudgetDialog(
            currentBudget = monthlyBudget,
            onDismiss = { showBudgetEditor = false },
            onSave = { vm.setMonthlyBudget(it); showBudgetEditor = false }
        )
    }
}

@Composable
private fun BudgetCard(spent: Double, budget: Double, currency: String, onEdit: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Monthly budget", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onEdit) { Text(if (budget > 0) "Edit" else "Set budget") }
            }
            if (budget > 0) {
                Text("${formatMoney(spent, currency)} of ${formatMoney(budget, currency)}")
                XmlBudgetMeter(
                    progressPercent = ((spent / budget) * 100).toInt().coerceIn(0, 100),
                    overBudget = spent > budget
                )
                Text(
                    if (spent > budget) "You are over budget by ${formatMoney(spent - budget, currency)}"
                    else "${formatMoney(budget - spent, currency)} remaining this month",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Text("Set a monthly limit to track your spending.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/** Inflates a small legacy XML view inside the Compose budget card. */
@Composable
private fun XmlBudgetMeter(progressPercent: Int, overBudget: Boolean) {
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            LayoutInflater.from(context).inflate(R.layout.view_budget_meter, null, false)
        },
        update = { view ->
            view.findViewById<ProgressBar>(R.id.budget_meter_progress).apply {
                progress = progressPercent
                progressTintList = ColorStateList.valueOf(
                    Color.parseColor(if (overBudget) "#B3261E" else "#147D64")
                )
                contentDescription = "$progressPercent% of monthly budget used"
            }
        }
    )
}

@Composable
private fun CategoryChart(byCategory: Map<String, Double>, currency: String) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("This month's spending", style = MaterialTheme.typography.titleMedium)
            if (byCategory.isEmpty()) {
                Text("Add expenses to see your category breakdown.", style = MaterialTheme.typography.bodyMedium)
            } else {
                val maxAmount = byCategory.values.maxOrNull()?.coerceAtLeast(0.01) ?: 1.0
                byCategory.entries.sortedByDescending { it.value }.forEach { (category, amount) ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(category)
                        Text(formatMoney(amount, currency))
                    }
                    LinearProgressIndicator(
                        progress = { (amount / maxAmount).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryFilters(selected: String, onSelect: (String) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        (listOf("All") + categories).forEach { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onSelect(category) },
                label = { Text(category) }
            )
        }
    }
}

@Composable
private fun BudgetDialog(currentBudget: Double, onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    var amount by remember(currentBudget) {
        mutableStateOf(if (currentBudget > 0) currentBudget.toString() else "")
    }
    val parsed = amount.toDoubleOrNull()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Monthly budget") },
        text = {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Budget amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = amount.isNotBlank() && (parsed == null || parsed <= 0),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(enabled = parsed != null && parsed > 0, onClick = { onSave(parsed!!) }) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
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
