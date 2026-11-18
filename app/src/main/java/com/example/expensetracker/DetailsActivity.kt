package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.ui.ExpenseDialog
import com.example.expensetracker.ui.ExpenseTheme
import com.example.expensetracker.ui.formatDate
import com.example.expensetracker.ui.formatMoney
import com.example.expensetracker.ui.openNearbyAtm
import com.example.expensetracker.ui.shareText

class DetailsActivity : ComponentActivity() {
    companion object { const val EXTRA_ID = "expense_id" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Read the extra sent by the explicit intent
        val id = intent.getLongExtra(EXTRA_ID, -1L)
        setContent { ExpenseTheme { DetailsScreen(id, onBack = { finish() }) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(id: Long, onBack: () -> Unit, vm: ExpenseViewModel = viewModel()) {
    val context = LocalContext.current
    val expense by remember(id) { vm.observe(id) }.collectAsState(initial = null)
    val currency by vm.currency.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val e = expense
        if (e == null) {
            Text("Expense not found", Modifier.padding(padding).padding(16.dp))
            return@Scaffold
        }
        Column(
            Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(e.title, style = MaterialTheme.typography.headlineSmall)
            Text(formatMoney(e.amount, currency), style = MaterialTheme.typography.headlineMedium)
            Text("Category: ${e.category}")
            Text("Date: ${formatDate(e.date)}")
            if (e.note.isNotBlank()) Text("Note: ${e.note}")

            Button(onClick = { editing = true }, modifier = Modifier.fillMaxWidth()) { Text("Edit") }
            OutlinedButton(
                onClick = { context.shareText("${e.title}: ${formatMoney(e.amount, currency)} (${e.category})") },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Share") }
            OutlinedButton(onClick = { context.openNearbyAtm() }, modifier = Modifier.fillMaxWidth()) {
                Text("Find nearby ATM")
            }
            OutlinedButton(
                onClick = { vm.delete(e); onBack() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Delete") }
        }

        if (editing) {
            ExpenseDialog(
                existing = e,
                onDismiss = { editing = false },
                onSave = { vm.save(it); editing = false }
            )
        }
    }
}
