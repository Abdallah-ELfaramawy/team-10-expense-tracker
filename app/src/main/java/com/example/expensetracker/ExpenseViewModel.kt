package com.example.expensetracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.AppDatabase
import com.example.expensetracker.data.Expense
import com.example.expensetracker.data.SettingsStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class ExpenseViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).expenseDao()
    private val store = SettingsStore(app)

    private fun <T> Flow<T>.asState(initial: T): StateFlow<T> =
        stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)

    val expenses = dao.observeAll().asState(emptyList())
    val currency = store.currency.asState("EGP")
    val monthlyBudget = store.monthlyBudget.asState(0.0)
    val total = expenses.map { list -> list.sumOf { it.amount } }.asState(0.0)
    val byCategory = expenses.map { list ->
        list.groupBy { it.category }.mapValues { (_, items) -> items.sumOf { it.amount } }
    }.asState(emptyMap())
    val thisMonthExpenses = expenses.map { list ->
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val monthStart = calendar.timeInMillis
        list.filter { it.date >= monthStart }
    }.asState(emptyList())
    val thisMonthTotal = thisMonthExpenses.map { list -> list.sumOf { it.amount } }.asState(0.0)
    val thisMonthByCategory = thisMonthExpenses.map { list ->
        list.groupBy { it.category }.mapValues { (_, items) -> items.sumOf { it.amount } }
    }.asState(emptyMap())

    fun observe(id: Long) = dao.observeById(id)

    fun save(expense: Expense) = viewModelScope.launch { dao.upsert(expense) }
    fun delete(expense: Expense) = viewModelScope.launch { dao.delete(expense) }
    fun setCurrency(value: String) = viewModelScope.launch { store.setCurrency(value) }
    fun setMonthlyBudget(value: Double) = viewModelScope.launch { store.setMonthlyBudget(value) }
}
